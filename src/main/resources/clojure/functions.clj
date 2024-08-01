(ns clojure.functions)

(defn add-numbers [a b]
  (+ a b))

(defn subtract-numbers [a b]
  (- a b))

(defn multiply-numbers [a b]
  (* a b))

(defn divide-numbers [a b]
  (if (zero? b)
    (throw (IllegalArgumentException. "Division by zero"))
    (/ a b)))

(defn sqrt-number [a]
  (if (neg? a)
    (throw (IllegalArgumentException. "Square root of negative number"))
    (Math/sqrt a)))

(defn generate-random-string []
  (slurp "https://www.random.org/strings/?num=1&len=10&digits=on&upperalpha=on&loweralpha=on&unique=on&format=plain&rnd=new"))
