(ns reboot.views
  (:require
   [re-frame.core :as rf :refer [dispatch]]))

(defn user-input
  []
  (let [gettext (fn [e] (-> e .-target .-value))
        emit    (fn [e] (rf/dispatch [:user-change (gettext e)]))]
    [:div
     [:input {:type      "text"
              :value     @(rf/subscribe [:username])
              :on-change emit}]]))

(defn password-input
  []
  (let [gettext (fn [e] (-> e .-target .-value))
        emit    (fn [e] (rf/dispatch [:password-change (gettext e)]))]
    [:div
     [:input {:type      "password"
              :value     @(rf/subscribe [:password])
              :on-change emit}]]))

(defn workouts
  []
  (let [workouts @(rf/subscribe [:workouts])]
    [:div {:class "workout-list"} "Workouts"
     (for [{:keys [_id name xss difficulty duration advisorScore]} workouts]
       [:div ^{:key _id} {:class "flex workout"}
        [:div {:class "name"} name]
        [:div {:class "xss"} xss]
        [:div {:class "difficulty"} difficulty]
        [:div {:class "duration"} duration]
        [:div {:class "advistorScore"} advisorScore]])]))

(defn activities
  []
  (let [activities @(rf/subscribe [:activities])]
    [:div {:class "activities-list"} "Activities"
     (for [{:keys [path name start_date description activity_type]} activities]
       [:div ^{:key path} {:class "flex activity"}
        [:div (-> start_date :date)]
        [:div name]
        [:div activity_type]])]))

(defn xert-app
  []
  (let [auth     @(rf/subscribe [:authentication])
        loading? @(rf/subscribe [:loading?])]
    [:h1 "Xert"
     [:div
      [user-input]
      [password-input]]
     [:div (str "access-token: " (:access_token auth))]
     [:div (str "refresh-token: "(:refresh_token auth))]
     [:div
      [:button {:on-click #(rf/dispatch [:login])} "Login"]
      [:button {:on-click #(rf/dispatch [:get-user-workouts])} "Workouts"]
      [:button {:on-click #(rf/dispatch [:get-activities])} "Activites"]
      (if loading?
        [:text "Loading..."])]
     [:div
      [:button {:on-click #(rf/dispatch [:clear-workouts])} "Clear Workouts"]
      [:button {:on-click #(rf/dispatch [:clear-activities])} "Clear Activities"]]
     [workouts]
     [activities]]))
