#pragma once

#include "Assert.h"

#include <x86intrin.h>
#include <iosfwd>
#include <stdint.h>

namespace timetrace {

    struct Vector4 {
        float data[4];

        float x() const { return data[0]; }
        float y() const { return data[1]; }
        float z() const { return data[2]; }
        float t() const { return data[3]; }
    };

    inline const bool operator == (const Vector4& a, const Vector4& b)
    {
        const uint16_t test = _mm_movemask_epi8(_mm_cmpneq_ps(_mm_loadu_ps(a.data), _mm_loadu_ps(b.data)));
        return test == 0;
    }

    inline const Vector4 min(const Vector4& a, const Vector4& b)
    {
        Vector4 result;
        _mm_storeu_ps(result.data, _mm_min_ps(_mm_loadu_ps(a.data), _mm_loadu_ps(b.data)));
        return result;
    }

    inline const Vector4 max(const Vector4& a, const Vector4& b)
    {
        Vector4 result;
        _mm_storeu_ps(result.data, _mm_max_ps(_mm_loadu_ps(a.data), _mm_loadu_ps(b.data)));
        return result;
    }

    inline const Vector4 sub(const Vector4& a, const Vector4& b)
    {
        Vector4 result;
        _mm_storeu_ps(result.data, _mm_sub_ps(_mm_loadu_ps(a.data), _mm_loadu_ps(b.data)));
        return result;
    }

    inline const float dotProduct(const Vector4& a, const Vector4& b)
    {
        __m128 t = _mm_dp_ps(_mm_loadu_ps(a.data), _mm_loadu_ps(b.data), 0xF1);
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

