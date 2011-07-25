#pragma once

#include "Object.h"

namespace sili {
    template<typename T>
    const boost::intrusive_ptr<T> Object::AsA()
    {
        return &dynamic_cast<T&>(*this);
    }

    template<typename T>
    const boost::intrusive_ptr<T> Object::AsA() const
    {
        return &dynamic_cast<T&>(*this);
    }
    
    template<typename T>
    const boost::intrusive_ptr<T> Object::AsA00()
    {
        return dynamic_cast<T*>(this);
    }

    template<typename T>
    const boost::intrusive_ptr<T> Object::AsA00() const
    {
        return dynamic_cast<T*>(this);
    }

    template<typename T>
    const bool Object::IsA()
    {
        return dynamic_cast<T*>(this) != NULL;
    }
    
    template<typename T>
    const bool Object::IsA() const
    {
        return dynamic_cast<T*>(this) != NULL;
    }
}
