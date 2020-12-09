(ns reboot.subs
  (:require
   [clojure.set :as s]
   [clojure.core.reducers :as r]
   [re-frame.core :as rf]))

(rf/reg-sub
 :authentication
 (fn [db _]
   (:authentication db)))

(rf/reg-sub
 :username
 (fn [db _]
   (:username db)))

(rf/reg-sub
 :password
 (fn [db _]
   (:password db)))

(rf/reg-sub
 :loading?
 (fn [db _]
   (:loading? db)))

(rf/reg-sub
 :activities
 (fn [db _]
   (-> db :activities)))

(rf/reg-sub
 :workouts
 (fn [db _]
   (-> db :user-workouts :workouts)))

(rf/reg-sub
 :workout-sort
 (fn [db _]
   (:workout-sort db)))

(rf/reg-sub
 :activity-sort
 (fn [db _]
   (:activity-sort db)))

(rf/reg-sub
 :activity-details
 (fn [db _]
   ;; (let [as (:activities db)
   ;;       ads (vals (:activity db))]
   ;;   (s/join ads as {:path :path}))
   (:activity db)))
