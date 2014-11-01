package timetrace

import timetrace.photon.Photon

object PhotonMap {
  def build(photons: Array[Photon]): PhotonMap =
    new PhotonMap(photons)
}

class PhotonMap(val photons: Array[Photon]) {

}