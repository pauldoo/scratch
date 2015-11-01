#pragma once

#include <x86intrin.h>

namespace timetrace {
    struct Vector4;
    struct Photon;
    struct KDTreeNode;
    struct PhotonMap;
    
    struct Vector4 {
        __m128 data;
    };
    
    struct Photon {
        const Vector4 position;
        const Vector4 direction;
    };
  
    // TODO: can experiment with packing options
//#pragma pack(push, 1)
    struct KDTreeNode {
        const char splitAxis;
        const Photon photon;
    };
//#pragma pack(pop)
    
    struct PhotonMap {
        const int count;
        const KDTreeNode* const begin;
    };
    
}