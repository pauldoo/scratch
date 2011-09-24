#pragma once

#include "ListFwd.h"
#include "ObjectFwd.h"

#include <string>

namespace sili {
    namespace BuiltinProcedures {
        const ObjectPtr Invoke(
            const std::wstring& builtinName,
            const ListPtr& argumentValues);
    }
};
