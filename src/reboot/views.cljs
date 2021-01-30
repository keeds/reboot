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
          ;; [:legend "Login"]
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

(defn logout
  []
  [:p
   [:button {:class "pure-button pure-button-primary"
             :on-click #(rf/dispatch [:logout])} "Logout"]])

(defn activity-details
  []
  (let [activities @(rf/subscribe [:activity-details])]
    (prn activities)
    [:div
     (count activities)]))

(defn workouts
  []
  (let [{col :col order :order :as workout-sort} @(rf/subscribe [:workout-sort])
        sorted (cond->> @(rf/subscribe [:workouts])
                 col (sort-by col)
                 order (reverse))]
    ;; (prn "workout-sort: " workout-sort col order)
    (when (seq sorted)
      [:div
       [:h4 "Workouts"]
       [:table {:class "pure-table pure-table-bordered"}
        [:thead
         [:tr
          [:td {:on-click #(rf/dispatch [:workout-sort :name])} "Name"]
          [:td {:on-click #(rf/dispatch [:workout-sort :xss])} "XSS"]
          [:td {:on-click #(rf/dispatch [:workout-sort :difficulty])} "Difficulty"]
          [:td {:on-click #(rf/dispatch [:workout-sort :duration])} "Duration"]
          [:td {:on-click #(rf/dispatch [:workout-sort :advisorScore])} "Advisor Score"]
          [:td]]]
        [:tbody
         (for [{:keys [_id name xss difficulty duration advisorScore thumb]} sorted]
           ^{:key _id} [:tr
                        [:td name]
                        [:td xss]
                        [:td (Math/round difficulty)]
                        [:td duration]
                        [:td (Math/round advisorScore)]
                        [:td [:img {:src thumb
                                    :class "workout-thumb"}]]])]]])))

(defn activities
  []
  (let [{col :col order :order} @(rf/subscribe [:activity-sort])
        sorted (cond->> @(rf/subscribe [:activities])
                 col (sort-by col)
                 order (reverse))
        details @(rf/subscribe [:activity-details])]
    ;; (prn details)
    (when (seq sorted)
      [:div
       [:h4 "Activities"]
       [:table {:class "pure-table pure-table-bordered"}
        [:thead
         [:tr
          [:td]
          [:td {:on-click #(rf/dispatch [:activity-sort :date])} "Date"]
          [:td {:on-click #(rf/dispatch [:activity-sort :name])} "Name"]
          [:td "xss"]
          [:td "xep"]
          [:td "diff"]
          [:td "fresh"]]]
        [:tbody
         (for [{:keys [path name start_date xss xep difficulty freshness]} sorted]
           (let [{:keys [xss xep difficulty freshness]} (:summary (get details path))]
             ^{:key path} [:tr
                           [:td {:on-click #(rf/dispatch [:activity path])} "#"]
                           [:td (-> start_date :date)]
                           [:td name]
                           [:td (Math/round xss)]
                           [:td (Math/round xep)]
                           [:td (Math/round difficulty)]
                           [:td freshness]]))]]])))

(defn xert-app
  []
  (let [auth     @(rf/subscribe [:authentication])
        loading? @(rf/subscribe [:loading?])]
    [:div {:id "layout" :class "pure-g"}
     [:div {:class "pure-u-1"}
      (if-not (:access_token auth)
        [login]
        [:p
         [:section
          [logout]
          [:div
           [:button {:class "pure-button-primary pure-button"
                     :on-click #(rf/dispatch [:get-user-workouts])} "Fetch Workouts"]
           [:button {:class "pure-button-primary pure-button"
                     :on-click #(rf/dispatch [:get-activities])} "Fetch Activites"]
           (if loading?
             [:div "Loading..."])]]
         [:section
          [:button {:class "pure-button-secondary pure-button"
                    :on-click #(rf/dispatch [:clear-workouts])} "Clear Workouts"]
          [:button {:class "pure-button-warning pure-button"
                    :on-click #(rf/dispatch [:clear-activities])} "Clear Activities"]]
         [:div {:class "content"}
          ;; [activity-details]
          [workouts]
          [activities]]])]]))
