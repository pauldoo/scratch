#pragma once

#include "Object.h"

namespace sili {
    class Pair;
    typedef boost::intrusive_ptr<Pair> PairPtr;    
    
    class Pair : public Object
    {
    public:
        static const boost::intrusive_ptr<Pair> New(const ObjectPtr& first = NULL, const ObjectPtr& second = NULL);
        
        const std::vector<ObjectPtr> References() const;
        void NullAllReferences();
        void WriteAsString(std::wostream&) const;

        ObjectPtr mFirst;
        // TODO: Change type of mSecond to boost::intrusive_ptr<Pair>, and s/Pair/Cons/.
        PairPtr mSecond;

    private:
        Pair(const ObjectPtr& first, const PairPtr& second);
        ~Pair();
    };
}
