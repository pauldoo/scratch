#pragma once

#include "ObjectFwd.h"

namespace sili {
    namespace Interpreter {
        const ObjectPtr eval(const ObjectPtr& expression, const ObjectPtr& environment);
        
        const ObjectPtr apply(const ObjectPtr& procedure, const ObjectPtr& arguments);
    };
}


