(ns clazz.base
  [:require [clazz.shared :as shared]])

(defn declare_class
  ""
  [class_name vector_of_attributes & list_of_superclasses]
  (shared/add_class class_name vector_of_attributes list_of_superclasses)
  )

(defn default_value
  ""
  [value]
  (fn DefaultGetter [] value)
  )

(defn new_obj
  ""
  [class_name]
  (->>
    ((shared/get_state_map) class_name)
    (vec)
    (reduce
      #(if
         (> (count %2) 1)
         (assoc %1 (nth %2 0) ((nth %2 1)))
         %1
         ) {}
      )
    (atom)
    )
  )

(defn get_value
  ""
  [class_object attribute_name]
  (@class_object attribute_name)
  )

(defn set_value
  ""
  [class_object attribute_name attribute_value]
  (do
    (swap! class_object #(assoc %1 attribute_name attribute_value))
    class_object
    )
  )