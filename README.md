# korma.incubator

A playground for potential new features and nice utilities for korma.

## Usage

In a leiningen project add:

```clojure
[korma.incubator "0.1.1-SNAPSHOT"]
```

For existing features, please see https://github.com/ibdknox/Korma or
http://sqlkorma.com/docs.

## New Features

### Many-to-Many Relationships

Many-to-many relationships are typically implemented using a join table that
contains foreign keys that reference both tables, and these relationships are
expected to be implemented in this way in Korma.  In the following example,
two entities, `foo` and `bar`, are defined with a many-to-many relationship
between them using the join table `foo_bar`.

```clojure
;; Entities with many-to-many relationships.
(declare foo bar)

(defentity foo
  (entity-fields :baz)
  (many-to-many bar :foo_bar
                {:lfk :foo_id
                 :rfk :bar_id}))

(defentity bar
  (entity-fields :quux)
  (many-to-many foo :foo_bar
                {:lfk :bar_id
                 :rfk :baz_id}))


;; Retrieving entities in many-to-many relationships.
(select foo
  (with bar))
```

The first argument to the macro, `many-to-many`, macro is the name of the
foreign entity.  The second argument is the name of the join table as a
keyword.  The third argument is a map containing the names of the foreign keys
in the join table.  The keyword, `:lfk`, refers to the "left-hand foreign
key."  That is, the column in the join table that refers to the primary key of
the current entity.  The keyword, `:rfk`, refers to the "right-hand foreign
key."  That is, the column in the join table that refers to the primary key of
the foreign entity.

### Retrieving Entities in Has-One and Belongs-To Relationships Separately

By default, Korma, returns the columns of foreign entities in has-one and
belongs-to relationships as keys within the map belonging to the local
entity.  Sometimes, it's convenient to be able to retrieve the entity as a
separate map.  The macro, `with-object`, provides a simple way to do this.

```clojure
;; Entities in a one-to-many relationship.
(declare foo bar)

(defentity foo
  (entity-fields :baz)
  (has-one bar))

(defentity bar
  (entity-fields :quux)
  (haz-one foo))

;; Retrieve an entity in a has-one relationship separately.
(select foo
  (with-object bar))

;; Retrieve an entity in a belongs-to relationship separately.
(select bar
  (with-object foo))
```

The entities are defined just like thay would be in any one-to-many
relationship, but `with-object` is used instead of `with` in the query.

## License

Copyright (C) 2011 Chris Granger

Distributed under the Eclipse Public License, the same as Clojure.
