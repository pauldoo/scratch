#pragma once

#include <boost/intrusive_ptr.hpp>
#include <iosfwd>


namespace sili {
    class Object;

    typedef boost::intrusive_ptr<Object> ObjectPtr;
}

void intrusive_ptr_add_ref(const sili::Object* const);
void intrusive_ptr_release(const sili::Object* const);

std::wostream& operator << (std::wostream&, const sili::Object&);
