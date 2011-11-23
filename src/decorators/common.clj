(ns decorators.common
  [:use decorators.core 
   decorators.number-protocols
   clojure.template]
  )

(defn pass 
  "This is a special function that passes the arguments through, in
  order to help with decorator testing."
  [& args] args)

(defn post-comp [& fns]
  (fn decorate [f]
    (fn wrap [& args]
      ((apply comp fns) (apply f args))))) 

(defn functional-logger [logging-fn] 
  (fn decorate [f]
    (fn [& args] (apply logging-fn f args) (apply f args))))

(decorate functional-logger (post-comp dual-decorator))

(def ^{:doc "This is a very basic version of functional-logger, which prints the fn & arguments to std out."
       :arglists '([f])}basic-logger
  (functional-logger println))

(defn validate [& validator-fns]
  "This function creates a decorator that forces every condition to be
  true to allow function execution.  This is a similar version of "
  (fn decorator [f]
    (fn
      ([a]
         (if (every? #(% a) validator-fns)
           (f a)
           (throw (Exception. "There was an error in validation"))))
      ([a b]
         (if (every? #(% a b) validator-fns)
           (f a b)
           (throw (Exception. "There was an error in validation"))))
      ([a b c]
         (if (every? #(% a b c) validator-fns)
           (f a b c)
           (throw (Exception. "There was an error in validation"))))
      ([a b c & args]
         (if (every? #(apply % a b c args) validator-fns)
           (apply f a b c args)
           (throw (Exception. "There was an error in validation"))))
      )))

(decorate validate (post-comp dual-decorator))

(defn forbid
  [& forbidden-fns]
  (apply validate (map complement forbidden-fns)))

(defn coerce [& coerce-fns]
  "Coerce funtions take a vector of args in, a vector of args out.  This
  ensures that the resulting decorated fn will be the same arity."
  (let [comp-coerce (apply comp coerce-fns)]
    (fn decorator [f]
      (fn [& args]
        (apply f (comp-coerce args))))))

(decorate coerce (post-comp dual-decorator))

(do-template 
  [fn-name coerce-fn]
  (def fn-name (coerce #(map coerce-fn %)))
  args-to-int    to-int
  args-to-long   to-long
  args-to-double to-double
  args-to-float  to-float
  args-to-short  to-short
  args-to-byte   to-byte)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; Dictionary specific decorators
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn validate-values [validator-dict]
  "Accepts a dictionary as input an returns a validator
  decorator as output.  Delegates the actual work to validate."
  (apply validate (map (fn [[k v]] (comp v k)) validator-dict)))

(defn forbid-values [forbidden-dict]
  "Accepts a dictionary as input an returns a validator
  decorator as output.  Delegates the actual work to validate."
  (apply forbid (map (fn [[k v]] (comp v k)) forbidden-dict)))

(defn coerce-values [coerce-dict]
  "Accepts a dictionary as input an returns a coercion
  decorator as output.  Delegates the actual work to coerce.

  Note that this will crash and burn if the key is not present."
  (apply coerce (map (fn coerce-gen [[k v]]
                       (fn corce-fn [[input-dict & args]]
                         [(update-in input-dict [k] v)])
                       ) coerce-dict)))

(defn unpack [& values]
  "This decorator factory is designed to convert a fn 
  that takes positional arguements to a function that
  takes a dictionary argument. Only tries to match the arity
  of the values list."
  (let [unpacker (apply juxt values)]
  (fn decorator [f]
    (comp f unpacker))))

(decorate unpack (post-comp dual-decorator))
(decorate unpack (validate (comp pos? count)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; Word-like decorator
;
; This is a decorator designed to play nice with word-like
; objects, such as string, keywords, and symbols. 
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol Wordish
  (plain-string [s] "This gets a plain string representation of an object")
  (from-string [object s] "This returns an object from a string s"))

(extend-protocol Wordish
  java.lang.String
  (plain-string [s] s)
  (from-string [object s] s)
  clojure.lang.Keyword
  (plain-string [s] (name s))
  (from-string [object s] (keyword s))
  clojure.lang.Symbol
  (plain-string [s] (name s))
  (from-string [object s] (symbol s)))

(defn word-like
  "This decorator converts a string function into a string/keyword/symbol function.
  It assumes that the first argument is operated on, and the remaining arguments
  control how the function behaves. This eliminates the need for writing a 
  clojure.keyword or clojure.symbol lib."
  [f]
  (fn wrap [word & args]
    (from-string word (apply f (plain-string word) args))))

(decorate word-like dual-decorator)
