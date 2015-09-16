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

  def incomingLightAt(location: Vector4): List[PhotonMap.Contribution] = {

    // TODO: not sure how much of the cone modulation, normalization, etc to do here vs in the caller.
    
    val closestPhotons: List[Photon] = photons.findClosestTo(location, 100)
    
    def photonDistance(photon: Photon) : Double = (location - photon.location).magnitude()
    val distanceToFurthestPhoton = photonDistance(closestPhotons.last)
    def coneModulation(photon: Photon) : Double = 1.0 - (photonDistance(photon) / distanceToFurthestPhoton)
    
    val denominator = closestPhotons.map(coneModulation _).sum
    val areaNormalization = math.pow(distanceToFurthestPhoton, 3.0)
   
    def contributionFromPhoton( photon: Photon ) : PhotonMap.Contribution = {
      PhotonMap.Contribution(photon.direction, photon.color * (coneModulation(photon) / denominator / areaNormalization))
    }
    
    closestPhotons.map( contributionFromPhoton _ )    
  }
}
