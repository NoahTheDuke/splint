default:
    @just --list

repl:
    clojure -M:dev:test:repl/rebel

test *args:
    clojure -M:dev:test:kaocha {{args}}

new-rule arg:
    @clojure -M:new-rule -n {{arg}}

deploy:
    export CLOJARS_USERNAME := "noahtheduke"
    export CLOJARS_PASSWORD := `cat ../clojars.txt`
    clojure -T:build uber
    clojure -T:build deploy
