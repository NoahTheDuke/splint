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
    clojure -M:dev:test{{arg}}:repl

dev-run *args:
    clojure -M:dev:test:run {{args}}

run *args:
    clojure -M:run {{args}}

format *args="check":
    clojure -M:cljfmt {{args}}

[no-exit-message]
test *args:
    clj-kondo --parallel --lint dev src test
    bb run
    just format
    clojure -M:dev:test:runner -e :integration {{args}}

[no-exit-message]
test-all *args:
    clj-kondo --parallel --lint dev src test
    bb run
    just format
    clojure -M:dev:test:runner {{args}}

@new-rule arg:
    clojure -M:new-rule -n {{arg}}

@gen-docs:
    clojure -M:gen-docs

today := `date +%F`

# Set version, change all instances of <<next>> to version
set-version version:
    echo '{{version}}' > resources/SPLINT_VERSION
    fd '.(clj|edn|md)' . -x sd '<<next>>' '{{version}}' {}
    sd '## Unreleased' '## Unreleased\n\n## {{version}} - {{today}}' CHANGELOG.md

@clojars:
    env CLOJARS_USERNAME='noahtheduke' CLOJARS_PASSWORD=`cat ../clojars.txt` clojure -T:build deploy

# Builds the uberjar, builds the jar, sends the jar to clojars
@deploy version:
    echo 'Running tests'
    just test-all
    echo 'Setting new version {{version}}'
    just set-version {{version}}
    echo 'Rendering docs'
    just gen-docs
    echo 'Commit and tag'
    git commit -a -m 'Bump version for release'
    git tag v{{version}}
    echo 'Pushing to github'
    git push
    git push --tags
    echo 'Building uber'
    clojure -T:build uber
    echo 'Deploying to clojars'
    just clojars
