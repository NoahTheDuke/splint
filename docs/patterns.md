# Patterns

Literals are treated as their value, so `nil`, booleans, numbers of all supported kinds, keywords, and symbols are compared with `=`. Collections are deconstructed and compared one element at a time, each element either a literal or further deconstructed. Some symbols or symbol prefixes have special meaning: `_` is treated as "any value", a symbol starting with a question mark is a binding (`?pred` will match any value and store the result), a symbol starting with a percent sign is a predicate that will only match if the function is truthy (`%keyword?` will match any keyword).
