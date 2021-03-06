(ns scratch.crime
  (:require [cheshire.core :as json]))

(defn load-json
  "Given a filename, reads a JSON file and returns it, parsed, with keywords."
  [file]
  (json/parse-string (slurp file) true))

(def fips
  "A map of FIPS codes to their county names."
  (->> "fips.json"
       load-json
       :table
       :rows
       (into {})))

(defn fips-code
  "Given a county (a map with :fips_state_code and :fips_county_code keys), returns the five-digit FIPS code for the
  county, as a string."
  [county]
  (str (:fips_state_code county) (:fips_county_code county)))

(defn most-duis
  "Given a JSON filename of UCR crime data for a particular year, finds the counties with the most DUIs."
  [file]
  (->> file
       load-json
       (sort-by :driving_under_influence)
       (take-last 10)
       (map (fn [county]
              [(fips (fips-code county))
               [:duis (:driving_under_influence county)
                :population (:county_population county)
                :report-count (:grand_total county)
                :prevalence (double (/ (:driving_under_influence county) (:county_population county)))]]))
       (into {})))


(defn prevalence-of-duis
  "Prevalence of DUIs."
  [file]
  (->> file
       load-json
       (map (fn [county]
              {:county (fips (fips-code county))
               :duis (:driving_under_influence county)
               :population (:county_population county)
               :prevalence (double (if
                                    (pos? (:county_population county))
                                     (/ (:driving_under_influence county) (:county_population county))
                                     0))}))
       (sort-by :prevalence)
       (take-last 10)))

(defn most-prevalent
  "Given a JSON filename of UCR crime data for a particular year, and a crime, find the counties with the highest prevalence of this crime."
  [file crime]
  (->> file
       load-json
       (map (fn [county]
              {:county (fips (fips-code county))
               :occurrence (crime county)
               :population (:county_population county)
               :prevalence (double (if
                                    (pos?
                                     (:county_population county))
                                     (/
                                      (crime county)
                                      (:county_population county))
                                     0))}))
       (sort-by :prevalence)
       (take-last 10)))