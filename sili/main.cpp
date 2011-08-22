#include "Heap.h"
#include "Pair.h"
#include "Repl.h"

#include <cstdlib>
#include <fstream>

using namespace sili;

int main(int argc, char** argv) {
    try {
        {
            const ObjectPtr environment = Pair::New(ObjectPtr(), ObjectPtr());

            {
                std::wifstream boot("boot.sili");
                Repl::Repl(boot, std::wcout, std::wcerr, environment);
            }

            Repl::Repl(std::wcin, std::wcout, std::wcerr, environment);
        }

        sili::Heap::Shutdown();
        
        return EXIT_SUCCESS;
    } catch (const std::exception& ex) {
        std::wcerr << typeid(ex).name() << L": " << ex.what() << L"\n";
    }
    
    return EXIT_FAILURE;
}

