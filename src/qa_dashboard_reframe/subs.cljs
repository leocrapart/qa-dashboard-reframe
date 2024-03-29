(ns qa-dashboard-reframe.subs
  (:require
   [re-frame.core :as rf]))


(rf/reg-sub
 :name
 (fn [db]
   (:name db)))

(rf/reg-sub
	:last-build-id
	(fn [db]
		(db :last-build-id)))

(rf/reg-sub
	:timeline
	(fn [db]
		(db :timeline)))

(rf/reg-sub
	:run-log-url
	(fn [db]
		(db :run-log-url)))

(rf/reg-sub
	:tests-results
	(fn [db]
		(db :tests-results)))