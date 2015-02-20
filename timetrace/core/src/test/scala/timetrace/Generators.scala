package timetrace

import org.scalacheck.Gen
import timetrace.math.Vector3
import timetrace.math.Vector4

/**
 * Generators for primitive types.
 */
object Generators {
  val numbers: Gen[Double] = Gen.choose(-1e3, 1e3)
}