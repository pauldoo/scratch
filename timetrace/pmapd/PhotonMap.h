#pragma once

#include "PhotonMapFwd.h"
#include "Vector4.h"

#include <iosfwd>

namespace timetrace {
    struct Photon {
        const Vector4 position;
        const Vector4 direction;
    };

    // TODO: can experiment with packing options
    struct KDTreeNode {
        const Photon photon;
        const int splitAxis;
    };

    struct PhotonMap {
        const int count;
        const Vector4 mins;
        const Vector4 maxs;
        const KDTreeNode* const begin;

        PhotonMap( //
          int count, //
          const Vector4 mins, //
          const Vector4 maxs, //
          const KDTreeNode* begin) :
          count(count),
          mins(mins),
          maxs(maxs),
          begin(begin)
        {
        }
    };

    struct Request {
        Vector4 target;
        Vector4 interestingHemisphere;
        int count;
    };

    void findClosestTo(FILE* const out, const PhotonMap& photonMap, const Request& request);

}
