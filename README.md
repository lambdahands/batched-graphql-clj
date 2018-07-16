# Batch GraphQL Clojure Example

A small example of using async batches to resolve GraphQL fields in Clojure
using [lacinia][lacinia] and [grouper][grouper].

[lacinia]: https://github.com/walmartlabs/lacinia
[grouper]: https://github.com/junegunn/grouper

## Setup

Make sure Clojure is installed. On MacOS, for example: `brew install clojure`.

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

Run `clj -A:dev:examples` in the source directory.

Once it's up, require and enter the schema namspace:

```clj
(require '[postgres.core :as pg])
; default-db can be replaced with a connection string/object
(pg/reset-example pg/default-db)
(pg/run-example pg/default-db)
```
