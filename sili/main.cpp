#include "Atom.h"
#include "AtomImp.h"
#include "Heap.h"
#include "ObjectImp.h"
#include "Pair.h"

#include <boost/intrusive_ptr.hpp>
#include <cstdlib>


using namespace std;

int main(int argc, char** argv) {
    try {
        {
            boost::intrusive_ptr<sili::Object> test = sili::Atom<int>::New(6);
            {
                boost::intrusive_ptr<sili::Object> pair1 = sili::Pair::New(test, test);
                boost::intrusive_ptr<sili::Object> pair2 = sili::Pair::New(test, test);

                pair1->As<sili::Pair>()->mFirst = pair2;
                pair2->As<sili::Pair>()->mFirst = pair1;
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
        
        return EXIT_SUCCESS;
    } catch (const std::exception& ex) {
        std::wcerr << typeid(ex).name() << L": " << ex.what() << L"\n";
    }
    
    return EXIT_FAILURE;
}

