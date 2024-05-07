# Contributing

If you want to contribute to Splint, you have my gratitude. Let's make sure we set you up for success.

NOTE: I (Noah) am writing this from my perspective. If Splint ever takes off, we can change this to be less conversational lol.

## Issues

The primary method of contributing is through opening issues on Github. Bug reports, feature requests, suggestions or ideas are all welcome. I only ask that you give grace to all, you remember the human on the other side of the screen, and you take your time to really think through what you want to say before you say it.

## Documentation

I am not a good writer but I have done my best to be thorough and helpful in these docs. I welcome anyone's help to make them more clear or more friendly or more descriptive. You'd be surprised how easy it is to forget to document important features!

## Code

Bug fixes, especially those with existing issues, are free game. Comment that you're attemping the fix so no one doubles up on the effort, and then go off. PRs are welcome and I am generally a quick reviewer.

If you have an idea for a new rule, please start with an issue so we can discuss it before you put in the effort. That way we can hash out names and options, and gutcheck obvious edge cases before diving into the work. Additionally, linking to existing blog posts, Stack Overflow answers, Clojureverse posts, or other places where a given rule has been discussed in public will help guide us to finding a community-based solution.

Once we've decided it's worth persuing, you'll need to follow these steps:

1) Use `clojure -M:new-rule -n GENRE/RULE-NAME` to create a stub rule file and test file and insert stub into default config file.
2) Fill out both appropriately, including doc strings.
3) Add the rule's fully-qualified name to `src/noahtheduke/splint.clj` in the second `:require` block in the right place.
4) Open `resources/config/default.edn` and move the new rule config to the right location in the file. Add a short description of the rule to `:description` to help users when they auto-generate a `.splint.edn` file.
5) Run the full test suite with `clojure -M:dev:test:runner`, update the integration tests as necessary. Inspect their output if you wish to verify that the new rule works correctly.
