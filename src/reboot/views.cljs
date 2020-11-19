(ns reboot.views
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf :refer [dispatch]]))

(defn login
  []
  (let [default {:username "" :password ""}
        credentials (reagent/atom default)]
    (fn []
      (let [{:keys [username password]} @credentials
            login-user (fn [event credentials]
                         (.preventDefault event)
                         (rf/dispatch [:login credentials]))]
        [:form {:class "pure-form"}
         [:fieldset
          [:legend "Login"]
          [:input {:type "text"
                   :placeholder "Username"
                   :value username
                   :on-change #(swap! credentials assoc :username (-> % .-target .-value))}]
          [:input {:type "password"
                   :placeholder "Password"
                   :value password
                   :on-change #(swap! credentials assoc :password (-> % .-target .-value))}]
          [:button {:class "pure-button pure-button-primary"
                    :on-click #(login-user % @credentials)} "Login"]]]))))

(defn workouts
  []
  (let [{col :col order :order :as workout-sort} @(rf/subscribe [:workout-sort])
        sorted (cond->> @(rf/subscribe [:workouts])
                 col (sort-by col)
                 order (reverse))]
    (prn "workout-sort: " workout-sort col order)
    [:div
     [:h4 "Workouts"]
     [:table {:class "pure-table"}
      [:thead
       [:tr
        [:td {:on-click #(rf/dispatch [:workout-sort :name])}"Name"]
        [:td {:on-click #(rf/dispatch [:workout-sort :xss])} "XSS"]
        [:td {:on-click #(rf/dispatch [:workout-sort :difficulty])} "Difficulty"]
        [:td {:on-click #(rf/dispatch [:workout-sort :duration])} "Duration"]
        [:td {:on-click #(rf/dispatch [:workout-sort :advisorScore])} "Advisor Score"]]]
      [:tbody
       (for [{:keys [_id name xss difficulty duration advisorScore]} sorted]
         [:tr ^{:key _id}
          [:td name]
          [:td xss]
          [:td (Math/round difficulty)]
          [:td duration]
          [:td (Math/round advisorScore)]])]]]))

(defn activities
  []
  (let [activities @(rf/subscribe [:activities])]
    [:div
     [:h4 "Activities"]
     [:table {:class "pure-table"}
      [:thead
       [:tr
        [:td "Date"]
        [:td "Name"]]]
      [:tbody
       (for [{:keys [path name start_date description activity_type]} activities]
         [:tr ^{:key path}
          [:td (-> start_date :date)]
          [:td name]])]]]))

(defn xert-app
  []
  (let [auth     @(rf/subscribe [:authentication])
        loading? @(rf/subscribe [:loading?])]
    [:div {:id "layout" :class "pure-g"}
     [:div {:class "pure-u-1"}
      [login]
      [:section
       [:div (str "access-token: " (:access_token auth))]
       [:div (str "refresh-token: "(:refresh_token auth))]]
      [:section
       [:button {:class "button-secondary pure-button"
                 :on-click #(rf/dispatch [:get-user-workouts])} "Workouts"]
       [:button {:class "button-secondary pure-button"
                 :on-click #(rf/dispatch [:get-activities])} "Activites"]
       (if loading?
         [:p "Loading..."])]
      [:section
       [:button {:class "button-secondary pure-button"
                 :on-click #(rf/dispatch [:clear-workouts])} "Clear Workouts"]
       [:button {:class "button-secondary pure-button"
                 :on-click #(rf/dispatch [:clear-activities])} "Clear Activities"]]
      [:div {:class "content"}
       [workouts]
       [activities]]]]))
