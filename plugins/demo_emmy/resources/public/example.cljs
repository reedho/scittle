(ns example
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [emmy.viewer :as ev]
   [clojure.walk :refer [postwalk]]))

(def viewer-name :emmy.scittle/reagent)

(def xform-key
  "Currently transforms are stored under this key for compatibility with the
  Emmy-Viewers Clerk code."
  :nextjournal.clerk/viewer)

(defn ^:no-doc strip-meta
  "Given an unevaluated Reagent body, returns an identical form with all metadata
  stripped off."
  [form]
  (postwalk
   (fn [x]
     (if (meta x)
       (vary-meta x dissoc xform-key)
       x))
   form))

(defn ->f
  "Given a quoted Reagent body (which might use functions like
  `reagent.core/with-let` or third-party components, not just Hiccup syntax),
  returns a no-argument-function component that renders an `eval`-ed version of
  `body`."
  [body]
  (eval
   (list 'fn [] (strip-meta body))))


;; --- emmy viewer code demo ---

(require
 '[emmy.env :as e]
 '[emmy.mafs :as mafs]
 )

(def some-graph
  (mafs/mafs
   {:height 300}
   (mafs/cartesian)
   (mafs/vector [1 2] {:color :blue})
   (mafs/of-x {:y (fn [x] (e/square (e/sin (e/+ x 3)))) :color :blue})
   (mafs/text "face" {:color :green})))

(defn my-component []
  [:div
   [(->f some-graph)]])

(rdom/render [my-component] (.getElementById js/document "app"))
