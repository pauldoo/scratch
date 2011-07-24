#pragma once

#include <boost/scoped_ptr.hpp>
#include <set>

namespace sili { class Object; }

namespace sili {
    class Heap {
    public:
        static Heap* const Instance();
        static void Shutdown();

        ~Heap();
        
        // Detect all objects which have been created (NotifyCreated has been called on them)
        // and are not reachable from an intrusive_ptr outwith the Object graph.
        void Sweep();
        
        // Called when an object first becomes the target of an intrusive_ptr.
        // The heap may decide to act on this object in someway if it determines
        // in the future that it is only being kept alive due to a cyclic reference.
        void NotifyCreated(Object* const object);
        
        // Called when an object is no longer the target of any intrusive_ptr,
        // and will imminently be destroyed.  The heap will not try to act on this
        // object in the future.
        void NotifyDestroyed(Object* const object);
                
    private:
        Heap();
        
        static boost::scoped_ptr<Heap> sInstance;
        std::set<Object*> mAllObjects;
    };
    
}