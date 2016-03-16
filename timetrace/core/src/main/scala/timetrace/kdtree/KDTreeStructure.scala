package timetrace.kdtree

import timetrace.math.Vector4
import timetrace.math.PointLike
import timetrace.math.RayLike
import scala.collection.mutable.PriorityQueue
import scala.annotation.tailrec

// Abstract definitions of the structure of a KD-tree
// separates in-memory representation (KDTreeInMemory) from the algorithm of searching (KDTreeSearch)

trait KDTreeStructure[T <: PointLike] extends java.io.Serializable {
  def bounds(): (Vector4, Vector4)

  def rootNode(): KDTreeStructureNode[T]
}

trait KDTreeStructureNode[T <: PointLike] {
  def pivot(): T
  def splitAxis(): Axis
  def leftChild(): Option[KDTreeStructureNode[T]]
  def rightChild(): Option[KDTreeStructureNode[T]]
  def isLeaf(): Boolean = leftChild.isEmpty && rightChild.isEmpty
}
