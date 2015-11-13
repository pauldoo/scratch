#include "Assert.h"

#include <exception>
#include <sstream>

namespace timetrace {
    class Exception : public std::exception
    {
    public:
        Exception(const std::string& reason);

        ~Exception() throw ();

        /// std::exception
        virtual const char* what () const throw ();

    private:
        const std::string fReason;
    };

    Exception::Exception(const std::string& reason) :
        fReason(reason)
    {
    }

    Exception::~Exception() throw ()
    {
    }

    const char* Exception::what () const throw ()
    {
        return fReason.c_str();
    }

    void Assert(
                const bool v,
                const std::string& expression,
                const std::string& function,
                const std::string& file,
                const int lineNumber)
    {
        if (v == false) {
            std::ostringstream message;
            message << "Assertion failure: \"" << expression << "\" in " << function << " (" << file << ", line " << lineNumber << ")";
            throw Exception(message.str());
        }
    }

}

