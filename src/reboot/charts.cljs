(ns reboot.charts
  (:require
   [cljsjs.c3]))


(defn xdates
  [data]
  (let [d (mapcat keys data)]
    (into ["x"]
          (map #(subs % 0  10) d))))

(defn xdata
  [data key]
  (into [(name key)] (map key (mapcat vals data))))

(defn build
  [data]
  (let [sdata (sort-by first data)
        config {"bindto" "#chart"
                "data" {"x" "x"
                        "xFormat" "%Y-%m-%d"
                        "columns" [
                                   (xdates sdata)
                                   (xdata sdata :avg-power)
                                   (xdata sdata :avg-hr)
                                   (xdata sdata :max-power)
                                   (xdata sdata :max-hr)]}
                "axis" {"x" {"type:" "timeseries"
                             "tick:" {"format" "%Y-%m-%d"}}}}]
    (-> js/c3
        (.generate (clj->js config)))))


(comment
  
  )
