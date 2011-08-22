#include "Parser.h"

#include "Pair.h"
#include "Primitive.h"
#include "PrimitiveImp.h"
#include "Symbol.h"

#include <cwctype>
#include <iterator>
#include <iostream>
#include <sstream>

namespace sili {
    namespace Parser {
        namespace {
            template<typename T>
            const T FirstNonWhitespace(const T& begin, const T& end) {
                BOOST_ASSERT(begin <= end);
                T i = begin;
                while (i != end &&
                        iswspace(*i)) {
                    ++i;
                }
                return i;
            }
            
            template<typename T>
            const T FirstCloseParenOrWhitespace(const T& begin, const T& end) {
                BOOST_ASSERT(begin <= end);
                T i = begin;
                while (i != end && 
                        *i != L')' && 
                        !iswspace(*i)) {
                    ++i;
                }
                return i;
            }
            
            template<typename T>
            const ObjectPtr ToSexpList(const T& begin, const T& end)
            {
                if (begin == end) {
                    return NULL;
                } else {
                    return Pair::New(*begin, ToSexpList(begin + 1, end));
                }
            }
           
            template<typename R, typename T>
            const R ConsumeToken(T& i, const T& end)
            {
                const T begin = i;
                i = FirstCloseParenOrWhitespace(begin, end);
                
                const std::wstring fragment = std::wstring(begin, i);
                std::wistringstream buf(fragment);
                R value;
                buf >> value;
                BOOST_ASSERT(buf.fail() == false);
                BOOST_ASSERT(buf.eof() == true);
                return value;
            }
            
            template<typename T>
            const ObjectPtr ParseFromString(T& i, const T& end)
            {
                BOOST_ASSERT(i < end);
                i = FirstNonWhitespace(i, end);
                BOOST_ASSERT(i != end);
                
                ObjectPtr result;
                
                if (*i == L'(') {
                    ++i;
                    std::vector<ObjectPtr> items;

                    while (true) {
                        i = FirstNonWhitespace(i, end);
                        if (*i == L')') {
                            ++i;
                            return ToSexpList(items.begin(), items.end());
                        } else {
                            items.push_back(ParseFromString(i, end));
                        }
                    }
                                        
                } else if (*i == L')') {
                    BOOST_ASSERT(false);
                } else if (iswdigit(*i)) {
                    const double value = ConsumeToken<double, T>(i, end);
                    result = Primitive<double>::New(value);
                } else if (iswalpha(*i)) {
                    const std::wstring value = ConsumeToken<std::wstring, T>(i, end);
                    result = Symbol::New(value);
                } else {
                    BOOST_ASSERT(false);
                }
                
                BOOST_ASSERT(result.get() != NULL);
                return result;
            }
        }
        
        const ObjectPtr ParseFromStream(std::wistream& in, std::wostream& error)
        {
            std::wstring str;
            std::getline(in, str);
            
            std::wstring::const_iterator begin = str.begin();
            ObjectPtr result = ParseFromString<std::wstring::const_iterator>(begin, str.end());
            if (begin != str.end()) {
                
                error << "Warning didn't consume: \"";
                std::copy<std::wstring::const_iterator>(
                        begin, 
                        str.end(), 
                        std::ostream_iterator<wchar_t, wchar_t>(error));
                error << "\"\n";
            }
            
            return result;
        }
    }
}
