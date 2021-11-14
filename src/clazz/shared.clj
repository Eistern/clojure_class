(ns clazz.shared
  (:require [clojure.set :refer :all])
  )

(let
  [
   class_state_map (atom {"T" []})
   class_inheritance_map (atom {"T" []})
   ]

  (defn get_class_state_map
    "Get image of class state map. Changes to this object will not affect shared context"
    []
    @class_state_map
    )

  (defn get_class_inheritors
    "Get class inheritors by the class name. Changes to this object will not affect shared context"
    [name]
    (@class_inheritance_map name)
    )

  (defn add_class!
    "Add new entry to the class state maps. class_name should be the same as the class_name that will be passed to the new_obj method"
    [class_name vector_of_attributes list_of_superclasses]
    (swap!
      class_inheritance_map
      #(reduce
         (fn InheritanceReduceHelper [coll, x]
           (assoc coll x (conj (coll x) class_name))
           )
         %1
         list_of_superclasses)
      )
    (swap! class_inheritance_map #(assoc %1 class_name []))
    (let
      [
       inherited_attributes
       (reduce
         #(concat %1 ((get_class_state_map) %2))
         vector_of_attributes
         list_of_superclasses
         )
       ]
      (swap! class_state_map #(assoc %1 class_name inherited_attributes))
      (println "For class " class_name " added " inherited_attributes)
      )
    )

  (defn print_class_state_map
    "Print current state of class map. Use only for debug"
    []
    (println @class_state_map)
    )

  (defn print_class_inheritance_map
    "Print current state of class inheritance map. Use only for debug"
    []
    (println @class_inheritance_map)
    )
  )