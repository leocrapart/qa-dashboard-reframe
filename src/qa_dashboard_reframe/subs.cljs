(ns qa-dashboard-reframe.subs
  (:require
   [re-frame.core :as rf]))


(rf/reg-sub
 ::name
 (fn [db]
   (:name db)))

(rf/reg-sub
 ::ditto
 (fn [db]
   (db :ditto)))

