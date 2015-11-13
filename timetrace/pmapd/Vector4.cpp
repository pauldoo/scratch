#include "Vector4.h"

#include <ostream>

namespace timetrace {

    float Vector4::x() const {
        return ((float*)&data)[0];
    }

    float Vector4::y() const {
        return ((float*)&data)[1];
    }

    float Vector4::z() const {
        return ((float*)&data)[2];
    }

    float Vector4::t() const {
        return ((float*)&data)[3];
    }

    std::ostream& operator<<(std::ostream& os, const Vector4& obj)
    {
        os << "(" << obj.x() << ", " << obj.y() << ", " << obj.z() << ", " << obj.t() << ")";
        return os;
    }
}

