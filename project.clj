(defproject clojure_class "0.0.1-SNAPSHOT"
  :description "Lisp class model for Clojure language"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :repl-options {:init-ns clazz.base}
  :plugins [[lein-codox "0.10.8"]]
  :codox {
          :output-path "doc"
          :metadata {:doc/format :markdown}
          }
  )
