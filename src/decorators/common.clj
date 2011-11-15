(ns decorators.common
  [:use decorators.core])

(defn keep-meta 
  "This is a decorator to decorate decorators :)
  It preserves the input fns metadata."
  [d]
  (do
   (println (class d)) 
  (with-meta 
    (fn decorator [f] (with-meta (d f) (meta f)))
    (meta d))))

;(println (macroexpand '(decorate keep-meta keep-meta)))
;(decorate keep-meta keep-meta)

(defn basic-logger 
  "This is a very basic logging decorator."
  [f]
  (fn [& args] (apply println f args) (apply f args)))

;(println (meta (keep-meta basic-logger)))

(decorate basic-logger keep-meta)

(defn functional-logger [logging-fn] 
  (fn decorate [f]
    (fn [& args] (apply println (apply logging-fn args)) (apply f args))))

;The partial comp pattern is for fns that return a decorator.
(decorate functional-logger (partial comp keep-meta)) 

(defn validate [& validator-fns]
  (fn decorator [f]
    (fn [& args]
      (if (every? #(apply % args) validator-fns)
        (apply f args)
        (throw (Exception. "There was an error in validation"))))))

(decorate validate (partial comp keep-meta)) 

(defn coerce [& coerce-fns]
  (fn decorator [f]
    (fn [& args]
      (apply (apply comp f coerce-fns) args))))

(decorate coerce (partial comp keep-meta)) 

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; Dictionary specific decorators
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn validate-values [validator-dict]
  "Accepts a dictionary as input an returns a validator
  decorator as output.  Delegates the actual work to validate."
  (validate (map (fn [[k v]] (comp v k)) validator-dict)))

(defn coerce-values [coerce-dict]
  "Accepts a dictionary as input an returns a coercion
  decorator as output.  Delegates the actual work to coerce."
  (coerce (map (fn [[k v]] #(update-in % [k] v)) coerce-dict)))

(defn unpack [& values]
  "This decorator factory is designed to convert a fn 
  that takes positional arguements to a function that
  takes a dictionary argument. Only tries to match the arity
  of the values list."
  (let [unpacker (apply juxt values)]
  (fn decorator [f]
    (comp f unpacker))))

(decorate unpack (validate (comp pos? count)))
(decorate unpack (partial comp keep-meta)) 

(defn auto-unpack [f]
  "Tries to automatically uppack the fn into a dictonary.  This doesn't
  play nice with varargs."
  (let [args ((comp (partial map keyword) first :arglists meta) f)]
    ((apply unpack args) f))) 

;Reject multiple arity arglists
(decorate auto-unpack 
          (validate 
            (comp #{1} count :arglists meta)))

(defn multi-unpack [& value-seqs]
  "This is a version of unpack that attempts to play with
  multiple arities well.  Note that the value sequences must
  all have different lengths."
  (let [unpacker-dict (apply merge 
                             (map (fn [& args] {(sort args) (apply juxt args)}) value-seqs))]
  ))

;Whine if the same length is provided as an input more than once.
(decorate multi-unpack 
          (validate 
            (comp pos? count)
            (partial every? (comp pos? count))
            (fn [& args]
                 (= (count args) (count (set (map count args)))))))
