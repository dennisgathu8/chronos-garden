(ns chronos.ui
  "Reactive frontend for Chronos Garden."
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [chronos.growth :as growth]
            [chronos.security :as security]
            [goog.dom :as dom]))

;; --- DB Structure ---
;; {:timeline {:history [] :current-index 0}
;;  :seed "default"}

;; --- Events ---

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:timeline {:history [{:branches [{:x 300 :y 500 :angle (* -0.5 Math/PI) :length 50 :depth 0}]}]
               :current-index 0}
    :seed "chronos"}))

(rf/reg-event-db
 :grow
 (fn [db _]
   (let [timeline (:timeline db)
         current-state (get-in timeline [:history (:current-index timeline)])
         new-state (growth/grow-step current-state (hash (:seed db)))
         new-timeline (update timeline :history conj new-state)
         new-index (dec (count (:history new-timeline)))]
     (assoc db :timeline (assoc new-timeline :current-index new-index)))))

(rf/reg-event-db
 :set-time
 (fn [db [_ index]]
   (assoc-in db [:timeline :current-index] index)))

;; --- Subscriptions ---

(rf/reg-sub
 :current-state
 (fn [db _]
   (let [timeline (:timeline db)]
     (get-in timeline [:history (:current-index timeline)]))))

(rf/reg-sub
 :history-count
 (fn [db _]
   (count (get-in db [:timeline :history]))))

;; --- Rendering ---

(defn render-branch [ctx branch]
  (let [{:keys [x y angle length]} branch]
    (.beginPath ctx)
    (.moveTo ctx x y)
    (.lineTo ctx (+ x (* length (Math/cos angle)))
                 (+ y (* length (Math/sin angle))))
    (set! (.-strokeStyle ctx) "#4ade80")
    (set! (.-lineWidth ctx) 2)
    (.stroke ctx)))

(defn garden-view []
  (let [canvas-ref (r/atom nil)]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (let [canvas (dom/getElement "garden-canvas")
              ctx (.getContext canvas "2d")
              state @(rf/subscribe [:current-state])]
          (set! (.-width canvas) 600)
          (set! (.-height canvas) 600)
          (doseq [b (:branches state)]
            (render-branch ctx b))))
      
      :component-did-update
      (fn [this]
        (let [canvas (dom/getElement "garden-canvas")
              ctx (.getContext canvas "2d")
              state @(rf/subscribe [:current-state])]
          (.clearRect ctx 0 0 600 600)
          (doseq [b (:branches state)]
            (render-branch ctx b))))
      
      :reagent-render
      (fn []
        [:div.flex.flex-col.items-center.p-8
         [:h1.text-3xl.font-bold.mb-4 "Chronos Garden"]
         [:canvas#garden-canvas.bg-slate-900.rounded-lg.shadow-2xl]
         [:div.mt-4.flex.gap-4
          [:button.px-4.py-2.bg-green-600.text-white.rounded {:on-click #(rf/dispatch [:grow])} "Grow"]
          [:input {:type "range" 
                   :min 0 
                   :max (dec @(rf/subscribe [:history-count])) 
                   :on-change #(rf/dispatch [:set-time (js/parseInt (.. % -target -value))])}]]])})))

(defn ^:export init []
  (rf/dispatch-sync [:initialize])
  (r/render [garden-view] (dom/getElement "app")))
