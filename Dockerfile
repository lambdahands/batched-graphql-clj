FROM clojure:tools-deps-alpine
MAINTAINER Philip Diaz "lambdahands@gmail.com"
ENV REFRESHED_AT 2018-07-17

ADD . /volume

VOLUME ["/volume"]

WORKDIR /volume

# Is there a better way to resolve dependencies than evaluating a blank string?
RUN clojure -R:dev:examples -e ""

ENTRYPOINT ["clojure"]
CMD ["-A:examples", "-e", "(require 'postgres.core)(postgres.core/-main)"]
