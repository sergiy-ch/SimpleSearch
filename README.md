# SimpleSearch

Rank calculation
---------------

If `inputString` is one word, ranking is simple: 100% match if file contains inputString word, 0% match if not.

If `inputString` is a sentence, ranking = longest matching sub-sentence compared to original inputString sentence.

Examples:

File: `quick brown fox jumps`

`zzz zzz zzzz` - 0% match (0 of 3 words match)

`zzz fox zzzz` - 33% match (1 of 3 words match)

`fox jumps zzzz` - 66% match (2 of 3 words match)

`brown fox` - 100% match (2 of 2 words match)

`brown fox jumps` - 100% match (3 of 3 words match)

`fox quick jumps` - 33% match (wrong order, although file contains all 3 words, they are in different order, therefore searched separately)

and so on...


Running application
---------------

To run app:
```
sbt
> run foo/
```

To run tests:
```
sbt
> test
```

To exit app please enter `:quit` command.
