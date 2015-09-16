package timetrace.kdtree

import timetrace.math.Vector4
import scala.language.implicitConversions
import scala.collection.mutable.PriorityQueue
import scala.annotation.tailrec

trait PointLike {
  val location: Vector4
}

object KDTree {
  def build[T <: PointLike](points: List[T]): KDTree[T] = {
    assert(!points.isEmpty)

    val pointLocations = points.map(_.location)
    val mins: Vector4 = pointLocations.reduce(Vector4.componentMinimums)
    val maxs: Vector4 = pointLocations.reduce(Vector4.componentMaximums)

    new KDTree[T](mins, maxs, buildNode(points))
  }

  private def buildNode[T <: PointLike](points: List[T]): KDTreeNode[T] = {
    if (points.isEmpty) {
      null
    } else if (points.size == 1) {
      new KDTreeLeafNode[T]( //
        points.head)
    } else {
      val splitDirection: Axis = decideSplitDirection(points)
      def sortByFn(p: PointLike) = splitDirection.extractor(p.location)
      val sortedPoints: List[T] = points.sortBy(sortByFn)

      val middleIndex = sortedPoints.size / 2

      val leftSubTree = buildNode(sortedPoints.slice(0, middleIndex))
      val rightSubTree = buildNode(sortedPoints.slice(middleIndex + 1, sortedPoints.size))

      new KDTreeInnerNode[T]( //
        sortedPoints(middleIndex), //
        splitDirection, //
        leftSubTree, //
        rightSubTree)
    }
  }

  private def decideSplitDirection(points: List[PointLike]): Axis = {
    val pointLocations = points.map(_.location)
    val mins: Vector4 = pointLocations.reduce(Vector4.componentMinimums)
    val maxs: Vector4 = pointLocations.reduce(Vector4.componentMaximums)
    val ranges = maxs - mins

    List((ranges.x, X), (ranges.y, Y), (ranges.z, Z), (ranges.t, T)).maxBy(_._1)._2
  }

}

class KDTree[T <: PointLike]( //
  private val mins: Vector4, //
  private val maxs: Vector4, //
  private val rootNode: KDTreeNode[T]) extends java.io.Serializable {

  def findClosestTo(target: Vector4, n: Int): List[T] = {

    class NodeWithKnownBoundsAndMinDistance( //
      val node: KDTreeNode[T], //
      val mins: Vector4, //
      val maxs: Vector4) {
      val minDistance: Double = {
        val closestPoint = Vector4.clamp(mins, maxs)(target)
        (closestPoint - target).magnitude()
      }
    }

    class PointWithDistance( //
      val point: T) {
      val distance = (point.location - target).magnitude()
    }

    // Queue of kdtree nodes to try, ordered by closest first
    val queue: PriorityQueue[NodeWithKnownBoundsAndMinDistance] = //
      new PriorityQueue[NodeWithKnownBoundsAndMinDistance]()(Ordering.by(n => -n.minDistance))

    def considerEnqueue( //
      node: KDTreeNode[T], //
      minsHint: Vector4, //
      maxsHint: Vector4): Unit = {
      node match {
        case null => ()
        case leaf: KDTreeLeafNode[T] => {
          queue.enqueue(new NodeWithKnownBoundsAndMinDistance(
            leaf, leaf.point.location, leaf.point.location))
        }
        case inner: KDTreeInnerNode[T] => {
          queue.enqueue(new NodeWithKnownBoundsAndMinDistance(
            inner, minsHint, maxsHint))
        }
      }
    }

    considerEnqueue(rootNode, mins, maxs)

    @tailrec
    def find(result: List[T]): List[T] = {
      if (result.size >= n || queue.isEmpty) {
        result
      } else {
        val curr = queue.dequeue()

        curr.node match {
          case leaf: KDTreeLeafNode[T] => {

            find(leaf.point :: result)

          }
          case inner: KDTreeInnerNode[T] => {
            considerEnqueue(new KDTreeLeafNode[T](inner.pivot), null, null)
            if (inner.left != null) {
              considerEnqueue(inner.left, curr.mins, inner.axis.set(curr.maxs, inner.pivot.location))
            }
            if (inner.right != null) {
              considerEnqueue(inner.right, inner.axis.set(curr.mins, inner.pivot.location), curr.maxs)
            }

            find(result)
          }
        }

      }
    }

    find(List.empty)
  }

}

private sealed trait Axis {
  def extractor(p: Vector4): Double

  def set(p: Vector4, v: Vector4): Vector4
}

private case object X extends Axis {
  def extractor(p: Vector4): Double = p.x

  def set(p: Vector4, v: Vector4): Vector4 =
    new Vector4(v.x, p.y, p.z, p.t)
}

private case object Y extends Axis {
  def extractor(p: Vector4): Double = p.y

  def set(p: Vector4, v: Vector4): Vector4 =
    new Vector4(p.x, v.y, p.z, p.t)
}

private case object Z extends Axis {
  def extractor(p: Vector4): Double = p.z

  def set(p: Vector4, v: Vector4): Vector4 =
    new Vector4(p.x, p.y, v.z, p.t)
}

private case object T extends Axis {
  def extractor(p: Vector4): Double = p.t

  def set(p: Vector4, v: Vector4): Vector4 =
    new Vector4(p.x, p.y, p.z, v.t)
}

private sealed trait KDTreeNode[T <: PointLike] {
}

private case class KDTreeInnerNode[T <: PointLike]( //
  val pivot: T, //
  val axis: Axis, //
  val left: KDTreeNode[T], //
  val right: KDTreeNode[T]) extends KDTreeNode[T] {

  assert(left != null || right != null)

  private def nonNullSubTrees(): List[KDTreeNode[T]] = {
    List(Option(left), Option(right)).flatten
  }
}

private case class KDTreeLeafNode[T <: PointLike]( //
  val point: T) extends KDTreeNode[T] {
}
