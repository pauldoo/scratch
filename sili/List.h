#pragma once

#include "ListFwd.h"
#include "Object.h"

namespace sili {
    class List : public Object
    {
    public:
        static const boost::intrusive_ptr<List> New(const ObjectPtr& head = NULL, const ObjectPtr& tail = NULL);
        
        const std::vector<ObjectPtr> References() const;
        void NullAllReferences();
        void WriteAsString(std::wostream&) const;

        ObjectPtr mHead;
        ListPtr mTail;

    private:
        List(const ObjectPtr& head, const ListPtr& tail);
        ~List();
    };
}
