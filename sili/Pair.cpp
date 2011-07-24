#include "Pair.h"


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
        std::wcout << __FUNCTION__ << "\n";
        mFirst.reset();
        mSecond.reset();
    }
}
