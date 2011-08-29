#include "List.h"

#include "ObjectImp.h"

#include <iostream>

namespace sili {
    const boost::intrusive_ptr<List> List::New(
        const ObjectPtr& head,
        const ObjectPtr& tail)
    {
        BOOST_ASSERT(tail == NULL || tail->IsA<List>());
        return new List(head, tail.get()->AsA00<List>());
    }
    
    List::List(const ObjectPtr& head, const ListPtr& tail) :
        mHead(head),
        mTail(tail)
    {
    }
    
    List::~List()
    {
    }
    
    const std::vector<ObjectPtr> List::References() const
    {
        std::vector<ObjectPtr> result;
        result.push_back(mHead);
        result.push_back(mTail);
        return result;
    }
    
    void List::NullAllReferences()
    {
        mHead = NULL;
        mTail = NULL;
    }
    
    namespace {
        void WriteInner(std::wostream& out, const boost::intrusive_ptr<List const>& pair)
        {
            if (pair.get() != NULL) {
                out << (*(pair->mHead.get()));
                if (pair->mTail.get() != NULL) {
                    out << L" ";
                    WriteInner(out, pair->mTail);
                }
            }
        }
    }
    
    void List::WriteAsString(std::wostream& out) const
    {
        out << L"(";
        WriteInner(out, this);
        out << L")";
    }
}
