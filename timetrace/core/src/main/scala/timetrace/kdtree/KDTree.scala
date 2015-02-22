package timetrace.kdtree

import timetrace.math.Vector4
import scala.language.implicitConversions

trait PointLike {
  val location: Vector4
}

object KDTree {
  def build[T <: PointLike](points: List[T]): KDTree[T] = {
    assert(!points.isEmpty)
    buildNull(points)
  }

  private def buildNull[T <: PointLike](points: List[T]): KDTree[T] = {
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

      val leftSubTree = buildNull(sortedPoints.slice(0, middleIndex))
      val rightSubTree = buildNull(sortedPoints.slice(middleIndex + 1, sortedPoints.size))

      new KDTreeInnerNode[T]( //
        sortedPoints(middleIndex), //
        splitDirection, //
        leftSubTree, //
        rightSubTree)
    }
  }

  sealed trait Axis {
    def extractor(p: Vector4): Double
  }

  case object X extends Axis {
    def extractor(p: Vector4): Double = p.x
  }

  case object Y extends Axis {
    def extractor(p: Vector4): Double = p.y
  }

  case object Z extends Axis {
    def extractor(p: Vector4): Double = p.z
  }

  case object T extends Axis {
    def extractor(p: Vector4): Double = p.t
  }

  def decideSplitDirection(points: List[PointLike]): Axis = {
    val pointLocations = points.map(_.location)
    val mins: Vector4 = pointLocations.reduce(Vector4.componentMinimums)
    val maxs: Vector4 = pointLocations.reduce(Vector4.componentMaximums)
    val ranges = maxs - mins

    List((ranges.x, X), (ranges.y, Y), (ranges.z, Z), (ranges.t, T)).maxBy(_._1)._2
  }

}

sealed trait KDTree[T <: PointLike] {
  def mins(): Vector4
  def maxs(): Vector4
  def size(): Int
}

case class KDTreeInnerNode[T <: PointLike]( //
  val pivot: T, //
  val axis: KDTree.Axis, //
  val left: KDTree[T], //
  val right: KDTree[T]) extends KDTree[T] {

  assert(left != null || right != null)
  assert(left == null || axis.extractor(left.maxs) <= axis.extractor(pivot.location))
  assert(right == null || axis.extractor(pivot.location) <= axis.extractor(right.mins))

  private def nonNullSubTrees(): List[KDTree[T]] = {
    List(Option(left), Option(right)).flatten
  }

  val mins = (pivot.location :: nonNullSubTrees.map(_.mins)).reduce(Vector4.componentMinimums)
  val maxs = (pivot.location :: nonNullSubTrees.map(_.maxs)).reduce(Vector4.componentMaximums)

  def size = {
    (1 :: nonNullSubTrees.map(_.size)).sum
  }
}

case class KDTreeLeafNode[T <: PointLike]( //
  val point: T) extends KDTree[T] {
  def mins() = point.location
  def maxs() = point.location
  def size() = 1
}
