(ns qa-dashboard-reframe.events
  (:require
   [re-frame.core :as rf]
   [qa-dashboard-reframe.db :as db]
   [ajax.core :as ajax]))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))



;; fetch ditto with xhrio call

;; call with (dispatch [:get-ditto])


(rf/reg-event-fx
	:fetch-ditto
	(fn [_ _]
		{:http-xhrio {:method :get
									:uri "https://pokeapi.co/api/v2/pokemon/ditto"
									:response-format (ajax/json-response-format {:keywords? true})
									:on-success [:save-result]
									:on-failure [:print-failure]}}))

(rf/reg-event-fx
	:print-result
	(fn [_ [_ result]]
		(prn "result")
		(prn result)))

(rf/reg-event-db
	:save-result
	(fn [db [_ result]]
		(prn "saving result")
		(prn "result")
		(prn result)
		(update db :ditto result)
		(prn "db")
		(prn db)
		))

(rf/reg-event-fx
	:print-name
	(fn [_ [_ result]]
		(prn "result")
		(prn (result :name))))

(rf/reg-event-fx
	:print-failure
	(fn [_ _]
		(prn "failure")))
