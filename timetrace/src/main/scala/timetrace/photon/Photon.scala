package timetrace.photon

import timetrace.Color
import timetrace.math.Vector3
import timetrace.math.Vector4
import timetrace.math.RayLike

/// Represents light from a light source as it is inbound on an interacting thing
case class Photon(val location: Vector4, val direction: Vector4.SpatiallyNormalized, val color: Color) extends RayLike {
  assume(direction.t == 1.0) // Photons only travel forward in time
}
