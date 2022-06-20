(ns qa-dashboard-reframe.events
  (:require
   [re-frame.core :as re-frame]
   [qa-dashboard-reframe.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))


(re-frame/reg-event-fx
  :get-ditto
  (fn [cofx])
    {:http-xhrio {:method :get
                  :uri "https://pokeapi.co/api/v2/pokemon/ditto"}
                  :on-success [:get-ditto-success]
                  :on-failure [:get-ditto-failure]})

(re-frame/reg-event-db
  :get-user-success
  (fn [db [_ {ditto :ditto}]]
    (assoc db :ditto ditto)))


(re-frame/reg-event-db
  :get-user-failure
  (fn [db [_ {ditto :ditto}]]
    (assoc db :ditto {:fail "fail"})))