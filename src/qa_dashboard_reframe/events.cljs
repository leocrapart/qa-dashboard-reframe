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


;;azure api
;;

(def pat "fvi37ngpvq7s4jnimlnbflhznyrvb3ljfygztmaivjn4ax6ptl7q")
(def basic-auth-authorization (str "Basic " pat))

(defn last-build-id []
	39655)


;; a fetch here
(defn timeline-url [build-id]
	(let [url (str "https://dev.azure.com/hagerdevops-prod/Platform/_apis/build/builds/" build-id "/Timeline")]))

(defn timeline [build-id]
	(let [url (str "https://dev.azure.com/hagerdevops-prod/Platform/_apis/build/builds/" build-id "/Timeline")]))

(defn is-run-integration-test-job [record]
	(let [type (record :type)
				name (record :name)]
		(if (and (= (type "Job"))
				   	 (= (name "Run integration tests on DEV")))
			true
			false)))

(defn good-job [timeline]
	(filter is-run-integration-test-job (timeline :records)))

(defn run-log-url [timeline]
	(let [records (timeline :records)
				job (filter is-run-integration-test-job records)]
		(get-in job [:log :url])))

;;another fetch here
(defn raw-run-log [run-log-url])

;; with this, can display ui
;; some parsing here
(defn parsed-run-log [raw-run-log]
	{:passed 19
	 :failed 0
	 :duration 6
	 :test-results-url "https://dev.azure.com/hagerdevops-prod/Platform/_TestManagement/Runs?runId=81004&_a=runCharts"})


;; api
;; async await here ? fetching constraints
(defn test-results []
	(parsed-run-log 
		(raw-run-log 
			(run-log-url
				(timeline
					(last-build-id))))))


									
(rf/reg-event-fx
	:fetch-ditto
	(fn [_ _]
		{:http-xhrio {:method :get
									:uri "https://pokeapi.co/api/v2/poemon/ditto"
									:response-format (ajax/json-response-format {:keywords? true})
									:on-success [:save-ditto]
									:on-failure [:print-failure]}}))



(rf/reg-event-fx
	:fetch-timeline
	(fn [_ _]
		{:http-xhrio {:method :get
									:uri "https://dev.azure.com/hagerdevops-prod/Platform/_apis/build/builds/39655/Timeline"
									:headers {:authorization "Basic OmZ2aTM3bmdwdnE3czRqbmltbG5iZmxoem55cnZiM2xqZnlnenRtYWl2am40YXg2cHRsN3E="}
									:response-format (ajax/json-response-format {:keywords? true})
									:on-success [:save-timeline]
									:on-failure [:print-result]}}))

(rf/reg-event-fx
	:print-result
	(fn [_ [_ result]]
		(prn result)))


(defn equals-good [number]
	(= (32 number)))

(rf/reg-event-db
	:save-timeline
	(fn [db [_ timeline]]
		(prn db)
		(prn timeline)
		(prn (timeline :records))
		(prn (nth (timeline :records) 32))
		(prn ((nth (timeline :records) 32) :name))
		(prn (((nth (timeline :records) 32) :log) :url))
		(prn (let [number 32]
						(= 32 number)))

		(assoc db :timeline timeline)))

(rf/reg-event-db
	:save-ditto
	(fn [db [_ result]]
		(prn db)
		(assoc db :ditto result)
		))

(rf/reg-event-db
		:ditto
		(fn [db _]
				(prn db)
				(assoc db :ditto "new ditto")))

(rf/reg-event-fx
	:print-name
	(fn [_ [_ result]]
		(prn "result")
		(prn (result :name))))

(rf/reg-event-fx
	:print-failure
	(fn [_ _]
		(prn "failure")))
