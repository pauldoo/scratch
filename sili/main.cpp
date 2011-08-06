#include "Heap.h"
#include "Interpreter.h"
#include "ObjectFwd.h"
#include "Parser.h"

#include <cstdlib>

using namespace sili;

int main(int argc, char** argv) {
    try {
        while (true) {
            sili::Heap::Instance()->Sweep();
            std::wcout << L">: ";
            const ObjectPtr expression = Parser::ParseFromStream(std::wcin);
            std::wcout << *(expression.get()) << "\n";
            if (expression.get() == NULL) {
                break;
            } else {
                ObjectPtr result;
                try {
                    result = Interpreter::Eval(expression, NULL);
                } catch (const std::exception& ex) {
                    std::wcerr << typeid(ex).name() << L": " << ex.what() << L"\n";
                }                    
                std::wcout << "<: " << *(result.get()) << "\n";
            }
        }

        sili::Heap::Shutdown();
        
        return EXIT_SUCCESS;
    } catch (const std::exception& ex) {
        std::wcerr << typeid(ex).name() << L": " << ex.what() << L"\n";
    }
    
    return EXIT_FAILURE;
}

