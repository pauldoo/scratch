package timetrace.photon

import timetrace.kdtree.KDTree
import timetrace.math.Vector4
import timetrace.Color
import timetrace.math.Vector3
import scala.math.max
import timetrace.kdtree.KDTreeStructure

object PhotonMap {
  case class Contribution(val incomingDirection: Vector4, val color: Color) {
  }
}

case class PhotonMap(val photonPower: Double, val photons: KDTree[Photon]) {

  def incomingLightAt(location: Vector4, surfaceNormal: Vector3): Seq[PhotonMap.Contribution] = {

    // TODO: not sure how much of the cone modulation, normalization, etc to do here vs in the caller.
    val closestPhotons: Seq[Photon] = photons.findClosestTo(location, 100, (-surfaceNormal).to4(0.0))

    def photonDistance(photon: Photon): Double = (location - photon.location).magnitude()
    val distanceToFurthestPhoton = photonDistance(closestPhotons.last)
    def coneModulation(photon: Photon): Double = {
      val distance = photonDistance(photon)
      // small tolerance allowed, since photon map actually works with floats.
      assert(distance <= (distanceToFurthestPhoton * 1.001))
      val modulation = max(0.0, 1.0 - (distance / distanceToFurthestPhoton))
      modulation
    }

    val denominator = closestPhotons.map(coneModulation _).sum
    // TODO: is ^3.0 correct?
    val areaNormalization = math.pow(distanceToFurthestPhoton, 3.0)

    def contributionFromPhoton(photon: Photon): PhotonMap.Contribution = {
      PhotonMap.Contribution(photon.direction, photon.color * (photonPower * coneModulation(photon) / denominator / areaNormalization))
    }

    closestPhotons.map(contributionFromPhoton _)
  }
}
