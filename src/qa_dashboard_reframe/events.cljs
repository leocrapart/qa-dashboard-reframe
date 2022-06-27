(ns qa-dashboard-reframe.events
  (:require
   [re-frame.core :as rf]
   [qa-dashboard-reframe.db :as db]
   [ajax.core :as ajax]
   [clojure.string :as str]))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))



;; fetch ditto with xhrio call

;; call with (dispatch [:get-ditto])


;;azure api
;;

(def pat "wj47nae3gzcsrrrge43myr3qoracbmudit3eb3qhahsgwidyczha")
(def pat-authorization-header {:authorization (str "Basic " pat)})

(defn last-build-id []
	39655)


;; a fetch here
(defn timeline-url [build-id]
	(let [url (str "https://dev.azure.com/hagerdevops-prod/Platform/_apis/build/builds/" build-id "/Timeline")]))

(defn timeline [build-id]
	(let [url (str "https://dev.azure.com/hagerdevops-prod/Platform/_apis/build/builds/" build-id "/Timeline")]))

(defn is-run-integration-test-job [record]
	(let [record-type (record :type)
				name (record :name)]
		(if (and (= record-type "Job")
				   	 (= name "Run integration tests on DEV"))
			true
			false)))

(defn good-job [timeline]
	(filter is-run-integration-test-job (timeline :records)))

(defn run-log-url [timeline]
	(let [records (timeline :records)
				job (first (filter is-run-integration-test-job records))]
		(get-in job [:log :url])))


;; with this, can display ui
;; some parsing here
(defn parsed-run-log [raw-run-log]
	{:passed 19
	 :failed 0
	 :duration 6
	 :test-results-url "https://dev.azure.com/hagerdevops-prod/Platform/_TestManagement/Runs?runId=81004&_a=runCharts"})


;; api

(rf/reg-event-fx
	:fetch-last-build-id
	(fn [_ _]
		{:http-xhrio {:method :get
									:uri "https://dev.azure.com/hagerdevops-prod/Platform/_apis/build/builds/39655/Timeline"
									:headers {:authorization "Basic OmZ2aTM3bmdwdnE3czRqbmltbG5iZmxoem55cnZiM2xqZnlnenRtYWl2am40YXg2cHRsN3E="}
									:response-format (ajax/json-response-format {:keywords? true})
									:on-success [:save-last-build-id]
									:on-failure [:print-result]}}))


(rf/reg-event-fx
	:fetch-timeline
	(fn [_ _]
		{:http-xhrio {:method :get
									:uri "https://dev.azure.com/hagerdevops-prod/Platform/_apis/build/builds/39655/Timeline"
									:headers {:authorization "Basic OmZ2aTM3bmdwdnE3czRqbmltbG5iZmxoem55cnZiM2xqZnlnenRtYWl2am40YXg2cHRsN3E="}
									:response-format (ajax/json-response-format {:keywords? true})
									:on-success [:save-timeline-run-log-url]
									:on-failure [:print-result]}}))


(rf/reg-event-fx
	:fetch-run-log
	(fn [{:keys [db]} _]
		{:http-xhrio {:method :get
									:uri (db :run-log-url)
									:headers {:authorization "Basic OmZ2aTM3bmdwdnE3czRqbmltbG5iZmxoem55cnZiM2xqZnlnenRtYWl2am40YXg2cHRsN3E="}
									:response-format (ajax/json-response-format {:keywords? true})
									:on-success [:save-tests-results]
									:on-failure [:print-result]}}))



(rf/reg-event-fx
	:save-last-build-id
	(fn [_ [_ builds]])
		{:http-xhrio {:method :get
									:uri "https://dev.azure.com/hagerdevops-prod/Platform/_apis/build/builds"
									:headers pat-authorization-header
									:response-format (ajax/json-response-format {:keywords? true})
									:on-success [:save-last-build-id]
									:on-failure [:print-result]
									}})

(rf/reg-event-fx
	:print-run-log
	(fn [_ [_ run-log]]
		(prn "run-log")
		(prn (run-log :value))))

(defn split-by-z-space [line]
	(str/split line #"Z "))

(defn line-without-time [line]
	(let [split-line (split-by-z-space line)]
		(second split-line)))

(defn contains-skipped [line]
	(if (str/includes? line "Skipped:")
		true
		false))
		

(defn passed-tests-result [line]
	(int
		(first 
			(str/split (second
										(str/split line #"Passed:    "))
									#", "))))

(defn failed-tests-result [line]
	(int
		(first 
			(str/split (second
										(str/split line #"Failed:    "))
									#", "))))
		
(defn skipped-tests-result [line]
	(int
		(first 
			(str/split (second
										(str/split line #"Skipped:    "))
									#", "))))


(defn duration-tests-result [line]
	(int
		(first 
			(str/split (second
										(str/split line #"Duration: "))
									#" s - "))))



(defn tests-results [run-log]
	(let [tests-results-line (first (filter contains-skipped run-log))]
		{:passed (passed-tests-result tests-results-line)
		 :failed (failed-tests-result tests-results-line)
		 :skipped (skipped-tests-result tests-results-line)
		 ; :duration (duration-tests-result tests-results-line)
	 	 :test-results-url "alala"
		 }))



(rf/reg-event-db
	:save-tests-results
	(fn [db [_ res-run-log]]
		(let [run-log (res-run-log :value)]
			(prn "run-log")
			(prn run-log)
			(prn "tests-results")
			(prn (tests-results run-log))
			(assoc db :tests-results (tests-results run-log)))))



(rf/reg-event-fx
	:print-result
	(fn [_ [_ result]]
		(prn "result")
		(prn result)))



(rf/reg-event-db
	:save-timeline
	(fn [db [_ timeline]]
		(assoc db :timeline timeline)))


(rf/reg-event-db
	:save-timeline-run-log-url
	(fn [db [_ timeline]]
		(assoc db :run-log-url (run-log-url timeline))
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
