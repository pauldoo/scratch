#pragma once

#include "ObjectFwd.h"

#include <iosfwd>

namespace sili {
    namespace Parser {
        const ObjectPtr ParseFromStream(std::wistream& in, std::wostream& error);
    }
}
