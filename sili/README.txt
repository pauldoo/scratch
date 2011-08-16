SILI -- SICP Inspired Lisp Interpreter.

Written in C++ to prevent accidental cheating (inheriting features from host language like closures or garbace collection).

TODO:
* Integer operations
* Parse string primitives
* Create sensible nil
* Mutual tail recursion
* List operations
* Symbol generation for sane macros
* Sensible demo script

NOTES:
How to make references using 'set!':
    (define make-ref (lambda (value) (lambda (fn) (set! value (fn value)))))
    (define get-ref (lambda (ref) (ref (lambda (x) x))))
    (define set-ref (lambda (ref newvalue) (ref (lambda (x) newvalue))))

    (define r (make-ref 10))
    (get-ref r)
    (set-ref 20)
    (get-ref r)

Using a macro to do nothing:
    (define v 10)
    ((lambda (x) 0) (set! v 20))
    v
    // v is now 20
    ((macro (x) 0) (set! v 30))
    v
    // v is still 20

