(ns clazz.shared_test
  (:require [clojure.test :refer :all]
            [clazz.base :refer :all]
            [clazz.shared :refer :all]))


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

(declare_class!
  "derived_from_two_parents"
  []
  ["class_with_two_parents"])


(deftest test_inheritance_metric_one_way
  (is (= (get_child_metric "base_class_1" "base_class_1") 0))
  (is (= (get_child_metric "base_class_1" "class_with_two_parents") 1))
  (is (= (get_child_metric "base_class_1" "derived_from_two_parents") 2))
  (is (= (get_child_metric "base_class_1" "base_class_2") -1))
  )

(deftest test_inheritance_metric
  (is (= (get_param_metric "base_class_1" "derived_from_two_parents") 2))
  (is (= (get_param_metric "derived_from_two_parents" "base_class_1") 2))
  )

(declare_generic! "method1" ["a", "b", "c"])

(declare_generic! "method2" ["a", "b", "c"])

(declare_generic! "method2" ["a"])

(deftest test_check_method_exists
  (is (= (generic_method_exists? "method1" ["a", "b", "c"]) true))
  (is (= (generic_method_exists? "method1" ["a", "b"]) false))
  (is (= (generic_method_exists? "method1" ["b"]) false))
  (is (= (generic_method_exists? "m" ["b"]) false))
  )

(declare_method! "method1" [["a", "base_class_1"], ["b", "class_with_two_parents"], ["c", "class_with_two_parents"]] (fn[] (println "first to call") (+ 1 1)))
(declare_method! "method1" [["a", "base_class_1"], ["b", "base_class_1"], ["c", "class_with_two_parents"]] (fn[] (println "second to call") (+ 1 1)))
(declare_method! "method1" [["a", "base_class_1"], ["b", "base_class_1"], ["c", "base_class_1"]] (fn[] (println "third to call") (+ 1 2)))
(declare_method! "method1" [["a", "base_class_2"], ["b", "base_class_1"], ["c", "base_class_1"]] (fn[] (println "don't call me") (+ 1 2)))

(deftest test_metric
  (is (= (get_metric [["a", "base_class_1"], ["b", "base_class_1"], ["c", "base_class_1"]] [["a", "base_class_1"], ["b", "base_class_1"], ["c", "base_class_1"]]) 0))
  (is (= (get_metric [["a", "base_class_1"], ["b", "base_class_1"], ["c", "class_with_two_parents"]] [["a", "base_class_1"], ["b", "base_class_1"], ["c", "base_class_1"]]) -1))
  (is (= (get_metric [["a", "base_class_1"], ["b", "base_class_1"], ["c", "base_class_1"]] [["a", "class_with_two_parents"], ["b", "base_class_1"], ["c", "base_class_1"]]) 1))
  (is (= (get_metric [["a", "base_class_1"], ["b", "base_class_1"], ["c", "base_class_1"]] [["a", "class_with_two_parents"], ["b", "class_with_two_parents"], ["c", "base_class_1"]]) 2))
  (is (= (get_metric [["a", "base_class_1"], ["b", "base_class_1"], ["c", "base_class_1"]] [["a", "class_with_two_parents"], ["b", "class_with_two_parents"], ["c", "class_with_two_parents"]]) 3))
  (is (= (get_metric [["a", "base_class_1"], ["b", "base_class_1"], ["c", "base_class_1"]] [["a", "derived_from_two_parents"], ["b", "base_class_1"], ["c", "base_class_1"]]) 2))
  )

(call "method1" [["a", "base_class_1"], ["b", "class_with_two_parents"], ["c", "derived_from_two_parents"]])
(deftest test_call
  )
