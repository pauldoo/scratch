package timetrace.kdtree

import timetrace.math.RayLike
import timetrace.math.Vector4
import scala.collection.mutable.PriorityQueue
import scala.annotation.tailrec
import timetrace.math.PointLike
import java.io.OutputStream
import java.io.DataOutputStream
import timetrace.photon.Photon
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.blocking
import timetrace.kdtree.X
import timetrace.kdtree.Y
import timetrace.kdtree.Z
import timetrace.kdtree.T

object KDTreeInMemory {

  implicit val ec: ExecutionContext = ExecutionContext.global

  def buildNode(points: IndexedSeq[Photon]): Future[KDTreeNodeFlattened] = {
    val computation = () => {
      if (points.isEmpty) {
        null
      } else if (points.size == 1) {
        new KDTreeNodeFlattened( //
          points.head.location.x.toFloat, //
          points.head.location.y.toFloat, //
          points.head.location.z.toFloat, //
          points.head.location.t.toFloat, //
          points.head.direction.x.toFloat, //
          points.head.direction.y.toFloat, //
          points.head.direction.z.toFloat, //
          points.head.direction.t.toFloat, //
          -1, null, null)
      } else {
        val splitDirection: Axis = decideSplitDirection(points)
        def sortByFn(p: PointLike) = splitDirection.extractor(p.location)
        val sortedPoints: IndexedSeq[Photon] = points.sortBy(sortByFn _)

        val middleIndex = sortedPoints.size / 2

        val leftSubTree = buildNode(sortedPoints.slice(0, middleIndex))
        val rightSubTree = buildNode(sortedPoints.slice(middleIndex + 1, sortedPoints.size))

        val pivot = sortedPoints(middleIndex)

        val splitDirectionAsByte: Byte = splitDirection match {
          case X => 1
          case Y => 2
          case Z => 3
          case T => 4
        }

        blocking {
          new KDTreeNodeFlattened( //
            pivot.location.x.toFloat, //
            pivot.location.y.toFloat, //
            pivot.location.z.toFloat, //
            pivot.location.t.toFloat, //
            pivot.direction.x.toFloat, //
            pivot.direction.y.toFloat, //
            pivot.direction.z.toFloat, //
            pivot.direction.t.toFloat, //
            splitDirectionAsByte, //
            Await.result(leftSubTree, Duration.Inf), //
            Await.result(rightSubTree, Duration.Inf))
        }
      }
    }

    if (points.length >= 100000) {
      Future { computation() }
    } else {
      Future.fromTry(Try { computation() })
    }
  }

  private def decideSplitDirection(points: IndexedSeq[PointLike]): Axis = {
    val pointLocations = points.map(_.location)
    val mins: Vector4 = pointLocations.reduce(Vector4.componentMinimums)
    val maxs: Vector4 = pointLocations.reduce(Vector4.componentMaximums)
    val ranges = maxs - mins

    List((ranges.x, X), (ranges.y, Y), (ranges.z, Z), (ranges.t, T)).maxBy(_._1)._2
  }

}

class KDTreeInMemory( //
    private val mins: Vector4, //
    private val maxs: Vector4, //
    val rootNode: KDTreeNodeFlattened) extends KDTreeStructure[Photon] {

  def bounds() = (mins, maxs)
}

class KDTreeNodeFlattened( //
    private val photonLocationX: Float, //
    private val photonLocationY: Float, //
    private val photonLocationZ: Float, //
    private val photonLocationT: Float, //
    private val photonDirectionX: Float, //
    private val photonDirectionY: Float, //
    private val photonDirectionZ: Float, //
    private val photonDirectionT: Float, //
    private val splitAxisAsByte: Byte, //
    private val leftChildAsRef: KDTreeNodeFlattened, //
    private val rightChildAsRef: KDTreeNodeFlattened) extends KDTreeStructureNode[Photon] {

  def splitAxis(): Axis =
    splitAxisAsByte match {
      case 1 => X
      case 2 => Y
      case 3 => Z
      case 4 => T
    }

  def leftChild(): Option[KDTreeStructureNode[Photon]] = Option(leftChildAsRef)

  def rightChild(): Option[KDTreeStructureNode[Photon]] = Option(rightChildAsRef)

  def pivot(): Photon = {
    new Photon(location(), direction(), 0)
  }

  private def location(): Vector4 = Vector4( //
    photonLocationX, //
    photonLocationY, //
    photonLocationZ, //
    photonLocationT)

  private def direction(): Vector4.SpatiallyNormalized = Vector4( //
    photonDirectionX, //
    photonDirectionY, //
    photonDirectionZ, //
    photonDirectionT).spatiallyNormalize()
}
