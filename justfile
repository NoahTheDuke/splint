default:
    @just --list

flow-storm:
    clojure -Sforce -Sdeps '{:deps {com.github.jpmonettas/flow-storm-dbg {:mvn/version "RELEASE"}}}' -X flow-storm.debugger.main/start-debugger :port 31401 :debugger-host '"host.docker.internal"' :styles '"/home/noah/.config/flow-storm/big-fonts.css"'

clean:
    rm -rf classes
    mkdir classes

compile: clean
    clojure -M -e "(compile 'noahtheduke.splint)"

repl arg="":
    clojure -M:dev:test{{arg}}:repl/rebel

dev-run *args:
    clojure -M:dev:test:run {{args}}

run *args:
    clojure -M:run {{args}}

[no-exit-message]
test *args:
    clj-kondo --parallel --lint dev src test
    bb run dev src test
    clojure -M:dev:test:runner {{args}}

@new-rule arg:
    clojure -M:new-rule -n {{arg}}

@gen-docs:
    clojure -M:gen-docs

# Set version, change all instances of <<next>> to version
@set-version version:
    echo '{{version}}' > resources/SPLINT_VERSION
    fd '.(clj|edn|md)' . -x sd '<<next>>' '{{version}}' {}

# Builds the uberjar, builds the jar, sends the jar to clojars
@deploy version:
    echo 'Running tests'
    just test
    echo 'Setting new version {{version}}'
    just set-version {{version}}
    echo 'Rendering docs'
    just gen-docs
    echo 'Update changelog'
    # TODO
    git commit -a -m 'Bump version for release'
    git tag v{{version}}
    git push
    git push --tags
    echo 'Building uber'
    clojure -T:build uber
    echo 'Deploying to clojars'
    env CLOJARS_USERNAME='noahtheduke' CLOJARS_PASSWORD=`cat ../clojars.txt` \
        clojure -T:build deploy
    echo 'Building native image'
    scripts/compile
