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

object KDTree {
  def build[T <: RayLike](points: List[T]): KDTreeInMemory[T] = {
    assert(!points.isEmpty)

    val pointLocations = points.map(_.location)
    val mins: Vector4 = pointLocations.reduce(Vector4.componentMinimums)
    val maxs: Vector4 = pointLocations.reduce(Vector4.componentMaximums)

    new KDTreeInMemory[T](mins, maxs, KDTreeInMemory.buildNode(points))
  }

}

trait KDTree[T <: RayLike] extends java.io.Serializable {
  def findClosestTo(target: Vector4, n: Int, interestingHemisphere: Vector4): List[T]
}


