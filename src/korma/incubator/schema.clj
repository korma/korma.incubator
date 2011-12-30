(ns korma.incubator.schema
  (:require [korma.sql.utils :as utils])
  (:use [korma.core :only [empty-query exec raw]]
        [korma.sql.engine :only [->sql table-str delimit-str do-wrapper map-val 
                                 str-value bind-params]]))

(defn empty-schema []
  {:columns []
   :constraints []
   :attrs []})

(defn schema* [ent schema]
  (assoc ent :schema schema))

(defmacro schema [ent & body]
  `(let [s# (-> (empty-schema)
                ~@body)] 
     (schema* ~ent s#)))

(defn column [schema name type & attrs]
  (update-in schema [:columns] conj {:col name
                                     :type type
                                     :attrs attrs}))

(defn attr [type]
  (let [as {:pk "PRIMARY KEY"
            :not-null "NOT NULL"
            :auto "AUTO_INCREMENT"
            :serial "SERIAL"}]
    (raw (get as type (name type)))))

(defn create-query [ent]
  (merge (empty-query ent)
         {:type :create
          :columns (get-in ent [:schema :columns] [])}))

(defn create! [ent]
  (-> (create-query ent)
      (exec)))

(defn drop-query [ent]
  (merge (empty-query ent)
         {:type :drop}))

(defn drop! [ent]
  (-> (drop-query ent)
      (exec)))

(defn alter-query [ent]
  (merge (empty-query ent)
         {:type alter
          :columns []}))

(defn alter! [ent action params]
  (let [query (alter-query ent)
        query (condp = action
                :add (apply column query params)
                :drop (assoc query :drop (first params))
                :modify (assoc query :alter params)
                (throw (Exception. (str "Unknown alteration: " action))))]
    (exec query)))

(comment
(defentity users
  (schema
    (column :name [:varchar 256] :unique)
    (column :cool [:text] :unique)))
 

  (create! users)

  (alter! users :drop [:chris])
  (alter! users :add [:chris [:varchar 256] :not-null])
  (alter! users :modify [:chris :default "hey"])

  (drop! users)
  )

;;***************************************************
;;
;;***************************************************

(defn column-clause [{:keys [type col attrs] :as column}]
  (let [field (delimit-str (name col))
        field-type (cond 
                     (vector? type) (do-wrapper (name (first type)) (second type))
                     (map? type) (map-val type)
                     :else (name type))
        attr-clause (utils/space (for [v attrs] 
                                   (if (keyword? v) 
                                     (name v)
                                     (str-value v))))
        clause (utils/space [field field-type attr-clause])]
    clause))

(defn sql-create [query]
  (let [neue-sql (str "CREATE TABLE " (table-str query) " ")]
    (assoc query :sql-str neue-sql)))

(defn sql-drop [query]
  (let [neue-sql (str "DROP TABLE " (table-str query))]
    (assoc query :sql-str neue-sql)))

(defn sql-columns [query]
  (let [clauses (map column-clause (:columns query))
        clauses-str (utils/wrap-all clauses)]
    (update-in query [:sql-str] str clauses-str)))

;;***************************************************
;;
;;***************************************************

(defmethod ->sql :create [query]
  (bind-params
    (-> query
        (sql-create)
        (sql-columns))))

(defmethod ->sql :alter [query]
  (bind-params
    (-> query
        )))

(defmethod ->sql :drop [query]
  (bind-params
    (-> query
        (sql-drop))))

