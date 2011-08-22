#include "Repl.h"

#include "Heap.h"
#include "Interpreter.h"
#include "Parser.h"

namespace sili {
    namespace Repl {
        void Repl(
                std::wistream& in, 
                std::wostream& out, 
                std::wostream& error, 
                const ObjectPtr& environment)
        {
            while (true) {
                sili::Heap::Instance()->Sweep();
                out << L">: ";
                const ObjectPtr expression = Parser::ParseFromStream(in, error);
                out << *(expression.get()) << "\n";
                if (expression.get() == NULL) {
                    break;
                } else {
                    ObjectPtr result;
                    try {
                        result = Interpreter::Eval(expression, environment);
                    } catch (const std::exception& ex) {
                        error << typeid(ex).name() << L": " << ex.what() << L"\n";
                    }                    
                    out << "<: " << *(result.get()) << "\n";
                }
            }            
        }
    }
}