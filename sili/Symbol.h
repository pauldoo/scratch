#pragma once

#include "Object.h"

#include <string>

namespace sili {
    class Symbol;
    typedef boost::intrusive_ptr<Symbol> SymbolPtr;       
    
    class Symbol : public Object
    {
    public:
        static const boost::intrusive_ptr<Symbol> New(const std::wstring& name);
        
        const std::vector<ObjectPtr> References() const;
        void NullAllReferences();
        void WriteAsString(std::wostream&) const;
        
        const std::wstring mName;
        
    private:
        Symbol(const std::wstring&);
        ~Symbol();
    };
}
