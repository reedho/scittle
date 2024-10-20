(ns example
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [emmy.viewer :as ev]
   [mathbox.core :as mathbox]
   [mathbox.primitives :as mb]
   [emmy.mathbox.plot :as plot]
   [clojure.walk :refer [postwalk]]
   [emmy.env :as e :refer [+ - * / zero? compare divide numerator denominator
                           infinite? abs ref partial =
                           ->infix ->TeX
                           square sin cos tanh asin D cube simplify
                           literal-function Lagrange-equations up]]
   [emmy.mafs :as mafs]))

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
   (list 'fn [] body)))


;; --- emmy viewer code demo ---

(def some-graph
  (mafs/mafs
   {:height 300}
   (mafs/cartesian)
   (mafs/vector [1 2] {:color :blue})
   (mafs/of-x {:y (fn [x] (square (sin (+ x 3)))) :color :blue})
   (mafs/text "face" {:color :green})))

(def another-graph
  (ev/with-let [!phase [0 0]]
    (let [shifted (ev/with-params {:atom !phase :params [0]}
                    (fn [shift]
                      (fn [x]
                        (((cube D) tanh) (- x shift)))))]
      (mafs/mafs
       {:height 400}
       (mafs/cartesian)
       (mafs/of-x shifted)
       (mafs/movable-point
        {:atom !phase :constrain "horizontal"})
       (mafs/inequality
        {:y {:<= shifted :> cos} :color :blue})))))

(defn my-component []
  [:div
   [->f another-graph]
   [:hr]
   [->f some-graph]])

(rdom/render [my-component] (.getElementById js/document "app"))
