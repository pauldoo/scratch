#include "Primitive.h"

namespace sili {
    PrimitiveBase::PrimitiveBase()
    {
    }
    
    PrimitiveBase::~PrimitiveBase()
    {
    }
    
    const std::vector<ObjectPtr> PrimitiveBase::References() const
    {
        return std::vector<ObjectPtr>();
    }
    
    void PrimitiveBase::NullAllReferences()
    {
    }    
}
