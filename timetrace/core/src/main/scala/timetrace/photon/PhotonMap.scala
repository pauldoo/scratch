package timetrace.photon

import timetrace.kdtree.KDTree
import timetrace.math.Vector4
import timetrace.Color
import timetrace.math.Vector3

object PhotonMap {
  case class Contribution(val incomingDirection: Vector4, val color: Color) {
  }
}

case class PhotonMap(val photonPower: Double, val photons: KDTree[Photon]) {

  def incomingLightAt(location: Vector4, surfaceNormal: Vector3): List[PhotonMap.Contribution] = {

    def photonPredicate(photon: Photon) = {
      (photon.direction.truncateTo3() dot surfaceNormal) < 0.0
    }
    
    // TODO: not sure how much of the cone modulation, normalization, etc to do here vs in the caller.
    val closestPhotons: List[Photon] = photons.findClosestTo(location, 100, photonPredicate _)
    
    def photonDistance(photon: Photon) : Double = (location - photon.location).magnitude()
    val distanceToFurthestPhoton = photonDistance(closestPhotons.head)
    def coneModulation(photon: Photon) : Double = 1.0 - (photonDistance(photon) / distanceToFurthestPhoton)
    
    val denominator = closestPhotons.map(coneModulation _).sum
    // TODO: is ^3.0 correct?
    val areaNormalization = math.pow(distanceToFurthestPhoton, 3.0)
   
    def contributionFromPhoton( photon: Photon ) : PhotonMap.Contribution = {
      PhotonMap.Contribution(photon.direction, photon.color *  (photonPower * coneModulation(photon) / denominator / areaNormalization))
    }
    
    closestPhotons.map( contributionFromPhoton _ )    
  }
}
