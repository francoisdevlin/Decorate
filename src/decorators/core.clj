(ns decorators.core)

(defmacro decorate 
  "This macro decorates an object to add new behavior.  The funtion
  f is expected to return the same type of object that it takes as
  an input.  Multiple decorators are chained together simply using
  functional composition (comp). 

  This macro keeps the metadata associated with the original symbol."
  [sym f]
  `(let [m# (meta (var ~sym))]
     (def ~sym (~f ~sym))
     (reset-meta! (var ~sym) m#) 
     (var ~sym)))

(defn apply-decorator
  "This function takes a decorator, and applies it to the trailing
  arguments.  This is useful when you want to seperate use of a decorator
  from the function its used with." 
  [decorator f & args] 
  (apply (decorator f) args))

(defn to-hof
  "This function turns a decorator to a higer order function.  This
  allows you to use it on a per-form basis."
  [decorator]
  (partial apply-decorator decorator))

(defn to-decorator
  "This function turns a higher order fn to a decorator.  Not quite
  sure what the use case is, but it can effectively undo to-hof" 
  [hof]
  (fn decorator [f] 
    (fn wrap [& args] (apply hof f args))))

(defn dual-decorator
  "This function converts a decorator to its dual form.  In this form
it behaves as a decorator if only the a function is passed (arity 1),
and a higher order function if more arguments are passed."
  [decorator]
  (fn dual
    ([f] (decorator f))
    ([f a] ((decorator f) a))
    ([f a b] ((decorator f) a b))
    ([f a b c] ((decorator f) a b c))
    ([f a b c & args] (apply (decorator f) a b c args))))

(defn dual-hof
  "This behaves the same as dual-decorator, but it expects a hof as the input."
  [hof]
  (fn dual
    ([f] (partial hof f))
    ([f a] (hof f a))
    ([f a b] (hof f a b))
    ([f a b c] (hof f a b c))
    ([f a b c & args] (apply hof f a b c args))))
