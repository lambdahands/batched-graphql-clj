(ns postgres.schema)

(def objects
  {:person {:fields {:id      {:type 'Int}
                     :friends {:type    '(list :person)
                               :resolve ::friends
                               :batch   :batch/person}}}})
;                              ^^^^^^
;                              adding/removing this key has a
;                              significant impact on performance!

(def queries
  {:people {:type    '(list :person)
            :args    {:id {:type 'Int}}
            :resolve ::people
            :batch   :batch/people}})

(def schema
  {:objects objects
   :queries queries})
