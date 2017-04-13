import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.rdd.RDD._
import org.apache.spark.storage.StorageLevel

import scala.collection.Map
import scala.reflect.ClassTag

/**
  * Created by pauldoo on 13/04/2017.
  */
object HybridJoin {

  /**
    * Logically equivalent to:
    * left.map(_.swap).join(right).values
    *
    * But looks for hot keys, uses a map-side join for those, and does a regular
    * shuffle join for the non-hot keys.
    */
  def hybridJoin[A, B, C]
  (context: SparkContext)
  (left: RDD[(A, B)], right: RDD[(B, C)], limit: Long)
  (implicit at: ClassTag[A], bt: ClassTag[B], ct: ClassTag[C])
  : RDD[(A, C)] = {

    // not used..
    val logicallyEquivalentResult: RDD[(A, C)] = left.map(_.swap).join(right).values

    val counts: RDD[(B, (Long, Seq[C]))] = left
      .map { case (_, b) => (b, 1L) }
      .reduceByKey(_ + _)
      .cogroup(right)
      .flatMapValues {
        case (countIt: Iterable[Long], cIt: Iterable[C]) => {
          val counts: Seq[Long] = countIt.toSeq
          assert(counts.size <= 1)

          if (counts.isEmpty) {
            None
          } else {
            Some(counts.head, cIt.toSeq)
          }
        }
      }
      .persist(StorageLevel.DISK_ONLY)

    // In the counts map above values for B present in "left" only have been preserved (they have an empty Seq).
    // This is important to handle hot keys in "left" but with no corresponding value in "right" (we don't want to shuffle them).
    // Values for B present in "right" only have been discarded in the counts map.

    val hotItems: RDD[(B, Seq[C])] = counts
      .filter(_ match { case (_, (count: Long, _)) => (count >= limit) })
      .mapValues(_._2)
    val coldItems: RDD[(B, C)] = counts
      .filter(_ match { case (_, (count: Long, _)) => (count < limit) })
      .flatMapValues(_._2)

    val broadcast: Broadcast[Map[B, Seq[C]]] = context.broadcast(hotItems.collectAsMap())

    val hotJoin: RDD[(A, C)] = left.flatMapValues { b => broadcast.value.getOrElse(b, Seq.empty) }

    val coldJoin: RDD[(A, C)] = left
      .filter(_ match { case (_: A, b: B) => !broadcast.value.contains(b) }) // Omit the B's we are aready handling with a map-side join
      .map(_.swap)
      .join(coldItems)
      .values

    context.union(hotJoin, coldJoin)
  }
}

