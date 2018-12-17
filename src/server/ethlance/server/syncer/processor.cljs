(ns ethlance.server.syncer.processor
  "Processes events that have been passed through the "
  (:require
   [cuerdas.core :as str]
   [clojure.pprint :refer [pprint]]
   [bignumber.core :as bn]
   [ethlance.server.db :as db]
   [taoensso.timbre :as log]
   [district.server.config :refer [config]]
   [district.shared.error-handling :refer [try-catch]]
   [district.server.web3 :refer [web3]]
   [clojure.core.async :as async :refer [go go-loop <! >! chan] :include-macros true]

   ;; Enums
   [ethlance.shared.enum.currency-type :as enum.currency]
   [ethlance.shared.enum.payment-type :as enum.payment]
   [ethlance.shared.enum.bid-option :as enum.bid-option]

   ;; Ethlance Models
   [ethlance.server.model.job :as model.job]
   [ethlance.server.model.user :as model.user]
   [ethlance.server.model.arbiter :as model.arbiter]
   [ethlance.server.model.candidate :as model.candidate]
   [ethlance.server.model.employer :as model.employer]
   
   ;; Ethlance Contracts
   [ethlance.server.contract.ethlance-user :as contract.user :include-macros true]
   [ethlance.server.contract.ethlance-user-factory :as contract.user-factory]
   [ethlance.server.contract.ethlance-job-store :as contract.job :include-macros true]
   [ethlance.server.contract.ethlance-job-factory :as contract.job-factory]
   [ethlance.server.contract.ethlance-work-contract :as contract.work-contract]
   [ethlance.server.contract.ethlance-invoice :as contract.invoice :include-macros true]
   [ethlance.server.contract.ethlance-dispute :as contract.dispute :include-macros true]

   ;; Misc.
   [ethlance.server.ipfs :as ipfs]
   [ethlance.shared.async-utils :refer [<!-<log <!-<throw flush! go-try] :include-macros true]))


(defn pp-str [x]
  (with-out-str (pprint x)))


(defmulti process-event
  "Process an emitted event based on the `event-multiplexer/event-watchers` key.

  # Notes

  - Implementations are expected to return a single value async channel."
  :name)


(defmethod process-event :default
  [{:keys [name args] :as event}]
  (go (log/warn (str/format "Unprocessed Event: %s\n%s" (pr-str name) (pp-str event)))))


(declare process-registry-event)
(defmethod process-event :registry-event
  [event]
  (go (<! (process-registry-event event))))


(defmulti process-registry-event
  "Process a :registry-event. Each registry event has a unique :event_name

  # Notes

  - Similar to `process-event`, implementations must return a channel
  which places a value on completion.

  - The event name is a kebab-cased keyword from the
  original :event_name

    ex. UserRegistered --> :user-registered
  "
  (fn [{:keys [args]}] (-> args :event_name str/keyword)))


(defmethod process-registry-event :default
  [{:keys [args] :as event}]
  (go (log/warn (str/format "Unprocessed Registry Event: %s\n%s"
                            (-> args :event_name str/keyword pr-str)
                            (pp-str event)))))


(defmethod process-registry-event :user-registered
  [{:keys [args]}]
  (go-try
   (let [user-id (-> args :event_data first bn/number)]
     (contract.user/with-ethlance-user (contract.user-factory/user-by-id user-id)
       (let [ipfs-data (<!-<throw (ipfs/get-edn (contract.user/metahash-ipfs)))
             user-address (contract.user/user-address)
             date-created (contract.user/date-created)
             date-updated (contract.user/date-updated)
             
             user-data (assoc ipfs-data
                              :user/id user-id
                              :user/address user-address
                              :user/date-updated (bn/number date-updated)
                              :user/date-created (bn/number date-created))]
         (model.user/register! user-data))))))


(defmethod process-registry-event :user-registered-employer
  [{:keys [args]}]
  (go-try
   (let [user-id (-> args :event_data first bn/number)
         timestamp (-> args :timestamp bn/number)]
     (contract.user/with-ethlance-user (contract.user-factory/user-by-id user-id)
        (let [ipfs-data (<!-<throw (ipfs/get-edn (contract.user/metahash-ipfs)))
              employer-data (assoc ipfs-data :employer/date-registered timestamp)]
           (model.employer/register! employer-data))))))