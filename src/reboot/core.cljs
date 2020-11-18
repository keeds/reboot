(ns reboot.core
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

(defn ^:export ^:after-load main
  []
  (prn "main")
  (render))

;; (defn ^:export login [username password]
;;   (go (let [res (<! (http/post "https://www.xertonline.com/oauth/token"
;;                                {;; :with-credentials? true
;;                                 :basic-auth {:username "xert_public" :password "xert_public"}
;;                                 ;; :with-credentials? false
;;                                 :form-params {:grant_type "password"
;;                                               :username username
;;                                               :password password}}))]
;;         (prn (:status res))
;;         (prn (:body res)))))

