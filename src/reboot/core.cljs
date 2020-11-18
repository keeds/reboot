(ns ^:figwheel-hooks reboot.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [reboot.events]
            [reboot.subs]
            [reboot.views]
            [reagent.dom]
            [re-frame.core :as rf :refer [dispatch dispatch-sync]]
            [devtools.core :as devtools]))

(devtools/install!)
(enable-console-print!)

;; (dispatch-sync [:initialize-db])

(defn render
  []
  (reagent.dom/render [reboot.views/xert-app]
                      (.getElementById js/document "app")))

(defn ^:after-load main
  []
  (render))
