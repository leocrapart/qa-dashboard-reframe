(ns qa-dashboard-reframe.views
  (:require
   [re-frame.core :as rf]
   [qa-dashboard-reframe.subs]
   ))



(def ditto-url
  "https://pokeapi.co/api/v2/pokemon/ditto")

(defn ditto-res [])







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
				ditto (rf/subscribe [:ditto])
				timeline (rf/subscribe [:timeline])
				run-log-url (rf/subscribe [:run-log-url])]
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

     (button "Fetch ditto" :fetch-ditto)
     (button "new ditto" :ditto)
     [:div (str @ditto)]


     (button "fetch timeline" :fetch-timeline)
     ; [:div (str @timeline)]

     [:div (str @run-log-url)]

     (button "fetch run-log" :fetch-run-log)

     
     ]))


