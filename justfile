default:
    @just --list

repl:
    clojure -M:dev:test:repl/rebel

test *args:
    clojure -M:dev:test:kaocha {{args}}

new-rule arg:
    @clojure -M:new-rule -n {{arg}}

export CLOJARS_USERNAME := "noahtheduke"
export CLOJARS_PASSWORD := `cat ../clojars.txt`

deploy:
    clojure -T:build uber
    clojure -T:build deploy
