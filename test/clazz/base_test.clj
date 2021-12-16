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

(declare_class!
  "class_with_inheritance"
  []
  ["class_with_default_value"]
  )

(declare_class!
  "base_class_1"
  [
   [:Attribute1 (default_value 1)]
   ]
  )

(declare_class!
  "base_class_2"
  [
   [:Attribute2 (default_value 2)]
   ]
  )

(declare_class!
  "class_with_two_parents"
  []
  ["base_class_1", "base_class_2"])

(deftest base_class_test
  (is (nil? (get_value (new_obj "class") :attribute)))
  (is (= (get_value (set_value! (new_obj "class") :attribute 1) :attribute) 1))
  )

(deftest classname_test
  (is (= (get_classname (new_obj "class")) "class")))

(deftest default_value_test
  (is (= (get_value (new_obj "class_with_default_value") :attribute) 1))
  (is (= (get_value (set_value! (new_obj "class_with_default_value") :attribute 2) :attribute) 2))
  )

(deftest inheritance_test
  (is (= (get_value (new_obj "class_with_inheritance") :attribute) 1))
  )

(deftest multiple_inheritance_test
  (is (= (get_value (new_obj "class_with_two_parents") :Attribute1) 1))
  (is (= (get_value (new_obj "class_with_two_parents") :Attribute2) 2))
  )
