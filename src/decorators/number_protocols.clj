(ns decorators.number-protocols
  (:use clojure.template))

(do-template 
  [proto-name fn-name parser number-handler message]
  (do
    (defprotocol proto-name
      (fn-name [o] message))
    (extend-protocol proto-name
      java.lang.String
      (fn-name [s] (parser s))
      java.lang.Number
      (fn-name [n] (number-handler n))))
  ToInt to-int Integer/parseInt int "Converts the object to an integer"
  ToLong to-long Long/parseLong long "Converts the object to a long"
  ToFloat to-float Float/parseFloat float "Converts the object to a float"
  ToDouble to-double Double/parseDouble double "Converts the object to a double"
  ToShort to-short Short/parseShort short "Converts the object to a short"
  ToByte to-byte Byte/parseByte byte "Converts the object to a byte"
  )
