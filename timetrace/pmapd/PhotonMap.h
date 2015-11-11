#pragma once
#include <iostream>

#include <x86intrin.h>

namespace timetrace {
    struct Vector4;
    struct Photon;
    struct KDTreeNode;
    struct PhotonMap;
    
    struct Vector4 {
        __m128 data;
        
        float x() const {
            return ((float*)&data)[0];
        }

        float y() const {
            return ((float*)&data)[1];
        }

        float z() const {
            return ((float*)&data)[2];
        }

        float t() const {
            return ((float*)&data)[3];
        }
        
    };
    
    const Vector4 min(const Vector4& a, const Vector4& b)
    {
        return Vector4(_mm_min_ps(a.data, b.data));
    }

    const Vector4 max(const Vector4& a, const Vector4& b)
    {
        return Vector4(_mm_max_ps(a.data, b.data));
    }
    
    const Vector4 sub(const Vector4& a, const Vector4& b)
    {
        return Vector4(_mm_sub_ps(a.data, b.data));
    }
    
    const float magnitudeSquared(const Vector4& v)
    {
      // TODO: check _mm_dp_ps from SSE 4.1
      __m128 t = _mm_mul_ps(v.data, v.data);
      t = _mm_hadd_ps(t, t);
      t = _mm_hadd_ps(t, t);
      return ((float*)&t)[0];
    }
    
    
    std::ostream& operator<<(std::ostream& os, const Vector4& obj)
    {
        os << "(" << obj.x() << ", " << obj.y() << ", " << obj.z() << ", " << obj.t() << ")";
        return os;
    }

#pragma pack(push, 4)    
    struct Photon {
        const Vector4 position;
        const Vector4 direction;
        const int bounceCount;
    };
#pragma pack(pop)
  
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
    
}
