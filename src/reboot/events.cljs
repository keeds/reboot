(ns reboot.events
  (:require
   [ajax.core :as ajax]
   [day8.re-frame.http-fx]
   [goog.crypt.base64 :as base64]
   [goog.date :as date]
   [reboot.db     :refer [default-db]]
   [re-frame.core :refer [reg-event-db reg-event-fx]])
  (:import
   [goog.date Interval]))

(reg-event-fx
 :initialise-db
 (fn []
   {:db default-db}))

(defn- basic-auth-header
  [user password]
  (->> (str user ":" password)
       (base64/encodeString)
       (str "Basic ")))

(reg-event-fx
 :login
 (fn [{db :db} [_ credentials]]
   {:http-xhrio {:method :post
                 :uri "https://www.xertonline.com/oauth/token"
                 :timeout 8000
                 :headers {:authorization (basic-auth-header "xert_public" "xert_public")}
                 :format (ajax/url-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :params {:grant_type "password"
                          :username (:username credentials)
                          :password (:password credentials)}
                 :on-success [:login-success]
                 :on-failure [:bad-response]}
    :db (assoc db :logged-in? false)}))

(reg-event-db
 :login-success
 (fn [db [_ response]]
   (assoc db :authentication response)))

(reg-event-db
 :logout
 (fn [db _]
   (assoc db :authentication nil)))

(reg-event-fx
 :activity
 (fn [{db :db} [_ path]]
   (prn "Activity: " path)
   {:http-xhrio {:method :get
                 :uri (str "https://www.xertonline.com/oauth/activity/" path)
                 :timeout 8000
                 :headers {:authorization (str "Bearer " (-> db :authentication :access_token))}
                 :params {:include_session_data 0}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:activity-success path]
                 :on-failure [:bad-response]}
    :db (assoc db :loading? true db)}))

(reg-event-db
 :activity-success
 (fn [db [_ path response]]
   (let [data (-> response
                  (dissoc :success)
                  (assoc :path path))]
     (-> db
         (assoc :loading? false)
         (update :activity assoc path data)))))

(reg-event-fx
 :get-activities
 (fn [{db :db} _]
   (let [d1   (new date/DateTime)
         d2   (.clone d1)
         i    (new date/Interval Interval.YEARS 1)
         _    (.add d2 (.getInverse i))
         to   (/ (.valueOf d1) 1000)
         from (/ (.valueOf d2) 1000)]
     {:http-xhrio {:method  :get
                   :uri     "https://www.xertonline.com/oauth/activity"
                   :timeout 8000
                   :headers {:authorization (str "Bearer " (-> db :authentication :access_token))}
                   :params {:from from :to to}
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:activities-success]
                   :on-failure [:bad-response]}
      :db (assoc db :loading? true)})))

(reg-event-db
 :activities-success
 (fn [db [_ response]]
   (assoc db
          :loading? false
          :activities (:activities response))))

(reg-event-db
 :clear-activities
 (fn [db _]
   (dissoc db :activities)))

(reg-event-fx
 :get-user-workouts
 (fn [{db :db} _]
   {:http-xhrio {:method  :get
                 :uri     "https://www.xertonline.com/oauth/workouts"
                 :timeout 8000
                 :headers {:authorization (str "Bearer " (-> db :authentication :access_token))}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:user-workouts-success]
                 :on-failure [:bad-response]}
    :db (assoc db :loading? true)}))

(reg-event-db
 :user-workouts-success
 (fn [db [_ response]]
   (assoc db
          :loading? false
          :user-workouts response)))

(reg-event-db
 :clear-workouts
 (fn [db _]
   (dissoc db :user-workouts)))

(reg-event-db
 :user-change
 (fn [db [_ username]]
   (assoc db :username username)))

(reg-event-db
 :password-change
 (fn [db [_ password]]
   (assoc db :password password)))

(reg-event-db
 :bad-response
 (fn [db _]
   (assoc db :loading? false)))


;; Workout sorting
(reg-event-db
 :workout-sort
 (fn [{workout-sort :workout-sort :as db} [_ col]]
   (if (= col (:col workout-sort))
     (update-in db [:workout-sort :order] not)
     (assoc db :workout-sort {:col col :order false}))))

;; Actvity sorting
(reg-event-db
 :activity-sort
 (fn [{activity-sort :activity-sort :as db} [_ col]]
   (if (= col (:col activity-sort))
     (update-in db [:activity-sort :order] not)
     (assoc db :activity-sort {:col col :order false}))))
