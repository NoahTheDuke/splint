# Contributing

I'm putting this at the top because it's real important to me: Do not use any generative AI when contributing to this project in any form. This is on the honor system but I'm trusting you here. Don't fuckin do it.

If you want to contribute to Splint, you have my gratitude. Let's make sure we set you up for success.

* Issues are always welcome, a good way to help discover edge cases and incorrect impls.
* Bug fixes and small touch ups can be put in a PR, no problem. (Documentation updates are also fair game.)
* For anything bigger, I prefer to have an issue first so we can discuss the scope and nature of the bug, feature, or idea you have.

## Issues

The primary method of contributing is through opening issues on Github. Bug reports, feature requests, suggestions or ideas are all welcome. I only ask that you give grace to all, you remember the human on the other side of the screen, and you take your time to really think through what you want to say before you say it.

Things to think about:

* Check that the issue doesn't already exist.
* Check that the issue hasn't already been fixed (but not released yet).
* Keep your issue focused on a problem statement and associated solutions. General discussions are better suited for Clojurian slack.
* If writing about a bug, include the version you're using.

## Code

Bug fixes, especially those with existing issues, are free game. Comment that you're attemping the fix so no one doubles up on the effort, and then go off. Small PRs are great and I am generally a quick reviewer.

* Keep whitespace changes minimal.
* Follow the existing code style in the project.
* Add relevant tests.
* Keep your commits clean and self-contained. No `wip` or `fixes` commits.

Test helpers are welcome if you think you'll use them more than twice. This project uses [Lazytest](https://github.com/NoahTheDuke/lazytest) which is a fundamentally different testing paradigm than with clojure.test; double check your work.

PRs are not squashed, so counter to the classic [Utter Disregard For Git Commit History](https://zachholman.com/posts/git-commit-history/), the best course of action is active rebasing to laser-target each commit. If messing with git history is too hard, may I suggest [jujutsu](https://jj-vcs.github.io/jj/latest/)?

### Adding a new rule

If you have an idea for a new rule, please start with an issue so we can discuss it before you put in the effort. That way we can hash out names and options, and gutcheck obvious edge cases before diving into the work. Additionally, linking to existing blog posts, Stack Overflow answers, Clojureverse posts, or other places where a given rule has been discussed in public will help guide us to finding a community-based solution.

Once we've decided it's worth persuing, you'll need to follow these steps:

1) Use `clojure -M:new-rule -n GENRE/RULE-NAME` to create a stub rule file and test file and insert stub into default config file.
2) Fill out both appropriately, including doc strings.
3) Open `resources/noahtheduke/splint/config/default.edn` and move the new rule config to the right location in the file. Add a short description of the rule to `:description` to help users when they auto-generate a `.splint.edn` file.
4) Add the rule's fully-qualified name to `src/noahtheduke/splint.clj` in the second `:require` block in the right place.
5) Run the full test suite with `clojure -M:dev:test:runner`, updating the integration tests as necessary. Inspect their output if you wish to verify that the new rule works correctly.
