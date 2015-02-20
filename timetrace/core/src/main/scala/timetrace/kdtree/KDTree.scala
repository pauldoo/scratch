package timetrace.kdtree

import timetrace.math.Vector4
import scala.language.implicitConversions

trait PointLike {
  def location(): Vector4
}

object KDTree {
  def build[T <: PointLike](points: List[T]): KDTree[T] = {

    val splitDirection: Axis = decideSplitDirection(points)

    ???
  }

  sealed abstract class Axis
  case object X extends Axis
  case object Y extends Axis
  case object Z extends Axis
  case object T extends Axis

  def decideSplitDirection(points: List[PointLike]): Axis = {
    val pointLocations = points.map(_.location)
    val mins: Vector4 = pointLocations.reduce(Vector4.componentMinimums)
    val maxs: Vector4 = pointLocations.reduce(Vector4.componentMaximums)

    println(mins)
    println(maxs)

    ???
  }

}

sealed class KDTree[T <: PointLike](
  val pivox: T, //
  val axis: KDTree.Axis, //
  val left: KDTree[T], //
  val right: KDTree[T]) {

}
