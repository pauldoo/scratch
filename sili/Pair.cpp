#include "Pair.h"

#include "ObjectImp.h"

#include <iostream>

namespace sili {
    const boost::intrusive_ptr<Pair> Pair::New(
        const ObjectPtr& head,
        const ObjectPtr& tail)
    {
        BOOST_ASSERT(tail == NULL || tail->IsA<Pair>());
        return new Pair(head, tail.get()->AsA00<Pair>());
    }
    
    Pair::Pair(const ObjectPtr& head, const PairPtr& tail) :
        mHead(head),
        mTail(tail)
    {
    }
    
    Pair::~Pair()
    {
    }
    
    const std::vector<ObjectPtr> Pair::References() const
    {
        std::vector<ObjectPtr> result;
        result.push_back(mHead);
        result.push_back(mTail);
        return result;
    }
    
    void Pair::NullAllReferences()
    {
        mHead = NULL;
        mTail = NULL;
    }
    
    namespace {
        void WriteInner(std::wostream& out, const boost::intrusive_ptr<Pair const>& pair)
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
    
    void Pair::WriteAsString(std::wostream& out) const
    {
        out << L"(";
        WriteInner(out, this);
        out << L")";
    }
}
