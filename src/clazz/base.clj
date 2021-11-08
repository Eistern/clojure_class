(ns clazz.base
  [:require [clazz.shared :as shared]])

(defn declare_class!
  "Add entry to the class map. class_name will be used as the key, and it will be used in the `new_obj` function"
  ([class_name vector_of_attributes list_of_superclasses]
  (shared/add_class! class_name vector_of_attributes list_of_superclasses))
  ([class_name vector_of_attributes] (declare_class! class_name vector_of_attributes ["T"]))
  )

(defn default_value
  "Factory method for the default value initialization"
  [value]
  (fn DefaultCreator [] value)
  )

(defn new_obj
  "Instance new object of given class. class_name must exist as a key of the class map. See `declare_class!`"
  [class_name]
  (->>
    ((shared/get_class_state_map) class_name)
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
  "Get value from the object by attribute name"
  [class_object attribute_name]
  (@class_object attribute_name)
  )

(defn set_value!
  "Set the value of the object attribute"
  [class_object attribute_name attribute_value]
  (do
    (swap! class_object #(assoc %1 attribute_name attribute_value))
    class_object
    )
  )