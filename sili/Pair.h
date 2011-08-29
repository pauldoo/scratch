#pragma once

#include "Object.h"

namespace sili {
    class Pair;
    typedef boost::intrusive_ptr<Pair> PairPtr;    
    
    class Pair : public Object
    {
    public:
        static const boost::intrusive_ptr<Pair> New(const ObjectPtr& head = NULL, const ObjectPtr& tail = NULL);
        
        const std::vector<ObjectPtr> References() const;
        void NullAllReferences();
        void WriteAsString(std::wostream&) const;

        ObjectPtr mHead;
        PairPtr mTail;

    private:
        Pair(const ObjectPtr& head, const PairPtr& tail);
        ~Pair();
    };
}
