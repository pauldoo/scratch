#pragma once

#include "ObjectFwd.h"

namespace sili {
    namespace Interpreter {
        extern const std::wstring LAMBDA;
        extern const std::wstring DEFINE;
        extern const std::wstring COMPOUND_PROCEDURE;
        extern const std::wstring SET;
        
        const ObjectPtr Eval(const ObjectPtr& expression, const ObjectPtr& environment);
        
        const ObjectPtr Apply(const ObjectPtr& procedure, const ObjectPtr& arguments);
    };
}


