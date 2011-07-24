#include "Heap.h"

#include "Assert.h"
#include "Object.h"

#include <boost/assert.hpp>
#include <map>
#include <stack>
#include <vector>


namespace sili {
    boost::scoped_ptr<Heap> Heap::sInstance;
    
    Heap* const Heap::Instance()
    {
        if (sInstance.get() == NULL) {
            sInstance.reset(new Heap());
        }
        return sInstance.get();
    }
    
    void Heap::Shutdown()
    {
        sInstance.reset();
    }
    
    Heap::Heap()
    {
    }
    
    Heap::~Heap()
    {
        Sweep();
        BOOST_ASSERT(mAllObjects.empty());
    }
    
    void Heap::NotifyCreated(Object* const object)
    {
        mAllObjects.insert(object);
    }
    
    void Heap::NotifyDestroyed(Object* const object)
    {
        mAllObjects.erase(object);
    }
    
    namespace {
        const std::vector<Object*> Roots(
            const std::set<Object*>& allObjects)
        {
            // Count how many external references there are for each object.
            // Using naked pointers so not to confuse myself.
            std::map<Object*, int> externalReferenceCounts;
            
            typedef std::set<Object*>::const_iterator T;
            for (T i = allObjects.begin(); i != allObjects.end(); ++i) {
                Object* const object = *i;
                externalReferenceCounts[object] += object->ReferenceCount();
                
                const std::vector<ObjectPtr> targets = object->References();
                typedef std::vector<ObjectPtr>::const_iterator U;
                for (U j = targets.begin(); j != targets.end(); ++j) {
                    Object* const target = j->get();
                    externalReferenceCounts[target] --;
                }
            }
            
            std::vector<Object*> roots;
            typedef std::map<Object*, int>::const_iterator V;
            for (V i = externalReferenceCounts.begin(); i != externalReferenceCounts.end(); i++) {
                if (i->second > 0) {
                    roots.push_back(i->first);
                }
            }
            return roots;
        }
        
        const std::set<Object*> Reachable(const std::vector<Object*>& roots)
        {
            std::vector<Object*> stack = roots;
            
            std::set<Object*> result;
            while (stack.empty() == false) {
                Object* const object = stack.back();
                stack.pop_back();
                
                if (result.find(object) == result.end()) {
                    result.insert(object);
                    
                    const std::vector<ObjectPtr> targets = object->References();
                    typedef std::vector<ObjectPtr>::const_iterator U;
                    for (U j = targets.begin(); j != targets.end(); ++j) {
                        stack.push_back(j->get());
                    }
                }
            }
            
            return result;
        }
    }
    
    void Heap::Sweep()
    {
        const std::set<Object*> reachable = Reachable(Roots(mAllObjects));
        
        std::vector<ObjectPtr> unreachable;
        std::set_difference(
            mAllObjects.begin(), mAllObjects.end(),
            reachable.begin(), reachable.end(),
            std::back_inserter(unreachable));
        //std::vector<ObjectPtr> unreachable2(unreachable.begin(), unreachable.end());

        typedef std::vector<ObjectPtr>::const_iterator T;
        for (T i = unreachable.begin(); i != unreachable.end(); ++i) {
            const ObjectPtr object = *i;
            object->NullAllReferences();
        }
        
        unreachable.clear();
    }
}