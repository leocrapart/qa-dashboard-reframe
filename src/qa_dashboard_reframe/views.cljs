(ns qa-dashboard-reframe.views
  (:require
   [re-frame.core :as rf]
   [qa-dashboard-reframe.subs :as subs]
   ))



(def ditto-url
  "https://pokeapi.co/api/v2/pokemon/ditto")

(defn ditto-res [])

(defn closed-pbis [])


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


