(ns reboot.subs
  (:require
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
   (-> db :activities :activities)))

(rf/reg-sub
 :workouts
 (fn [db _]
   (-> db :user-workouts :workouts)))

(rf/reg-sub
 :workout-sort
 (fn [db _]
   (:workout-sort db)))
