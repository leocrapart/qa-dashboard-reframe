(ns qa-dashboard-reframe.views
  (:require
   [re-frame.core :as rf]
   [qa-dashboard-reframe.subs]
   ))

;; aim
;; give the QA a syntetic dashboard with only what's needed to communicate
;; no more everystat you need etc ...
;; everything meaningful for the QA communication located in one place
;; to easily monitor a project and give some overview of it's current state
;; and should also be easily understood by other people
;; (at least the see if all good, or if something is wrong, what is wrong and the gherkin scenario corresponding + error message)
;; so that they can share with the QA if he did not see, and communicate with enought details the problem



(defn button [text event-name]
	[:button.bg-blue-300.px-4.py-2.rounded
		{:on-click (fn [e]
									(.preventDefault e)
									(rf/dispatch [event-name]))}
		text])



(defn parse-job-log [job-log]
  {:passed 19
   :failed 0
   :duration 6555
   :run-id 81004
  })


(defn main-panel [github-names]
  (let [name (rf/subscribe [:name])
				timeline (rf/subscribe [:timeline])
				run-log-url (rf/subscribe [:run-log-url])
				tests-results (rf/subscribe [:tests-results])]
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


     (button "fetch timeline" :fetch-timeline)
     ; [:div (str @timeline)]

     [:div (str @run-log-url)]

     (button "fetch run-log" :fetch-run-log)

     [:div.text-green-500 (str "Passed: " (:passed @tests-results))]
     [:div.text-red-500 (str "Failed: " (:failed @tests-results))]
     [:div.text-gray-500 (str "Skipped: " (:skipped @tests-results))]
     
     ]))


