(ns ^:figwheel-hooks reboot.core
  (:require
   [reboot.events]
   [reboot.subs]
   [reboot.views :as views]
   [reagent.dom]
   [re-frame.core :as rf :refer [dispatch dispatch-sync]]
   [devtools.core :as devtools]))

(devtools/install!)
(enable-console-print!)

;; (dispatch-sync [:initialize-db])

(defn render
  []
  (reagent.dom/render [views/xert-app]
                      (.getElementById js/document "app")))

(defn ^:after-load main
  []
  (rf/clear-subscription-cache!)
  (render))

(defonce start-up
  (do (main) true))
