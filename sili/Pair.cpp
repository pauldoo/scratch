#include "Pair.h"

#include "ObjectImp.h"

#include <iostream>

namespace sili {
    const boost::intrusive_ptr<Pair> Pair::New(
        const ObjectPtr& first,
        const ObjectPtr& second)
    {
        return new Pair(first, second);
    }
    
    Pair::Pair(const ObjectPtr& first, const ObjectPtr& second) :
        mFirst(first),
        mSecond(second)
    {
    }
    
    Pair::~Pair()
    {
    }
    
    const std::vector<ObjectPtr> Pair::References() const
    {
        std::vector<ObjectPtr> result;
        result.push_back(mFirst);
        result.push_back(mSecond);
        return result;
    }
    
    void Pair::NullAllReferences()
    {
        mFirst = NULL;
        mSecond = NULL;
    }
    
    namespace {
        void WriteInner(std::wostream& out, const boost::intrusive_ptr<Pair const>& pair)
        {
            if (pair.get() != NULL) {
                out << (*(pair->mFirst.get()));
                if (pair->mSecond.get() != NULL) {
                    out << L" ";
                    WriteInner(out, pair->mSecond->AsA<Pair>());
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
