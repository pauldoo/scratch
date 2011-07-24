#pragma once

#include "Atom.h"

namespace sili {
    template<typename T>
    const boost::intrusive_ptr<Atom<T> > Atom<T>::New(const T& value)
    {
        return new Atom(value);
    }
    
    template<typename T>
    const std::vector<ObjectPtr> Atom<T>::References() const
    {
        return std::vector<ObjectPtr>();
    }
    
    template<typename T>
    void Atom<T>::NullAllReferences()
    {
    }
    
    template<typename T>
    Atom<T>::Atom(const T& value) : mValue(value)
    {
    }
    
    template<typename T>
    Atom<T>::~Atom()
    {
    }
}
