default:
    @just --list

repl:
    clojure -M:dev:test:repl/rebel

test *args:
    clojure -M:dev:test:runner {{args}}

new-rule arg:
    @clojure -M:new-rule -n {{arg}}

deploy:
    clojure -T:build uber
    env CLOJARS_USERNAME="noahtheduke" CLOJARS_PASSWORD=`cat ../clojars.txt` \
        clojure -T:build deploy
