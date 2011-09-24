#include "BuiltinProcedures.h"

#include "List.h"
#include "Object.h"
#include "ObjectImp.h"
#include "Primitive.h"
#include "Symbol.h"

#include <sstream>

namespace sili {
    namespace BuiltinProcedures {
        namespace {
            // Builtin procedures
            const std::wstring CONS = L"cons";
            const std::wstring CAR = L"car";
            const std::wstring CDR = L"cdr";
            const std::wstring INSTANCE_ID = L"instance-id";
            const std::wstring TYPE_OF = L"type-of";
            const std::wstring EQUAL_NUMBER = L"equal-number";
            const std::wstring EQUAL_SYMBOL = L"equal-symbol";            
            
            template<typename T>
            const std::wstring ToString(const T& v)
            {
                std::wostringstream buf;
                buf << v;
                return buf.str();
            }            
        }
        
        const ObjectPtr Invoke(
            const std::wstring& builtinName,
            const ListPtr& argumentValues)
        {
            const ObjectPtr arg0 = argumentValues->mHead;
            
            if (builtinName == CAR) {
                return arg0->AsA<List>()->mHead;
            } else if (builtinName == CDR) {
                return arg0->AsA<List>()->mTail;
            } else if (builtinName == INSTANCE_ID) {
                return Primitive<double>::New(arg0->mInstanceNumber);
            } else if (builtinName == TYPE_OF) {
                 return Symbol::New(ToString(typeid(*(arg0.get())).name()));
            } else {
                const ObjectPtr arg1 = argumentValues->mTail->mHead;
                
                if (builtinName == CONS) {
                    return List::New(arg0, arg1);
                } else if (builtinName == EQUAL_NUMBER) {
                    if (arg0->AsA<Primitive<double> >()->mValue ==
                            arg1->AsA<Primitive<double> >()->mValue) {
                        return arg0;
                    } else {
                        return NULL;
                    }
                } else if (builtinName == EQUAL_SYMBOL) {
                    if (arg0->AsA<Symbol>()->mName ==
                            arg1->AsA<Symbol>()->mName) {
                        return arg0;
                    } else {
                        return NULL;
                    }
                } else {
                    BOOST_ASSERT(false);
                }
            }
        }
    }
}
