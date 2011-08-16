SILI -- SICP Inspired Lisp Interpreter.

Written in C++ to prevent accidental cheating (inheriting features from host language like closures or garbace collection).

TODO:
* Integer ops
* Parse string primitives
* Create sensible nil
* Macros
* Mutual tail recursion


NOTES:
How to make references using 'set!':
    (define make-ref (lambda (value) (lambda (fn) (set! value (fn value)))))
    (define get-ref (lambda (ref) (ref (lambda (x) x))))
    (define set-ref (lambda (ref newvalue) (ref (lambda (x) newvalue))))

    (define r (make-ref 10))
    (get-ref r)
    (set-ref 20)
    (get-ref r)

