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
