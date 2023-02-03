(ns ethlance.shared.utils
  (:require-macros [ethlance.shared.utils])
  (:require [alphabase.base58 :as base58]
            [alphabase.hex :as hex]))

(defn now []
  (.getTime (js/Date.)))

(defn base58->hex
  "Useful for converting IPFS hash to a format suitable for storing in Solidity
  bytes memory _ipfsData

  Example:
    (base58->hex \"QmSj298W5U7cn7ync6kLxZgTdmSC1j9cMxeVAc8d6bt2ej\")"
  [base58-str]
  (->> base58-str
       base58/decode
       hex/encode
       (str "0x" ,,,)))

(defn hex->base58
  "Useful for converting Solidity bytes memory _ipfsData back to IPFS hash

  Example:
    (base58->hex \"0x12204129c213954a4864af722e5160c92b158f1215c13416a1165a6ee7142371b368\")"
  [hex-str]
  (-> hex-str
      (clojure.string/replace ,,, #"^0x" "")
      hex/decode
      base58/encode))
