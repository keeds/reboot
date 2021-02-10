(ns reboot.views
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf :refer [dispatch]]
   [reboot.charts :as charts]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Login

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


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Workouts

(defn workouts
  []
  (let [workout-sort (rf/subscribe [:workout-sort])
        sorted (rf/subscribe [:workouts])]
    (fn []
      (let [{col :col order :order :as workout-sort} @workout-sort
            sorted (cond->> @sorted
                     col (sort-by col)
                     order (reverse))]
        [:section
         [:button {:class "btn btn-secondary"
                   :on-click #(rf/dispatch [:get-user-workouts])} "Fetch"]
         [:button {:class "btn btn-secondary"
                   :on-click #(rf/dispatch [:clear-workouts])} "Clear"]
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
                                         :class "workout-thumb"}]]])]]])]))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Activities

(defn activities
  []
  (let [activity-sort (rf/subscribe [:activity-sort])
        activities (rf/subscribe [:activities])
        details (rf/subscribe [:activity-details])]
    (fn []
      (let [{col :col order :order} @activity-sort
            sorted (cond->> @activities
                     col (sort-by col)
                     order (reverse))
            details @details]
        [:section
         [:button {:class "btn btn-secondary"
                   :on-click #(rf/dispatch [:get-activities])} "Fetch"]
         [:button {:class "btn btn-warning"
                   :on-click #(rf/dispatch [:clear-activities])} "Clear"]
         [:button {:class "btn btn-info"
                   :on-click #(rf/dispatch [:fetch-all-activity-details])} "Details"]
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
               [:td "fresh"]
               [:td ""]]]
             [:tbody
              (for [{:keys [path name start_date xss xep difficulty freshness]} sorted]
                (let [{:keys [xss xep difficulty freshness chart_view]} (:summary (get details path))]
                  ^{:key path} [:tr
                                [:td (-> start_date :date js/Date. (.toLocaleString "en-GB"))]
                                [:th {:scope name} name]
                                [:td (Math/round xss)]
                                [:td (Math/round xep)]
                                [:td (Math/round difficulty)]
                                [:td freshness]
                                [:td [:img {:src chart_view
                                            :class "workout-thumb"}]]]))]]])]))))

(defn data
  [activity-data]
  (let [dates-fn #(-> % second :summary :start_date :date)
        data-fn #(-> % second :summary :session ((juxt :avg_power :max_power :avg_heart_rate :max_heart_rate)))
        data (map
              (juxt dates-fn data-fn)
              activity-data)]
    (map #(hash-map (first %) (zipmap [:avg-power :max-power :avg-hr :max-power] (second %)))
         data)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Charts

(defn charts
  []
  (let [activity-data (rf/subscribe [:activity-details])
        data (data @activity-data)]
    (prn data)
    [:div
     [:h2 "Charts"]
     [:button {:class "btn btn-secondary"
               :on-click #(charts/build data)} "Chart"]
     [:div#chart]
     [:div
      [:p (count @activity-data)]]]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Base

(defn blank
  []
  [:div
   [:h1 "Blank"]])

(defn tabs [tab]
  (case tab
    :workouts [workouts]
    :activities [activities]
    :charts [charts]
    [blank]))

(defn header
  []
  (let [tab (rf/subscribe [:current-tab])
        set-tab (fn [e new-tab]
                  (.preventDefault e)
                  (rf/dispatch [:set-current-tab new-tab]))]
    (fn []
      [:div
       [:nav {:class "navbar navbar-expand-lg navbar-light bg-light"}
        [:div {:class "container-fluid"}
         [:ul {:class "nav nav-tabs"}
          [:li {:class "nav-item"}
           [:a.nav-link {:class (when (= :workouts @tab) "active")
                         :href "#"
                         :on-click #(set-tab % :workouts)} "Workouts"]]
          [:li.nav-item
           [:a.nav-link {:class (when (= :activities @tab) "active")
                         :href "#"
                         :on-click #(set-tab % :activities)} "Activities"]]
          [:li.nav-item
           [:a.nav-link {:class (when (= :charts @tab) "active")
                         :href "#"
                         :on-click #(set-tab % :charts)} "Charts"]]]]
        [:div
         [logout]]]])))

(defn footer
  []
  (let [loading? (rf/subscribe [:loading?])]
    (fn []
      (if @loading?
        [:div {:class "fixed-bottom"}
         [:span {:class "badge bg-secondary"} "Loading..."]]))))

(defn xert-app
  []
  (let [auth     @(rf/subscribe [:authentication])
        tab      @(rf/subscribe [:current-tab])]
    (if-not (:access_token auth)
      [login]
      [:div
       [header]
       [tabs tab]
       [footer]])))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; REPL

(comment

  (-> @(rf/subscribe [:activity-details])
      first
      second
      keys)

  (-> @(rf/subscribe [:activity-details])
      first
      second
      :summary
      ;; :chart_view
      )
  
  )
