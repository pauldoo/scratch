package timetrace.kdtree

import timetrace.math.Vector4
import scala.language.implicitConversions

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
  private val rootNode: KDTreeNode[T]) {

}

private sealed trait Axis {
  def extractor(p: Vector4): Double
}

private case object X extends Axis {
  def extractor(p: Vector4): Double = p.x
}

private case object Y extends Axis {
  def extractor(p: Vector4): Double = p.y
}

private case object Z extends Axis {
  def extractor(p: Vector4): Double = p.z
}

private case object T extends Axis {
  def extractor(p: Vector4): Double = p.t
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
