#include "Pair.h"

#include "ObjectImp.h"

#include <iostream>

namespace sili {
    const boost::intrusive_ptr<Pair> Pair::New(
        const ObjectPtr& head,
        const ObjectPtr& second)
    {
        BOOST_ASSERT(second == NULL || second->IsA<Pair>());
        return new Pair(head, second.get()->AsA00<Pair>());
    }
    
    Pair::Pair(const ObjectPtr& head, const PairPtr& second) :
        mHead(head),
        mSecond(second)
    {
    }
    
    Pair::~Pair()
    {
    }
    
    const std::vector<ObjectPtr> Pair::References() const
    {
        std::vector<ObjectPtr> result;
        result.push_back(mHead);
        result.push_back(mSecond);
        return result;
    }
    
    void Pair::NullAllReferences()
    {
        mHead = NULL;
        mSecond = NULL;
    }
    
    namespace {
        void WriteInner(std::wostream& out, const boost::intrusive_ptr<Pair const>& pair)
        {
            if (pair.get() != NULL) {
                out << (*(pair->mHead.get()));
                if (pair->mSecond.get() != NULL) {
                    out << L" ";
                    WriteInner(out, pair->mSecond);
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
