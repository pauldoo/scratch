package timetrace

import timetrace.photon.Photon

object PhotonMap {
  def build(photons: Array[Photon]): PhotonMap =
    new PhotonMap(photons)
}

case class PhotonMap(val photons: Array[Photon]) {

}