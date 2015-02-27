package timetrace.photon

import timetrace.kdtree.KDTree
import timetrace.math.Vector4
import timetrace.Color
import timetrace.math.Vector3

object PhotonMap {
  case class Contribution(val incomingDirection: Vector4, val color: Color) {
  }
}

case class PhotonMap(val photonsEmitted: Int, val photons: KDTree[Photon]) {

  def incomingLightAt(location: Vector4, surfaceNormal: Vector3): List[PhotonMap.Contribution] = {

    val closesPhotons: List[Photon] = photons.findClosestTo(location, 100)
    ???
  }
}
