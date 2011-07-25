#pragma once

#include "Object.h"

namespace sili {
    class PrimitiveBase : public Object
    {
    public:
        const std::vector<ObjectPtr> References() const;
        void NullAllReferences();
        
    protected:
        PrimitiveBase();
        ~PrimitiveBase() = 0;
    };
    
    template<typename T>
    class Primitive : public PrimitiveBase
    {
    public:
        static const boost::intrusive_ptr<Primitive> New(const T& value);
        
        void WriteAsString(std::wostream&) const;
        
        const T mValue;
        
    private:
        Primitive(const T& value);
        ~Primitive();
    };
}

