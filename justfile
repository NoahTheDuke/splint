default:
    @just --list

repl:
    clojure -M:dev:test:lib/reloaded:repl/rebel

test *args:
    clojure -M:test {{args}}

export CLOJARS_USERNAME := "noahtheduke"
export CLOJARS_PASSWORD := `cat ../clojars.txt`

deploy:
    clojure -T:build deploy
