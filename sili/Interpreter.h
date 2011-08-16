#pragma once

#include "ObjectFwd.h"

namespace sili {
    namespace Interpreter {
        const ObjectPtr Eval(const ObjectPtr& expression, const ObjectPtr& environment);
        
        const ObjectPtr Apply(const ObjectPtr& procedure, const ObjectPtr& argumentExpressions, const ObjectPtr& environment);
    };
}


