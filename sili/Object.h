#pragma once

#include "ObjectFwd.h"

#include <vector>

namespace sili {
    class Object {
    public:
        const int ReferenceCount() const;
        virtual const std::vector<ObjectPtr> References() const = 0;
        virtual void NullAllReferences() = 0;
        
        /// Returns non-null results of AsA00(), raises an error otherwise.
        template<typename T>
        const boost::intrusive_ptr<T> AsA();
        /// Returns non-null results of AsA00(), raises an error otherwise.
        template<typename T>
        const boost::intrusive_ptr<T> AsA() const;

        /// Cast to another type, returning null if object is null or the wrong type.
        template<typename T>
        const boost::intrusive_ptr<T> AsA00();
        /// Cast to another type, returning null if object is null or the wrong type.
        template<typename T>
        const boost::intrusive_ptr<T> AsA00() const;
        
        /// True if AsA00() would return non-null.
        template<typename T> const bool IsA();
        /// True if AsA00() would return non-null.
        template<typename T> const bool IsA() const;
        
        
        virtual void WriteAsString(std::wostream&) const = 0;
        
        // Would be nice to just use the object address, but that won't fit into 'double' on 64-bit. :/
        const int mInstanceNumber;
        
    protected:
        Object();
        virtual ~Object() = 0;
        
    private:
        static int sInstanceCounter;
        mutable int mReferenceCount;
        
        void IncrementCount() const;
        void DecrementCount() const;
        
        friend void ::intrusive_ptr_add_ref(const sili::Object* const);
        friend void ::intrusive_ptr_release(const sili::Object* const);
    };

}

