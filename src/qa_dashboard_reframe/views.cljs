(ns qa-dashboard-reframe.views
  (:require
   [re-frame.core :as rf]
   [qa-dashboard-reframe.subs :as subs]
   ))



(def ditto-url
  "https://pokeapi.co/api/v2/pokemon/ditto")

(defn ditto-res [])



(defn last-build-id []
	39655)


;; a fetch here
(defn timeline [build-id]
	(let [url (str "https://dev.azure.com/hagerdevops-prod/Platform/_apis/build/builds/" build-id "/Timeline")]))

(defn is-run-integration-test-job [record]
	(let [type (record :type)
				name (record :name)]
		(if (and (= (type "Job"))
				   	 (= (name "Run integration tests on DEV")))
			true
			false)))


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

(defn fetch-button []
	[:button.bg-blue-300.px-4.py-2.rounded
		{:on-click (fn [e]
									(.preventDefault e)
									(rf/dispatch [:fetch-ditto]))}
		"Fetch ditto"])





(defn parse-job-log [job-log]
  {:passed 19
   :failed 0
   :duration 6555
   :run-id 81004
  })


(defn main-panel [github-names]
  (let [name (rf/subscribe [::subs/name])
				ditto (rf/subscribe [::subs/ditto])]
    [:div
     [:div.bg-gray-200.text-xl.flex.justify-center.py-2
      "QA Dashboard " @name]

     [:div.bg-green-500.hover:bg-green-600.px-2 "Create"]
     [:div.px-2 "pass rate = 100%"]
     [:div.px-2 "test coverage = 100%"]
     [:div.px-2 "."]

     [:div.bg-blue-500.hover:bg-blue-600.px-2 "Get"]
     [:div.px-2 "pass rate = 100%"]
     [:div.px-2 "test coverage = 100%"]
     [:div.px-2 "."]

     [:div.bg-orange-500.hover:bg-orange-600.px-2 "Update"]
     [:div.px-2 "pass rate = 100"]
     [:div.px-2 "test coverage = 100%"]
     [:div.px-2 "."]

     [:div.bg-red-500.hover:bg-red-600.px-2 "Delete"]
     [:div.px-2 "pass rate = 100%"]
     [:div.px-2 "test coverage = 100%"]
     [:div.px-2 "."]

     (fetch-button)

     [:a {:href "https://google.com"} "google"]

     [:div (str @ditto)]
     ]))


