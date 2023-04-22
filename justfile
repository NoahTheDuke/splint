default:
    @just --list

flow-storm:
    clojure -Sforce -Sdeps '{:deps {com.github.jpmonettas/flow-storm-dbg {:mvn/version "RELEASE"}}}' -X flow-storm.debugger.main/start-debugger :port 31401 :debugger-host '"host.docker.internal"' :styles '"/home/noah/.config/flow-storm/big-fonts.css"'

repl arg="":
    clojure -M:dev:test{{arg}}:repl/rebel

test *args:
    clojure -M:dev:test:runner {{args}}

new-rule arg:
    @clojure -M:new-rule -n {{arg}}

deploy:
    clojure -T:build uber
    env CLOJARS_USERNAME="noahtheduke" CLOJARS_PASSWORD=`cat ../clojars.txt` \
        clojure -T:build deploy
