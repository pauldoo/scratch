package timetrace.kdtree

import org.junit.runner.RunWith
import timetrace.UnitSpec
import timetrace.math.Vector4
import org.scalatest.junit.JUnitRunner
import timetrace.math.Vector4Test
import org.scalacheck.Gen
import timetrace.math.RayLike
import timetrace.photon.Photon
import timetrace.Generators
import timetrace.math.Vector3Test

object KDTreeTest {
  private val dummyPhotons: Gen[Photon] = {
      for (
          l <- Vector4Test.vector4s;
          d <- Vector3Test.vector3sNormalized;
          b <- Gen.choose(0, 5)
          ) yield new Photon(l, new Vector4.SpatiallyNormalized(d, 1.0), b)
  }

  val dummyPointsList: Gen[List[Photon]] = Gen.listOfN(1000, dummyPhotons)
}

@RunWith(classOf[JUnitRunner])
class KDTreeTest extends UnitSpec {

  "kdtrees" should "find closest points in random cloud, and return them in closest last order" in {
    forAll(KDTreeTest.dummyPointsList, Vector4Test.vector4s) { (points: List[Photon], target: Vector4) =>
      {
        println("F")
        
        println(points.size)
        val expected = points.sortBy(x => (target - x.location).magnitude()).take(10).reverse

        val tree = KDTree.build(points.toVector)
        val actual = tree.findClosestTo(target, 10, Vector4(1.0, 0.0, 0.0, 0.0))

        actual should equal(expected)
      }
    }
  }
}

