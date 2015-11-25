#pragma once

#include "Assert.h"

#include <x86intrin.h>
#include <iosfwd>
#include <stdint.h>

namespace timetrace {
    struct Vector4 {
        __m128 data;

        Vector4()
        {
        }

        Vector4(__m128 v) : data(v)
        {
        }

        // These are for debugging only, and aren't friendly to the SIMD.
        // For this reason I'm not even inlining them.
        float x() const;
        float y() const;
        float z() const;
        float t() const;
    };

    inline const bool operator == (const Vector4& a, const Vector4& b)
    {
        const uint16_t test = _mm_movemask_epi8(_mm_cmpneq_ps(a.data, b.data));
        return test == 0;
    }

    inline const Vector4 min(const Vector4& a, const Vector4& b)
    {
        return Vector4(_mm_min_ps(a.data, b.data));
    }

    inline const Vector4 max(const Vector4& a, const Vector4& b)
    {
        return Vector4(_mm_max_ps(a.data, b.data));
    }

    inline const Vector4 sub(const Vector4& a, const Vector4& b)
    {
        return Vector4(_mm_sub_ps(a.data, b.data));
    }

    inline const float dotProduct(const Vector4& a, const Vector4& b)
    {
        __m128 t = _mm_dp_ps(a.data, b.data, 0xF1);
        float r = t[0];
        return r;
    }

    inline const float magnitudeSquared(const Vector4& v)
    {
      return dotProduct(v, v);
    }

    inline const float distanceSquared(const Vector4& a, const Vector4& b)
    {
        return magnitudeSquared(sub(a, b));
    }

    inline const float distanceSquaredToBoundingBox(const Vector4& mins, const Vector4& maxs, const Vector4& target)
    {
        DEBUG_ASSERT(min(mins, maxs) == mins);
        DEBUG_ASSERT(max(mins, maxs) == maxs);
        return distanceSquared(target, min(maxs, max(mins, target)));
    }

    std::ostream& operator<<(std::ostream& os, const Vector4& obj);

}

