(ns clojure-class.core)

(let [class_state_map {}]
  (defn get_state_map []
    class_state_map)
  (defn print_state_map []
    (print class_state_map))
  )
