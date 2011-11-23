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
  (let [validate-fn (validate (partial every? pos?))]
    (is (validate-fn identity [1 2 3]) [1 2 3])
    (is (thrown? Exception (validate-fn identity [1 2 0])))
    ))

(deftest test-forbid
  (let [forbid-fn (forbid (partial some neg?))]
    (is (forbid-fn identity [1 2 3]) [1 2 3])
    (is (thrown? Exception (forbid-fn identity [-1 2 3])))
    ))

(deftest test-validate-dict
  (let [validate-fn (validate-values {:x pos?})]
    (is (validate-fn identity {:x 1}) {:x 1})
    (is (thrown? Exception (validate-fn identity {:x 0})))))

(deftest test-forbid-dict
  (let [forbid-fn (forbid-values {:x neg?})]
    (is (forbid-fn identity {:x 1}) {:x 1})
    (is (thrown? Exception (forbid-fn identity {:x -1})))))

(deftest test-args-to-int
         (is (args-to-int pass "1" 2 3.0) [1 2 3])
         (is (args-to-int + "1" 2 3.0) 6))

(deftest test-coerce-dict
  (let [coerce-fn (coerce-values {:x inc :y dec})]
    (is {:x 11 :y 1} (coerce-fn identity {:x 10 :y 2}))
    (is (thrown? java.lang.NullPointerException
                 (coerce-fn identity {:x 10})))))
