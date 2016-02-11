package timetrace.kdtree

import timetrace.math.RayLike
import timetrace.math.Vector4
import scala.collection.mutable.PriorityQueue
import scala.annotation.tailrec

object KDTreeSearch {

  def findClosest[T <: RayLike]( //
    tree: KDTreeStructure[T], //
    target: Vector4, //
    n: Int, //
    interestingHemisphere: Vector4): Vector[T] = {

    class NodeWithKnownBoundsAndMinDistance( //
        val node: KDTreeStructureNode[T], //
        val mins: Vector4, //
        val maxs: Vector4) {

      {
        val hasSinglePhoton = (node.leftChild().isEmpty && node.rightChild().isEmpty)
        val boundsArePoint = (mins eq maxs)
        if (hasSinglePhoton) {
          assert(boundsArePoint)
        }
      }

      val minDistance: Double = {
        val closestPoint = Vector4.clamp(mins, maxs)(target)
        (closestPoint - target).magnitude()
      }

      def representsSinglePhoton: Boolean = mins eq maxs
    }

    def predicate(point: RayLike) = {
      (point.direction dot interestingHemisphere) > 0.0
    }

    // Queue of kdtree nodes to try, ordered by closest first
    val queue: PriorityQueue[NodeWithKnownBoundsAndMinDistance] = //
      new PriorityQueue[NodeWithKnownBoundsAndMinDistance]()(Ordering.by(n => -n.minDistance))

    def considerEnqueue( //
      node: KDTreeStructureNode[T], //
      minsHint: Vector4, //
      maxsHint: Vector4): Unit = {

      if (node.isLeaf()) {
        enqueueAsSinglePhoton(node)
      } else {
        queue.enqueue(new NodeWithKnownBoundsAndMinDistance(
          node, minsHint, maxsHint))
      }
    }

    def enqueueAsSinglePhoton( //
      node: KDTreeStructureNode[T]): Unit = {

      if (predicate(node.pivot)) {
        val location = node.pivot().location
        queue.enqueue(new NodeWithKnownBoundsAndMinDistance(
          node, location, location))
      }
    }

    considerEnqueue(tree.rootNode(), tree.bounds()._1, tree.bounds()._2)

    @tailrec
    def find(result: Vector[T]): Vector[T] = {
      if (result.size >= n || queue.isEmpty) {
        result
      } else {
        val curr: NodeWithKnownBoundsAndMinDistance = queue.dequeue()

        if (curr.representsSinglePhoton) {
          find(result :+ curr.node.pivot())
        } else {
          enqueueAsSinglePhoton(curr.node)

          curr.node.leftChild().foreach { leftChild =>
            considerEnqueue(leftChild, curr.mins, curr.node.splitAxis().set(curr.maxs, curr.node.pivot().location))
          }

          curr.node.rightChild().foreach { rightChild =>
            considerEnqueue(rightChild, curr.node.splitAxis().set(curr.mins, curr.node.pivot().location), curr.maxs)
          }

          find(result)
        }

      }
    }

    find(Vector.empty)
  }

}
