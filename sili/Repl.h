#pragma once

#include "ObjectFwd.h"

namespace sili {
    namespace Repl {
        void Repl(
                std::wistream& in, 
                std::wostream& out, 
                std::wostream& error, 
                const ObjectPtr& environment);
    }
}
