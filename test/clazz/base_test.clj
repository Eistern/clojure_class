(ns clazz.base-test
  (:require [clojure.test :refer :all]
            [clazz.base :refer :all]))

(declare_class!
   "class"
   [
    [:attribute]
    ]
   )

(declare_class!
  "class_with_default_value"
  [
   [:attribute (default_value 1)]
   ]
  )

(deftest base_class_test
  (is (nil? (get_value (new_obj "class") :attribute)))
  (is (= (get_value (set_value! (new_obj "class") :attribute 1) :attribute) 1))
  )

(deftest default_value_test
  (is (= (get_value (new_obj "class_with_default_value") :attribute) 1))
  (is (= (get_value (set_value! (new_obj "class_with_default_value") :attribute 2) :attribute) 2))
  )

