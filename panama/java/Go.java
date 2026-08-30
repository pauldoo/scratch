import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

public class Go {


    public static void main() throws Throwable {
        final Linker linker = Linker.nativeLinker();
        try (Arena arena = Arena.ofShared()) {
            {
                SymbolLookup sl = SymbolLookup.libraryLookup("./build/libtest.so", arena);
                MemorySegment funcAddress = sl.findOrThrow("myfunc");
                FunctionDescriptor funcSig = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT);
                MethodHandle handle = linker.downcallHandle(funcAddress, funcSig);
                handle.invokeExact(42);
            }

            {
                SymbolLookup sl = SymbolLookup.libraryLookup("./build/liblogic.so", arena);
                MemorySegment funcAddress = sl.findOrThrow("hello_from_rust");
                FunctionDescriptor funcSig = FunctionDescriptor.ofVoid(
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT
                );
                MethodHandle handle = linker.downcallHandle(funcAddress, funcSig);

                MemorySegment data = arena.allocate(ptsLayout);
                for (int i = 0; i < LENGTH; i++) {
                    xHandle.set(data, 0L, (long)i, i * 10);
                    yHandle.set(data, 0L, (long)i, i * 100);
                }
                handle.invokeExact(data, LENGTH);
            }

            System.out.println("HELLO");
        }
    }

    static final int LENGTH = 10;

    static final SequenceLayout ptsLayout
        = MemoryLayout.sequenceLayout(LENGTH,
            MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("x"),
                MemoryLayout.paddingLayout(4),
                ValueLayout.JAVA_LONG.withName("y")));
                        
    static final VarHandle xHandle
        = ptsLayout.varHandle(PathElement.sequenceElement(),
            PathElement.groupElement("x"));
            
    static final VarHandle yHandle
        = ptsLayout.varHandle(PathElement.sequenceElement(),
            PathElement.groupElement("y"));    
}
