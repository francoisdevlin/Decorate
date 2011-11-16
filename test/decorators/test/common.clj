(ns decorators.test.common
  (:require [clojure.string :as s])
  (:use [decorators.core])
  (:use [decorators.common])
  (:use [clojure.test]))

(deftest test-word-like
         (let [word-cap (word-like s/capitalize)]
           (is (word-like s/capitalize :test) :Test)
           (is (word-like s/capitalize 'test) 'Test)
           (is (word-like s/capitalize "test") "Test")
           (is (word-cap :test) :Test)
           (is (word-cap 'test) 'Test)
           (is (word-cap "test") "Test")
           ))

;And that, ladies and gentlemen, is how you test a logger :)
(deftest test-functional-logger
         (let [message-queue (agent [])
               log-fn (fn [f & args] (send message-queue conj (apply str args)))
               logger (functional-logger log-fn)]
           (is 3 (logger + 1 2))
           (is 0 (logger - 2 1))
           (is 2 (logger * 1 2))
           (is 3 (count @message-queue))
           (is ["12" "21" "12"] @message-queue)))

(deftest test-validate
         (is false "FAIL - TODO!!"))

(deftest test-validate-dict
         (is false "FAIL - TODO!!"))

(deftest test-args-to-int
         (is (args-to-int pass "1" 2 3.0) [1 2 3])
         (is (args-to-int + "1" 2 3.0) 6)
         )

(deftest test-coerce-dict
         (is false "FAIL - TODO!!"))
