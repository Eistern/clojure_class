(ns clazz.shared)

(let [class_state_map (atom {})]
  (defn get_state_map []
    @class_state_map)
  (defn add_class [class_name vector_of_attributes & list_of_superclasses]
    (swap! class_state_map #(assoc %1 class_name vector_of_attributes))
    )
  (defn print_state_map []
    (print @class_state_map))
  )
