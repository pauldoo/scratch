package timetrace.shape

import timetrace.Ray
import timetrace.math.Vector4
import timetrace.math.RayLike
import org.apache.commons.math3.random.RandomGenerator

trait Shape extends Serializable {

  def intersect(ray: RayLike, rng: RandomGenerator): Option[ShapeHit]
}
