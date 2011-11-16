(ns decorators.common
  [:use decorators.core])

(defn functional-logger [logging-fn] 
  (fn decorate [f]
    (fn [& args] (apply logging-fn f args) (apply f args))))

(def ^{:doc "This is a very basic version of functional-logger, which prints the fn & arguments to std out."
       :arglists '([f])}basic-logger
  (functional-logger println))

(defn validate [& validator-fns]
  (fn decorator [f]
    (fn [& args]
      (if (every? #(apply % args) validator-fns)
        (apply f args)
        (throw (Exception. "There was an error in validation"))))))

(defn coerce [& coerce-fns]
  (fn decorator [f]
    (fn [& args]
      (apply (apply comp f coerce-fns) args))))

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
