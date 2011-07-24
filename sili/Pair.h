#pragma once

#include "Object.h"

namespace sili {
    class Pair : public Object
    {
    public:
        static const boost::intrusive_ptr<Pair> New(const ObjectPtr& first = NULL, const ObjectPtr& second = NULL);
        
        const std::vector<ObjectPtr> References() const;
        void NullAllReferences();

        ObjectPtr mFirst;
        ObjectPtr mSecond;
        
    private:
        Pair(const ObjectPtr& first, const ObjectPtr& second);
        ~Pair();
    };
}
