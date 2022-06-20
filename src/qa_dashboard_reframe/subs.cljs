(ns qa-dashboard-reframe.subs
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [re-frame.core :as re-frame]
   [cljs-http.client :as http]
   [cljs.core.async :refer [<!]]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))


