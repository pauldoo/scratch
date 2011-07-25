#pragma once

#include "Primitive.h"

namespace sili {
    template<typename T>
    const boost::intrusive_ptr<Primitive<T> > Primitive<T>::New(const T& value)
    {
        return new Primitive(value);
    }
    
    template<typename T>
    Primitive<T>::Primitive(const T& value) : mValue(value)
    {
    }
    
    template<typename T>
    Primitive<T>::~Primitive()
    {
    }
    
    template<typename T>
    void Primitive<T>::WriteAsString(std::wostream& out) const
    {
        out << L"<" << mValue << L">";
    }
}
