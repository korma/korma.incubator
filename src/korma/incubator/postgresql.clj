(ns korma.incubator.postgresql
  (:use [korma.sql.engine :only [infix]]))

(defn ilike [k v]
  (infix k "ILIKE" v))
