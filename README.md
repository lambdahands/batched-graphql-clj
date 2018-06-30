# Batch GraphQL Clojure Example

A small example of using async batches to resolve GraphQL fields in Clojure
using [lacinia][lacinia] and [grouper][grouper].

[lacina]: https://github.com/walmartlabs/lacinia
[grouper]: https://github.com/junegunn/grouper

## Usage

Make sure Leiningen is installed. On MacOS, for example: `brew install leiningen`.

Run `lein repl` in the source directory.

Once it's up, run:

    (require 'schema)
    (ns schema)
    (timed-execute)

This runs a simple example showing the implementation in `src/schema.clj`.
