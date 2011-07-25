
#include "Heap.h"
#include "Interpreter.h"
#include "ObjectImp.h"
#include "Pair.h"
#include "Primitive.h"
#include "PrimitiveImp.h"
#include "Symbol.h"

#include <boost/intrusive_ptr.hpp>
#include <cstdlib>


using namespace sili;

int main(int argc, char** argv) {
    try {
        {
            {
                boost::intrusive_ptr<sili::Object> test = sili::Primitive<int>::New(6);
                {
                    boost::intrusive_ptr<sili::Object> pair1 = sili::Pair::New(test, test);
                    boost::intrusive_ptr<sili::Object> pair2 = sili::Pair::New(test, test);

                    pair1->AsA<sili::Pair>()->mFirst = pair2;
                    pair2->AsA<sili::Pair>()->mFirst = pair1;
                    std::cout << "A\n";
                    sili::Heap::Instance()->Sweep();
                    std::cout << "B\n";
                }
                std::cout << "C\n";
                sili::Heap::Instance()->Sweep();
                std::cout << "D\n";
            }
            std::cout << "E\n";
            sili::Heap::Shutdown();
            std::cout << "F\n";
        }
        
        {
            //(((lambda (x) (lambda () x)) 30))
        
            ObjectPtr inner = Pair::New(Symbol::New(Interpreter::LAMBDA), Pair::New(NULL, Pair::New(Symbol::New(L"x"), NULL)));
            ObjectPtr outer = Pair::New(Symbol::New(Interpreter::LAMBDA), Pair::New( Pair::New(Symbol::New(L"x"), NULL), Pair::New(inner, NULL)));
            ObjectPtr expression = Pair::New(Pair::New(outer, Pair::New(Primitive<int>::New(30), NULL)), NULL);

            std::wcout << (*expression) << L"\n";
            
            ObjectPtr result = Interpreter::Eval(expression, NULL);
            
            std::wcout << (*result) << L"\n";
        }
        
        return EXIT_SUCCESS;
    } catch (const std::exception& ex) {
        std::wcerr << typeid(ex).name() << L": " << ex.what() << L"\n";
    }
    
    return EXIT_FAILURE;
}

