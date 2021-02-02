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
        [:div {:class "d-flex align-items-center min-vh-100"}
         [:div {:class "container"}
          [:form {:class "pure-form"}
           [:div {:class "row"}
            [:div {:class "col"}
             [:input {:type "text"
                      :class "form-control"
                      :placeholder "Username"
                      :value username
                      :on-change #(swap! credentials assoc :username (-> % .-target .-value))}]]
            [:div {:class "col"}
             [:input {:type "password"
                      :class "form-control"
                      :placeholder "Password"
                      :value password
                      :on-change #(swap! credentials assoc :password (-> % .-target .-value))}]]
            [:div {:class "col"}
             [:button {:type "button"
                       :class "btn btn-primary"
                       :on-click #(login-user % @credentials)} "Login"]]]]]]))))

(defn logout
  []
  [:p
   [:button {:type "button"
             :class "btn btn-primary"
             :on-click #(rf/dispatch [:logout])} "Logout"]])

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
       [:table {:class "table"}
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
                        [:th {:scope name} name]
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
    (when (seq sorted)
      [:div
       [:h4 "Activities"]
       [:table {:class "table"}
        [:thead
         [:tr
          [:td {:on-click #(rf/dispatch [:activity-sort :date])} "Date"]
          [:td {:on-click #(rf/dispatch [:activity-sort :name])} "Name"]
          [:td "xss"]
          [:td "xep"]
          [:td "diff"]
          [:td "fresh"]]]
        [:tbody
         (for [{:keys [path name start_date xss xep difficulty freshness]} sorted]
           (let [{:keys [xss xep difficulty freshness]} (:summary (get details path))]
             ^{:key path} [:tr {:on-click #(rf/dispatch [:activity path])}
                           [:td (-> start_date :date js/Date. (.toLocaleString "en-GB"))]
                           [:th {:scope name} name]
                           [:td (Math/round xss)]
                           [:td (Math/round xep)]
                           [:td (Math/round difficulty)]
                           [:td freshness]]))]]])))

(defn xert-app
  []
  (let [auth     @(rf/subscribe [:authentication])
        loading? @(rf/subscribe [:loading?])]
    [:div {:class "container"}
     (if-not (:access_token auth)
       [login]
       [:div
        [:nav {:class "navbar navbar-expand-lg navbar-light bg-light"}
         [:div {:class "container-fluid"}
          [:ul {:class "navbar-nav"}
           [:li {:class "nav-item"}
            [:a.navbar-text.nav-link "Workouts"]]
           [:li.nav-item
            [:a.navbar-text.nav-link "Activities"]]
           [:li {:class "nav-item"}
            [logout]]]]]
        [:div
         [:section
          [logout]
          [:div
           [:button {:class "btn btn-primary"
                     :on-click #(rf/dispatch [:get-user-workouts])} "Fetch Workouts"]
           [:button {:class "btn btn-primary"
                     :on-click #(rf/dispatch [:get-activities])} "Fetch Activites"]]]
         [:section
          [:button {:class "btn btn-secondary"
                    :on-click #(rf/dispatch [:clear-workouts])} "Clear Workouts"]
          [:button {:class "btn btn-secondary"
                    :on-click #(rf/dispatch [:clear-activities])} "Clear Activities"]]
         [:div {:class "content"}
          [workouts]
          [activities]]
         (if loading?
           [:div {:class "fixed-bottom"}
            [:span {:class "badge bg-secondary"} "Loading..."]])]])]))
