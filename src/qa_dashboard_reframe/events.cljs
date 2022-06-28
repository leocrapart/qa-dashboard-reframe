(ns qa-dashboard-reframe.events
  (:require
   [re-frame.core :as rf]
   [qa-dashboard-reframe.db :as db]
   [ajax.core :as ajax]
   [clojure.string :as str]
   [qa-dashboard-reframe.env :as env]
   ["js-base64" :as b64]
   ))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))


;;azure api


(def pat env/pat)
; (def pat64 env/pat64)
(def pat64 (b64/encode (str ":" pat)))
;; upgrade : use goog.crypt.base64

(def pat-authorization-header {:authorization (str "Basic " pat64)})




;; a fetch here
(defn timeline-url [build-id]
	(let [url (str "https://dev.azure.com/hagerdevops-prod/Platform/_apis/build/builds/" build-id "/Timeline")]))







;; prints 


(rf/reg-event-fx
	:print-result
	(fn [_ [_ result]]
		(prn "result")
		(prn result)))

(rf/reg-event-fx
	:print-pat
	(fn [_ _]
		(prn "pat")
		(prn pat)
		(prn "pat64")
		(prn pat64)))

(rf/reg-event-fx
	:print-name
	(fn [_ [_ result]]
		(prn "result")
		(prn (result :name))))

(rf/reg-event-fx
	:print-failure
	(fn [_ _]
		(prn "failure")))

(rf/reg-event-fx
	:print-run-log
	(fn [_ [_ run-log]]
		(prn "run-log")
		(prn (run-log :value))))




;; saves


(rf/reg-event-db
	:save-timeline
	(fn [db [_ timeline]]
		(assoc db :timeline timeline)))


;; tests results parsing


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
		 ;; +2 duration and details url
		 ; :duration (duration-tests-result tests-results-line)
	 	 :test-results-url "alala"
		 }))

;; save tests results

(rf/reg-event-db
	:save-tests-results
	(fn [db [_ res-run-log]]
		(let [run-log (res-run-log :value)]
			(prn "run-log")
			(prn run-log)
			(prn "tests-results")
			(prn (tests-results run-log))
			(assoc db :tests-results (tests-results run-log)))))


;; run-log

(rf/reg-event-fx
	:fetch-run-log
	(fn [{:keys [db]} _]
		{:http-xhrio {:method :get
									; :uri "https://dev.azure.com/hagerdevops-prod/Platform/_apis/build/builds/39655/logs/29"
									:uri (db :run-log-url)
									:headers pat-authorization-header
									:response-format (ajax/json-response-format {:keywords? true})
									:on-success [:save-tests-results]
									:on-failure [:print-result]}}))



;; run-log-url from timeline
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


;; save run-log-url
;; chain via effect to auto fetch-run-log
(rf/reg-event-db
	:save-timeline-run-log-url
	(fn [db [_ timeline]]
		(assoc db :run-log-url (run-log-url timeline))
		))




;; timeline
(rf/reg-event-fx
	:fetch-timeline
	(fn [_ _]
		{:http-xhrio {:method :get
									:uri "https://dev.azure.com/hagerdevops-prod/Platform/_apis/build/builds/39655/Timeline"
									:headers pat-authorization-header
									:response-format (ajax/json-response-format {:keywords? true})
									:on-success [:save-timeline-run-log-url]
									:on-failure [:print-result]}}))



;; last build-id parsing

; (defn last-build-id []
; 	39655)
(defn last-build-id [builds])

(rf/reg-event-db
	:save-last-build-id
	(fn [db [_ builds]]
		(prn builds)
		(assoc db :last-build-id 9999)
	))

;; last build-id
(rf/reg-event-fx
	:fetch-builds
	(fn [_ _]
		{:http-xhrio {:method :get
									:uri "https://dev.azure.com/hagerdevops-prod/Platform/_apis/build/builds"
									:headers pat-authorization-header
									:response-format (ajax/json-response-format {:keywords? true})
									:on-success [:save-last-build-id]
									:on-failure [:print-result]
									}}))
;;
(rf/reg-event-fx
	:get-last-build-id
	(fn [_ _]
		{:interval {:action :start
								:id :temp-id
								:event [:fetch-builds]}}))


;; tests results
(rf/reg-event-fx
	:get-tests-results
	(fn [_ _]
		{:interval {:action :start
								:id :temp-id
								:event [:fetch-run-log]}}))




;; with this, can display ui
;; some parsing here
(defn tests-results-model [raw-run-log]
	{:passed 19
	 :failed 0
	 :duration 6
	 :test-results-url "https://dev.azure.com/hagerdevops-prod/Platform/_TestManagement/Runs?runId=81004&_a=runCharts"})



















