default:
    @just --list

today := `date +%F`
current_version := `cat resources/noahtheduke/splint/SPLINT_VERSION | xargs`
project := 'io.github.noahtheduke/splint'

flow-storm:
    clojure -Sforce -Sdeps '{:deps {com.github.jpmonettas/flow-storm-dbg {:mvn/version "RELEASE"}}}' -X flow-storm.debugger.main/start-debugger :port 31401 :debugger-host '"host.docker.internal"' :styles '"/home/noah/.config/flow-storm/big-fonts.css"'

clean:
    rm -rf classes
    mkdir classes

compile: clean
    clojure -M -e "(compile 'noahtheduke.splint)"

repl arg="":
    clojure -M:dev:test:v1.12{{arg}}:repl

dev-run *args:
    clojure -M:dev:test:run {{args}}

run *args:
    clojure -M:run {{args}}

format *args="check":
    clojure -M:cljfmt {{args}}

clj-kondo:
    clj-kondo --parallel --lint dev src test

clojure-lsp:
    clojure-lsp diagnostics

[no-exit-message]
@test-raw *args:
    clojure -M:dev:test:runner --md README.md {{args}}

[no-exit-message]
test *args="--output summary":
    just clojure-lsp
    bb run splint
    bb run lazytest -e :integration {{args}}
    just test-raw -e :integration {{args}}

[no-exit-message]
test-all-versions *args="--output summary":
    just clojure-lsp
    bb run splint
    bb run lazytest {{args}}
    clojure -M:v1.10:dev:test:runner --md README.md {{args}}
    clojure -M:v1.11:dev:test:runner --md README.md {{args}}
    clojure -M:v1.12:dev:test:runner --md README.md {{args}}

@new-rule arg:
    clojure -M:new-rule -n {{arg}}

@gen-docs:
    clojure -M:gen-docs
    bb scripts/gen_toc.clj

@version:
    echo '{{current_version}}'

# Set version, change all instances of <<next>> to version
@set-version version:
    echo '{{version}}' > resources/noahtheduke/splint/SPLINT_VERSION
    fd '.(clj|edn|md)' . -x sd '<<next>>' '{{version}}' {}
    sd '{{current_version}}' '{{version}}' README.md
    sd '{{current_version}}' '{{version}}' docs/installation.md
    sd '{{current_version}}' '{{version}}' docs/usage.md
    sd '## Unreleased' '## Unreleased\n\n## {{version}} - {{today}}' CHANGELOG.md

@clojars:
    env CLOJARS_USERNAME='noahtheduke' CLOJARS_PASSWORD=`cat ../clojars.txt` clojure -T:build deploy

inform-cljdoc version=current_version:
    curl -X POST \
        -d project={{project}} \
        -d version={{version}} \
        https://cljdoc.org/api/request-build2

# Builds the uberjar, builds the jar, sends the jar to clojars
@release version:
    echo 'Running tests'
    just test-all-versions --output quiet
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
    echo 'Informing cljdoc'
    just inform-cljdoc {{version}}
    echo "Don't forget to cut a release on Github!"

install:
    clojure -T:build jar
    clojure -T:build install

run-cljdoc:
    docker run --rm \
      --volume $(pwd):{{invocation_directory_native()}} \
      --volume "$HOME/.m2:/root/.m2" \
      --volume /tmp/cljdoc:/app/data \
      --entrypoint clojure \
      cljdoc/cljdoc -Sforce -M:cli ingest \
        --project {{project}} \
        --version {{current_version}} \
        --git {{invocation_directory_native()}} \
        --rev $(git rev-parse HEAD)
