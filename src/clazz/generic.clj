(ns clazz.generic
  (:require [clojure.set :refer :all]
            [clazz.shared :refer :all])
  )


(let
  [
   generic_functions_map (atom {})
   declared_generic_impls_map (atom {})
   ]

  (defn get_generic_functions
    "Returns declared generic functions (name and parameters).
    Changes to this object will not affect generic context"
    []
    @generic_functions_map
    )

  (defn get_generic_impls_map
    "Returns declared generic functions implementations (name and parameters with types).
    Changes to this object will not affect generic context"
    []
    @declared_generic_impls_map
    )

  (declare search_class)

  (defn search_class_in_classes_list
    "Search class with a name the `target_name` among the list of classes `classes_list`.
    Returns `Long` distance in inheritors tree from the `target_name` class to the one of classes from `classes_list`,
    which is base class for `target_name`.
    Returns -1 if there is no base class for the `target_name` class among the `classes_list`"
    [classes_list target_name]
    (reduce
      (fn [acc x]
        (let [c_res (search_class x target_name)]
          (if (= c_res -1)
            acc
            c_res
            )
          )
        ) -1 classes_list)
    )

  (defn search_class
    "Returns `Long` distance in inheritors tree from the `target_name` class to the 'current_name' class.
      Returns -1 if 'current_name' is not the base class for the `target_name`"
    [current_name target_name]
    (let [children (get_class_inheritors current_name)]
      (if (= current_name target_name)
        0
        (if (or (= children `()) (= children nil))
          -1
          (let
            [result (search_class_in_classes_list children target_name)]
            (if (= result -1)
              -1
              (+ result 1)
              )
            )
          )
        )
      )
    )

  (defn declare_generic!
    "Declares a generic function with a `name` and a list of parameters in `params_list`.
   You can't declare two generic functions with the same name."
    [name params_list]
    (swap! generic_functions_map #(assoc %1 name params_list))
    (swap! declared_generic_impls_map #(assoc %1 name []))
    (println "Created generic function name " name)
    )

  (defn params_coincided?
    "Checks if two parameters names lists are the same."
    [passed_params actual_params]
    (let [result (count (clojure.set/intersection (set passed_params) (set actual_params)))]
      (and (= (count passed_params) result) (= (count actual_params) result))
      )
    )

  (defn generic_method_exists?
    "Checks if generic method with `name` and `params` is declared."
    [name params]
    (let [found_params (@generic_functions_map name)]
      (if (= found_params nil)
        false
        (params_coincided? params found_params)
        )
      )
    )

  (defn get_keys_from_list
    "Get list of parameters names from list pf parameter names with types."
    [params]
    (reduce (fn [acc x] (concat acc (list (first x)))) `() params)
    )

  (defn declare_method!
    "Declare generic function implementation with `name`, `params` parameters with types
    and `function` which will be executed with this implementation"
    [name additional_params params function]
    (let [param_names (get_keys_from_list params)]
      (if (generic_method_exists? name param_names)
        (swap! declared_generic_impls_map #(assoc %1 name (conj (@declared_generic_impls_map name) (list params function additional_params))))
        (throw (Exception. (str "Undeclared generic " name " with params: " param_names)))
        )
      (println "Created generic method impl with name" name)
      )
    )

  (defn get_metric
    "Get `Long` metric between two lists of parameters.
    `method_params` are parameters of one of a declared generic functions implementations.
    `passed_params` are parameters passed with generic function invocation."
    [method_params passed_params]
    (let [method_params_names (get_keys_from_list method_params)
          passed_params_names (get_keys_from_list passed_params)
          ]
      (if (params_coincided? method_params_names passed_params_names)
        (reduce (fn [acc x]
                  (if (= acc -1)
                    acc
                    (let [method_nth (second (nth method_params x))
                          passed_nth (second (nth passed_params x))
                          param_metric (search_class method_nth passed_nth)]
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

  (defn get_metric_for_params
    "Get list of pairs with metric between `params` and method implementation and method implementation itself
     for every implementation in `method_impls`"
    [params method_impls]
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

  (defn call
    "Call all suitable generic function implementations for the passed parameters `params` with types.
    The invocation order is determined by the fact how close 'params' types to the particular generic function implementation"
    [name params]
    (let [impl_distances (get_metric_for_params params (@declared_generic_impls_map name))]
      (let [sorted_methods (sort-by first impl_distances)
            call_next (atom (list true))]
        (doall (map (fn [x]
                      (do
                        (if (first @call_next)
                          ((second (second x)))
                          ()
                          )

                        (if (and (first @call_next) (some #(= :call_next %) (nth (second x) 2)))
                          ()
                          (do
                            (swap! call_next #(conj (drop 1 %1) false))
                            )
                          )
                        )
                      ) sorted_methods))
        )
      )
    )
  )