(ns clazz.shared)

(let [class_state_map (atom {})]

  (defn get_class_state_map
    "Get image of class state map. Changes to this object will not affect shared context"
    []
    @class_state_map
    )

  (defn add_class!
    "Add new entry to the class state map. class_name should be the same as the class_name that will be passed to the new_obj method"
    [class_name vector_of_attributes & list_of_superclasses]
    (swap! class_state_map #(assoc %1 class_name vector_of_attributes))
    )

  (defn print_class_state_map
    "Print current state of class map. Use only for debug"
    []
    (println @class_state_map)
    )

  )
