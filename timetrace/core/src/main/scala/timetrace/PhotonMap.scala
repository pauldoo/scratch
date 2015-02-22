package timetrace

import timetrace.photon.Photon
import timetrace.kdtree.KDTree

case class PhotonMap(val photonsEmitted: Int, val photons: KDTree[Photon]) {
}
