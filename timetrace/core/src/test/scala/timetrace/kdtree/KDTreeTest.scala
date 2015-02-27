package timetrace.kdtree

import org.junit.runner.RunWith
import timetrace.UnitSpec
import timetrace.math.Vector4
import org.scalatest.junit.JUnitRunner
import timetrace.math.Vector4Test
import org.scalacheck.Gen

object KDTreeTest {
  private val dummyPoints: Gen[DummyPoint] = Vector4Test.vector4s.map(x => new DummyPoint(x))

  val dummyPointsList: Gen[List[DummyPoint]] = Gen.listOfN(1000, dummyPoints)
}

@RunWith(classOf[JUnitRunner])
class KDTreeTest extends UnitSpec {

  "kdtrees" should "find closest points in random cloud" in {
    forAll(KDTreeTest.dummyPointsList, Vector4Test.vector4s) { (points: List[DummyPoint], target: Vector4) =>
      {
        val expected = points.sortBy(x => (target - x.location).magnitude()).take(10).toSet

        val tree = KDTree.build(points)
        val actual = tree.findClosestTo(target, 10).toSet

        actual should equal(expected)
      }
    }
  }
}

case class DummyPoint(val location: Vector4) extends PointLike {}
