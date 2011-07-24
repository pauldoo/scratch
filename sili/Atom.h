#pragma once

#include "Object.h"

namespace sili {
    template<typename T>
    class Atom : public Object
    {
    public:
        static const boost::intrusive_ptr<Atom> New(const T& value);
        
        const std::vector<ObjectPtr> References() const;
        void NullAllReferences();
        
    private:
        Atom(const T& value);
        ~Atom();
        
        const T mValue;
    };
}

