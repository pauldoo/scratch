(defmacro unless [test body else]
    (list 'if (list 'not test) body else))

(defprotocol MyProtocol
    (shiz [_ c]))

(defrecord MyRecord [some-key] MyProtocol
    (shiz [_ c] {some-key c}))

