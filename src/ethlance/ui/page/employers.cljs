(ns ethlance.ui.page.employers
  "General Employer Listings on ethlance"
  (:require
   [taoensso.timbre :as log]
   [district.ui.component.page :refer [page]]

   [ethlance.shared.enumeration.currency-type :as enum.currency]
   [ethlance.shared.constants :as constants]

   ;; Ethlance Components
   [ethlance.ui.component.main-layout :refer [c-main-layout]]
   [ethlance.ui.component.rating :refer [c-rating]]
   [ethlance.ui.component.tag :refer [c-tag c-tag-label]]
   [ethlance.ui.component.text-input :refer [c-text-input]]
   [ethlance.ui.component.radio-select :refer [c-radio-select c-radio-search-filter-element]]
   [ethlance.ui.component.search-input :refer [c-chip-search-input]]
   [ethlance.ui.component.currency-input :refer [c-currency-input]]
   [ethlance.ui.component.inline-svg :refer [c-inline-svg]]
   [ethlance.ui.component.select-input :refer [c-select-input]]
   [ethlance.ui.component.mobile-search-filter :refer [c-mobile-search-filter]]
   [ethlance.ui.component.profile-image :refer [c-profile-image]]))


(defn c-employer-search-filter []
  [:div.search-filter
   [:div.category-selector
    [c-select-input
     {:label "All Categories"
      :default-selection "All Categories"
      :color :secondary
      :selections ["All Categories" "Software Development" "Web Design"]}]]
   [:span.rating-label "Min. Rating"]
   [c-rating {:rating 1 :color :white :size :small
              :on-change (fn [index] (log/debug "Min. Rating: " index))}]

   [:span.rating-label "Max. Rating"]
   [c-rating {:rating 5 :color :white :size :small
              :on-change (fn [index] (log/debug "Max. Rating: " index))}]

   [c-text-input {:placeholder "Number of Feedbacks"}]

   [:div.country-selector
    [c-select-input
     {:label "Select Country"
      :selections constants/countries
      :search-bar? true
      :color :secondary
      :default-search-text "Search Countries"}]]])


(defn c-employer-mobile-search-filter
  []
  [c-mobile-search-filter
   [:div.category-selector
    [c-select-input
     {:label "All Categories"
      :default-selection "All Categories"
      :color :secondary
      :selections ["All Categories" "Software Development" "Web Design"]}]]

   [:span.rating-label "Min. Rating"]
   [c-rating {:rating 1 :color :white :size :small
              :on-change (fn [index] (log/debug "Min. Rating: " index))}]

   [:span.rating-label "Max. Rating"]
   [c-rating {:rating 5 :color :white :size :small
              :on-change (fn [index] (log/debug "Max. Rating: " index))}]

   [c-text-input {:placeholder "Number of Feedbacks"}]

   [:div.country-selector
    [c-select-input
     {:label "Select Country"
      :selections constants/countries
      :search-bar? true
      :color :secondary
      :default-search-text "Search Countries"}]]])


(defn c-employer-element
  [employer]
  [:div.employer-element
   [:div.profile
    [c-profile-image {}]
    [:div.name "Brian Curran"]
    [:div.title "Content Creator, Web Developer, Blockchain Analyst"]]
   #_[:div.price "$15 / Fixed Price"]
   [:div.tags
    [c-tag {} [c-tag-label "System Administration"]]
    [c-tag {} [c-tag-label "Game Design"]]
    [c-tag {} [c-tag-label "C++ Programming"]]
    [c-tag {} [c-tag-label "HopScotch Master"]]]
   [:div.rating
    [c-rating {:rating 3}]
    [:div.label "(4)"]]
   [:div.location "New York, United States"]])


(defn c-employer-listing []
  [:<>
   (doall
    (for [employer (range 10)]
      ^{:key (str "employer-" employer)}
      [c-employer-element employer]))])


(defmethod page :route.user/employers []
  (let []
    (fn []
      [c-main-layout {:container-opts {:class :employers-main-container}}
       [c-employer-search-filter]
       [c-employer-mobile-search-filter]
       [:div.employer-listing.listing {:key "listing"}
        [c-chip-search-input {:default-chip-listing #{"C++" "Python"}}]
        [c-employer-listing]]])))