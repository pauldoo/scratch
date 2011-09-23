#include "Object.h"

#include "Heap.h"

#include <iostream>

namespace sili {
    int Object::sInstanceCounter = 0;
    
    Object::Object() :
        mInstanceNumber(sInstanceCounter++),
        mReferenceCount(0)
    {
        Heap::Instance()->NotifyCreated(this);
    }
    
    Object::~Object()
    {
        Heap::Instance()->NotifyDestroyed(this);
    }
    
    const int Object::ReferenceCount() const
    {
        return mReferenceCount;
    }
    
    void Object::IncrementCount() const
    {
        mReferenceCount++;
    }
    
    void Object::DecrementCount() const
    {
        mReferenceCount--;
        if (mReferenceCount == 0) {
            delete this;
        }
    }
}

void intrusive_ptr_add_ref(const sili::Object* const object) {
    object->IncrementCount();
}

void intrusive_ptr_release(const sili::Object* const object) {
    object->DecrementCount();
}

std::wostream& operator << (std::wostream& out, const sili::Object& object)
{
    if (&object == NULL) {
        out << L"()";
    } else {
        object.WriteAsString(out);
    }
    return out;
}
