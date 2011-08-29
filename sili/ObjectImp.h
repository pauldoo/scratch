#pragma once

#include "Object.h"

namespace sili {
    template<typename T>
    const boost::intrusive_ptr<T> Object::AsA()
    {
        const boost::intrusive_ptr<T> result = AsA00<T>();
        BOOST_ASSERT(result != NULL);
        return result;
    }

    template<typename T>
    const boost::intrusive_ptr<T> Object::AsA() const
    {
        const boost::intrusive_ptr<T> result = AsA00<T>();
        BOOST_ASSERT(result != NULL);
        return result;
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
        return AsA00<T>() != NULL;
    }
    
    template<typename T>
    const bool Object::IsA() const
    {
        return AsA00<T>() != NULL;
    }
}
