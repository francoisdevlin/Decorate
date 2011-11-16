(ns decorators.test.common
  (:require [clojure.string :as s])
  (:use [decorators.core])
  (:use [decorators.common])
  (:use [clojure.test]))

(deftest test-word-like
         (let [hof (to-hof word-like)]
           (is (hof s/capitalize :test) :Test)
           (is (hof s/capitalize 'test) 'Test)
           (is (hof s/capitalize "test") "Test")
           ))

;And that, ladies and gentlemen, is how you test a logger :)
(deftest test-functional-logger
         (let [message-queue (agent [])
               log-fn (fn [f & args] (send message-queue conj (apply str args)))
               logging-hof (to-hof (functional-logger log-fn))]
           (is 3 (logging-hof + 1 2))
           (is 0 (logging-hof - 2 1))
           (is 2 (logging-hof * 1 2))
           (is 3 (count @message-queue))
           (is ["12" "21" "12"] @message-queue)))
