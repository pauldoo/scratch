#include "Vector4.h"

#include <ostream>

namespace timetrace {

    std::ostream& operator<<(std::ostream& os, const Vector4& obj)
    {
        os << "(" << obj.x() << ", " << obj.y() << ", " << obj.z() << ", " << obj.t() << ")";
        return os;
    }
}

