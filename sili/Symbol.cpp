#include "Symbol.h"

namespace sili {
    const boost::intrusive_ptr<Symbol> Symbol::New(const std::wstring& name)
    {
        return new Symbol(name);
    }
    
    Symbol::Symbol(const std::wstring& name) : mName(name)
    {
    }
    
    Symbol::~Symbol()
    {
    }
    
    const std::vector<ObjectPtr> Symbol::References() const
    {
        return std::vector<ObjectPtr>();
    }
    
    void Symbol::NullAllReferences()
    {
    }    
    
    void Symbol::WriteAsString(std::wostream& out) const
    {
        out << mName;
    }
}
