#include "Vector4.h"

#include <ostream>

namespace timetrace {

    float Vector4::x() const {
        return data[0];
    }

    float Vector4::y() const {
        return data[1];
    }

    float Vector4::z() const {
        return data[2];
    }

    float Vector4::t() const {
        return data[3];
    }

    std::ostream& operator<<(std::ostream& os, const Vector4& obj)
    {
        os << "(" << obj.x() << ", " << obj.y() << ", " << obj.z() << ", " << obj.t() << ")";
        return os;
    }
}

