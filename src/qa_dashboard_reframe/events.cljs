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

; (rf/reg-event-db
; 	:save-timeline-run-log-url
; 	(fn [db [_ timeline]]
; 		(assoc db :run-log-url (run-log-url timeline))))

(rf/reg-event-fx
	:save-timeline-run-log-url
	(fn [{:keys [db]} [_ timeline]]
		{:db (assoc db :run-log-url (run-log-url timeline))
		 :fx [[:dispatch [:fetch-run-log]]]}))


(defn timeline-url [build-id]
	(str "https://dev.azure.com/hagerdevops-prod/Platform/_apis/build/builds/" build-id "/Timeline"))


;; timeline
(rf/reg-event-fx
	:fetch-timeline
	(fn [_ [_ build-id]]
		{:http-xhrio {:method :get
									:uri (timeline-url build-id)
									:headers pat-authorization-header
									:response-format (ajax/json-response-format {:keywords? true})
									:on-success [:save-timeline-run-log-url]
									:on-failure [:print-result]}}))



;; last build-id parsing

; (defn last-build-id []
; 	39655)


(defn is-digital-graph-data-build [build]
	(let [name (get-in build [:definition :name])]
		(if (= "DigitalGraphData" name)
			true
			false)))

(defn digital-graph-data-builds [builds]
	(filter is-digital-graph-data-build builds))



(defn build-name [build]
	(get-in build [:definition :name]))

(defn builds-names [builds]
	(map build-name builds))

(defn last-build-id [builds]
	(let [digi-builds (digital-graph-data-builds builds)
				last-build (nth digi-builds 0)]
		(last-build :id)))


(defn digital-graph-data-builds-ids [builds]
	(let [digi-builds (digital-graph-data-builds builds)]
		(map :id digi-builds)))

(rf/reg-event-fx
	:save-last-build-id
	(fn [{:keys [db]} [_ result]]
		(let [builds (result :value)
					last-build-id (last-build-id builds)
					builds-ids (digital-graph-data-builds-ids builds)
					second-last-build-id (nth builds-ids 20)]

			(prn "digital graph data builds ids")
			(prn (digital-graph-data-builds-ids builds))

			{:db (assoc db :last-build-id last-build-id
										 :builds-ids builds-ids)
			 :fx [[:dispatch [:fetch-timeline last-build-id]]]})))

; (rf/reg-event-db
; 	:save-last-build-id
; 	(fn [db [_ result]]
; 		(let [builds (result :value)
; 					; first-build (nth (result :value) 0)
; 					; n 60
; 					; nbuild (nth (result :value) n)
; 					]
; 			; (prn "first build")
; 			; (prn first-build)
; 			; (prn "first build type")
; 			; (prn (type first-build))

; 			; (prn "first build name")
; 			; (prn (get-in first-build [:definition :name]))

; 			; (prn (str "is digi build " n))
; 			; (prn (build-name nbuild))
; 			; (prn (is-digital-graph-data-build nbuild))

; 			; (prn "are digi builds ?")
; 			; (prn (are-digital-graph-data-builds builds))

; 			; (prn "builds names")
; 			; (prn (builds-names builds))

; 			; (prn "digi builds")
; 			; (prn (digital-graph-data-builds builds))

; 			; (prn "number of digi builds")
; 			; (prn (count (digital-graph-data-builds builds)))

; 			; (prn "last-build-id")
; 			; (prn (last-build-id builds))

; 			; (prn "digital graph data builds ids")
; 			; (prn (digital-graph-data-builds-ids builds))

; 			(assoc db :last-build-id (last-build-id builds)
; 								:builds-ids (digital-graph-data-builds-ids builds)))))

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
		{:fx [[:dispatch [:fetch-builds]]]}))


;; tests results
(rf/reg-event-fx
	:get-tests-results
	(fn [_ _]
		{:fx [[:dispatch [:get-last-build-id]]]}))




;; with this, can display ui
;; some parsing here
(defn tests-results-model [raw-run-log]
	{:passed 19
	 :failed 0
	 :duration 6
	 :test-results-url "https://dev.azure.com/hagerdevops-prod/Platform/_TestManagement/Runs?runId=81004&_a=runCharts"})



















