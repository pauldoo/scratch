#pragma once

#include "Primitive.h"

#include <iostream>

namespace sili {
    namespace {
        template<typename T> struct PrintTrait {
            static const std::wstring begin() { return L""; }
            static const std::wstring end() { return L""; }
        };
        
        template<> struct PrintTrait<std::wstring> {
            static const std::wstring begin() { return L"\""; }
            static const std::wstring end() { return L"\""; }
        };
    }
    
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
        out << PrintTrait<T>::begin() << mValue << PrintTrait<T>::end();
    }
}
