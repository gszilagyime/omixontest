_Simple sequence match and demux app._
As performance was optimization was not a requirement and as wildcard-aware *KMP* or *Boyer-Moore* are less attractive options, a naive brute-force approach is presented.
Preliminary performance tests show that on the range of K to M sequences and few hundred patterns the run time is in the seconds/minutes range.
Implementing and maintaining the more complex algorithms might be an optimization strategy in the future, as well as doing some performance tests with the C++ Boost library boyer_moore and knuth_morris_pratt modules.
