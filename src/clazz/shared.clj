(ns clazz.shared
  (:require [clojure.set :refer :all])
  )

(let
  [
   class_state_map (atom {"T" []})
   class_inheritance_map (atom {"T" []})
   generic_functions_map (atom {})
   declared_methods_map (atom {})
   ]

  (defn get_class_state_map
    "Get image of class state map. Changes to this object will not affect shared context"
    []
    @class_state_map
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

  (declare get_child_metric)

  (defn find_last_not_nil_metric [children target_name]
    (reduce
      (fn [acc x]
        (let [c_res (get_child_metric x target_name)]
          (if (= c_res -1)
            acc
            c_res
            )
          )
        ) -1 children)
    )

  (defn get_child_metric [current_name target_name]
    (let [children (@class_inheritance_map current_name)]
      (if (= current_name target_name)
        0
        (if (or (= children `()) (= children nil))
          -1
          (let
            [result (find_last_not_nil_metric children target_name)]
            (if (= result -1)
              -1
              (+ result 1)
              )
            )
          )
        )
      )
    )

  (defn get_param_metric [first_name second_name]
    (let [first_name_child_metric (get_child_metric first_name second_name)]
      (if (= first_name_child_metric -1)
        (get_child_metric second_name first_name)
        first_name_child_metric
        )
      )
    )

  (defn declare_generic! [name params_list]
    (swap! generic_functions_map #(assoc %1 name params_list))
    (swap! declared_methods_map #(assoc %1 name []))
    (println "Created generic function name " name)
    )

  (defn get_generic_functions
    []
    @generic_functions_map
    )

  (defn get_declared_methods_map
    []
    @declared_methods_map
    )

  (defn params_coincided? [passed_params actual_params]
    (let [result (count (clojure.set/intersection (set passed_params) (set actual_params)))]
      (and (= (count passed_params) result) (= (count actual_params) result))
      )
    )

  (defn generic_method_exists? [name params]
    (let [found_params (@generic_functions_map name)]
      (if (= found_params nil)
        false
        (params_coincided? params found_params)
        )
      )
    )

  (defn get_keys_from_list [params]
    (reduce (fn [acc x] (concat acc (list (first x)))) `() params)
    )

  (defn declare_method! [name params function]
    (let [param_names (get_keys_from_list params)]
      (if (generic_method_exists? name param_names)
        (swap! declared_methods_map #(assoc %1 name (conj (@declared_methods_map name) (list params function))))
        (throw (Exception. (str "Undeclared generic " name " with params: " param_names)))
        )
      (println "Created generic method impl with name" name)
      )
    )

  (defn get_metric [method_params passed_params]
    (let [method_params_names (get_keys_from_list method_params)
          passed_params_names (get_keys_from_list passed_params)
          ]
      (if (params_coincided? method_params_names passed_params_names)
        (reduce (fn [acc x]
                  (if (= acc -1)
                    acc
                    (let [method_nth (second (nth method_params x))
                          passed_nth (second (nth passed_params x))
                          param_metric (get_child_metric method_nth passed_nth)]
                      (if (= param_metric -1)
                        -1
                        (+ acc param_metric)
                        )
                      )
                    )
                  )
                0
                (range 0 (count method_params) 1)
                )
        -1
        )
      )
    )

  (defn- get_method_name [method_impl]
    (first method_impl)
    )

  (defn- get_method_params [method_impl]
    (first (second method_impl))
    )

  (defn- get_method_function [method_impl]
    (second (second method_impl))
    )

  (defn get_metric_for_params [params method_impls]
    (reduce (fn [acc x]
              (let [p_metric (get_metric (first x) params)]
                (if (= p_metric -1)
                  acc
                  (conj acc (list p_metric x)))
                )
              )
            []
            method_impls
      )
    )
  (defn call [name params]
    (let [impl_distances (get_metric_for_params params (@declared_methods_map name))]
      (let [sorted_methods (sort-by first impl_distances)]
        (doall (map (fn [x] ((second (second x)))) sorted_methods))
        )
      )
    )
  )