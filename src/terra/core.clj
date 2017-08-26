(ns terra.core
  (:require [cheshire.core :as json]
            [clojure.java.io :as io])
  (:import java.io.File))

(defmacro defterra
  "Used to specify which var(s) will be generated as terraform configurations.
  This macro simply wraps a `def` with a meta `^:terraform`. If you prefer,
  that a perfectly acceptable way to mark vars down for terraform generation."
  [name# form]
  `(def ~(vary-meta name# #(merge {:terraform true} %)) ~form))


(declare ^:private sanitize-params)

(defn ^:private function-call
  [fform args]
  (str fform "(" (apply str (interpose ", " (sanitize-params args))) ")"))

(defn ^:private sanitize-param
  "Sanitizes a single parameter."
  [param]
  (cond
    (number? param) (str param)
    (keyword? param) (name param)
    (list? param) (function-call (first param) (rest param))
    (string? param) (pr-str param)
    :else param))

(defn ^:private sanitize-params
  "Sanitizes a collection of parameters."
  [args]
  (map #(sanitize-param %) args))

(defmulti $form
  "Dispatcher for each form in the `$` macro. It receives the form and any args sent to it.
  It uses the `class` of the form on the dispatching function."
  (fn [form args] (class form)))

(defmethod $form clojure.lang.Symbol
  [form args]
  (cond
    (nil? args) (str "${" form "}")
    (= 'get form) (str "${" (first args) "[" (sanitize-param (second args)) "]}")
    :default (str "${" (function-call form args) ")}")))

(defmethod $form clojure.lang.Keyword
  [form [name#]]
  (str "${" name# "[" (name form) "]}"))

(defmethod $form clojure.lang.PersistentList
  [form _]
  ($form (first form) (rest form)))


(defmacro $
  "This macro simulates the `${}` of Terraform. It's a convenience macro to make the code
  feel more like clojure and avoid the string interpolations from HCL."
  [form]
  ($form form nil))

(defn generate
  "Walks trhough all the namespaces in memory, finds the vars marked with `^:terraform and
  generates Terraform-compatible configuration files at `terraform/`."
  []
  (println "Terra: generating...")
  (doseq [ns# (all-ns)]
    (doseq [[sym var] (ns-publics ns#)
            :when (:terraform (meta var))]
      (let [file-name (str "terraform"
                           File/separator
                           (ns-name ns#)
                           File/separator
                           sym
                           ".tf.json")
            f (io/file file-name)]
        (println "Terra:" file-name)
        (.mkdirs (.getParentFile f))
        (json/generate-stream (var-get var)
                              (io/writer f)))))
  (println "Terra: done!"))
