# Batch GraphQL Clojure Example

A small example of using async batches to resolve GraphQL fields in Clojure
using [lacinia][lacinia] and [grouper][grouper].

[lacinia]: https://github.com/walmartlabs/lacinia
[grouper]: https://github.com/junegunn/grouper

## Setup

### Docker

If you don't have Docker CE installed, you can [download it here](https://download.docker.com/).

Once Docker is installed, you can move to [Usage with Docker](#usage-with-docker).

### Manually

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

## Usage with Docker

### Command Line

```
$ docker-compose run main
```

### REPL

Start the REPL like so

```
$ docker-compose run --service-ports main -A:dev:examples
```

Once it's up, require and enter the schema namspace:

```clj
(require '[postgres.core :as pg])
; default-db can be replaced with a connection string/object
(pg/reset-example pg/default-db)
(pg/run-example pg/default-db)
```

## Usage without Docker

### Command Line

```
$ export DATABASE_URL="<my-postgresql-database-string>"
$ clj -A:examples -e "(require 'postgres.core)(postgres.core/-main)"
```

### REPL

Run `clj -A:dev:examples` in the source directory.

Once it's up, require and enter the schema namspace:

```clj
(require '[postgres.core :as pg])
; default-db can be replaced with a connection string/object
(pg/reset-example pg/default-db)
(pg/run-example pg/default-db)
```
