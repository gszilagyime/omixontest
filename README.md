# Simple sequence match and demux app.
As performance optimization was not a requirement and as wildcard-aware *KMP* or *Boyer-Moore* are less attractive options, a naive brute-force approach is presented.
Preliminary performance tests show that on the range of K to M sequences and few hundred patterns the run time is in the seconds/minutes range.
Implementing and maintaining the more complex algorithms might be an optimization strategy in the future, as well as doing some performance tests with the C++ Boost library boyer_moore[^1] and knuth_morris_pratt [^2] modules.

# Running
```
./gradlew run --args='input_sequence_file config_file output_dir_prefix alignment_type'
```

# Running the test creator mode
There is an option to generate random test files, with unique sequences. The parameters are the number of lines required and the lenght of each line. Output is generated on standard output.
```
./gradlew run --args='generateRandomTestSequences 10000 128' -q > 1K_random.seq
```

#Packaging
```
./gradlew build
```
This will generate 2 archives (app.tar and app.zip) in the app/build/distributions folder. Unpacking one of the archives on a computer and then running
```
scripts/app
```
should run the application in a standalone manner without the gradle build tool.


[^1]: https://www.boost.org/doc/libs/1_86_0/libs/algorithm/doc/html/the_boost_algorithm_library/Searching/KnuthMorrisPratt.html
[^2]: https://www.boost.org/doc/libs/1_86_0/libs/algorithm/doc/html/algorithm/Searching.html#the_boost_algorithm_library.Searching.BoyerMoore
