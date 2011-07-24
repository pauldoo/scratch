#pragma once

#include "ObjectFwd.h"

#include <vector>

namespace sili {
    class Object {
    public:
        const int ReferenceCount() const;
        virtual const std::vector<ObjectPtr> References() const = 0;
        virtual void NullAllReferences() = 0;
        
        template<typename T>
        const boost::intrusive_ptr<T> As();
        template<typename T>
        const boost::intrusive_ptr<T> As() const;
        
    protected:
        Object();
        virtual ~Object() = 0;
        
    private:
        mutable int mReferenceCount;
        
        void IncrementCount() const;
        void DecrementCount() const;
        
        friend void ::intrusive_ptr_add_ref(const sili::Object* const);
        friend void ::intrusive_ptr_release(const sili::Object* const);
    };
}
    
