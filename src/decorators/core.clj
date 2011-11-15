(ns decorators.core)

(defmacro defmeta [sym init]
  `(let [m# (meta ~init)] 
     (def ~sym ~init) 
     (reset-meta! (var ~sym) m#) 
     (var ~sym))) 

(defmacro decorate [sym f] `(defmeta ~sym (~f ~sym)))

(defn apply-decorator 
  ([decorator] (partial apply-decorator decorator))
  ([decorator f & args] (apply (decorator f) args)))  
