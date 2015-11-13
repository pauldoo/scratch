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
#pragma pack(push, 4)
    struct KDTreeNode {
        const Photon photon;
        const int splitAxis;
    };
#pragma pack(pop)

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

#pragma pack(push, 4)
    struct Request {
        Vector4 target;
        int count;
        Vector4 interestingHemisphere;
    };
#pragma pack(pop)

    void findClosestTo(FILE* const out, const PhotonMap& photonMap, const Request& request);

}
