#pragma once

namespace sili {
    template<typename T>
    const boost::intrusive_ptr<T> Object::As()
    {
        return &dynamic_cast<T&>(*this);
    }

    template<typename T>
    const boost::intrusive_ptr<T> Object::As() const
    {
        return &dynamic_cast<T&>(*this);
    }
}