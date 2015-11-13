#pragma once

#include <x86intrin.h>
#include <iosfwd>


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

    inline const float magnitudeSquared(const Vector4& v)
    {
      __m128 t = _mm_dp_ps(v.data, v.data, 0xF1);
      return ((float*)&t)[0];
    }

    inline const float minDistanceToBoundingBox(const Vector4& mins, const Vector4& maxs, const Vector4& target)
    {
        Vector4 closestPoint = min(maxs, max(mins, target));
        return magnitudeSquared(sub(target, closestPoint));
    }

    std::ostream& operator<<(std::ostream& os, const Vector4& obj);

}

