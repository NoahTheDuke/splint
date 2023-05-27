## Thoughts on performance

```
$ tokei
===============================================================================
 Language            Files        Lines         Code     Comments       Blanks
===============================================================================
 Clojure               169       113620       102756         4266         6598
 ClojureC                4         1012          850           36          126
 ClojureScript          48        12666        11649          142          875
 ```

### v1.3.0

```
$ time clojure -M:run ../netrunner/ --quiet
Linting took 5998ms, 704 style warnings

real	0m9.442s
user	1m14.559s
sys	0m0.904s
```

### v1.4.0

```
$ time clojure -M:run ../netrunner/ --quiet
Linting took 6181ms, 704 style warnings

real	0m9.528s
user	1m14.586s
sys	0m0.829s
```

### v1.5.0

```
$ time clojure -M:run ../netrunner/ --quiet
Linting took 6075ms, 701 style warnings

real	0m10.280s
user	1m17.039s
sys	0m1.074s
```

### v1.6.1

```
$ time clojure -M:run ../netrunner/ --quiet
Linting took 6422ms, 701 style warnings

real	0m10.391s
user	1m19.913s
sys	0m1.111s
```

### v1.7.0

```
$ time clojure -M:run ../netrunner/ --quiet
Linting took 6740ms, 779 style warnings

real	0m11.542s
user	1m30.477s
sys	0m1.333s
```

### main (9eb758a)

```
$ time clojure -M:run ../netrunner/ --quiet
Linting took 6457ms, 779 style warnings

real	0m11.288s
user	1m25.527s
sys	0m1.224s
```
