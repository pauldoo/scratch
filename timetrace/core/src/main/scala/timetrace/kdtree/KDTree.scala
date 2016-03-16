package timetrace.kdtree

import timetrace.math.Vector4
import scala.language.implicitConversions
import scala.collection.mutable.PriorityQueue
import scala.annotation.tailrec
import timetrace.math.RayLike
import timetrace.math.PointLike
import java.io.File
import java.io.FileOutputStream
import java.io.DataOutputStream
import java.io.BufferedOutputStream
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.atomic.AtomicLong
import timetrace.photon.Photon

object KDTree {
  def build(points: IndexedSeq[Photon]): KDTree[Photon] = {
    assert(!points.isEmpty)

    val pointLocations = points.map(_.location)
    val mins: Vector4 = pointLocations.reduce(Vector4.componentMinimums)
    val maxs: Vector4 = pointLocations.reduce(Vector4.componentMaximums)

    println("Building KD tree in memory")
    val result = new KDTreeInMemory(mins, maxs, Await.result(KDTreeInMemory.buildNode(points), Duration.Inf))
    println("Done building KD tree in memory")

    new KDTree(result)
  }

}

class KDTree[T <: RayLike](val tree: KDTreeStructure[T]) extends java.io.Serializable {

  @transient
  private val requestCount: AtomicLong = new AtomicLong

  def findClosestTo(target: Vector4, n: Int, interestingHemisphere: Vector4): Seq[T] = {
    val beginTime = System.nanoTime()
    val result = findClosestToImp(target, n, interestingHemisphere)
    val endTime = System.nanoTime()

    if ((requestCount.get % 1000) == 0) {
      println(s"KD tree lookup took ${(endTime - beginTime) / 1000000.0}ms")
    }
    requestCount.incrementAndGet()

    result
  }

  private def findClosestToImp(target: Vector4, n: Int, interestingHemisphere: Vector4): Seq[T] = {
    KDTreeSearch.findClosest(tree, target, n, interestingHemisphere)
  }
}
