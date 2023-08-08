(ns ethlance.ui.page.profile.subscriptions
  (:require [ethlance.ui.page.profile.events :as profile.events]
            [re-frame.core :as re]
            [district.ui.router.subs :as router-subs]
            [ethlance.ui.subscriptions :as ethlance-subs]
            [ethlance.ui.subscription.utils :as subscription.utils]))


(def create-get-handler #(subscription.utils/create-get-handler profile.events/state-key %))

(re/reg-sub :page.profile/job-for-invitation (create-get-handler :job-for-invitation))
(re/reg-sub :page.profile/invitation-text (create-get-handler :invitation-text))
(re/reg-sub :page.profile/pagination-offset (create-get-handler :pagination-offset))
(re/reg-sub :page.profile/pagination-limit (create-get-handler :pagination-limit))

(re/reg-sub
  :page.profile/viewed-user-address
  :<- [::router-subs/active-page-params]
  :<- [::ethlance-subs/active-session]
  (fn [[params session-user] _]
    (if (empty? (:address params))
      (:user/id session-user)
      (:address params))))
