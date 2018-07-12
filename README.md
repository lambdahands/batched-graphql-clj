# Batch GraphQL Clojure Example

A small example of using async batches to resolve GraphQL fields in Clojure
using [lacinia][lacinia] and [grouper][grouper].

[lacinia]: https://github.com/walmartlabs/lacinia
[grouper]: https://github.com/junegunn/grouper

## Setup

Make sure Leiningen is installed. On MacOS, for example: `brew install leiningen`.

Next, setup a running PostgreSQL instance:

```
brew install postgresql
brew services start postgresql
```

Create the example database:

```
createdb graphql_batching
```

## Usage


Run `lein repl` in the source directory.

Once it's up, require and enter the schema namspace:

```clj
(require 'schema)
(ns schema)
```

Reset the database:

```clj
(reset-db)
```

Run a batched query:

```clj
(timed-execute)
```
