# ConcurrentSet
This project contains following concurrent set algorithms:

1) LazyList: Heller, S., Herlihy, M., Luchangco, V., Moir, M., Scherer III, W.N. and Shavit, N., 2005. A lazy concurrent list-based set algorithm. In Principles of Distributed Systems (pp. 3-16). Springer Berlin Heidelberg.

2) HarrisLinkedList: Harris, T.L., 2001, October. A pragmatic implementation of non-blocking linked-lists. In DISC (Vol. 1, pp. 300-314).

3) HelpOptimalLFList: Chatterjee, B., Walulya, I. and Tsigas, P. (2016) Help-optimal and Language-portable Lock-free Concurrent Data Structures. Technical report - Department of Computer Science and Engineering, Chalmers University of Technology and Göteborg University, no: 2016-02 ISSN: 1652-926X. 

4) HelpOptimalSimpleLFBST: Chatterjee, B., Walulya, I. and Tsigas, P. (2016) Help-optimal and Language-portable Lock-free Concurrent Data Structures. Technical report - Department of Computer Science and Engineering, Chalmers University of Technology and Göteborg University, no: 2016-02 ISSN: 1652-926X. 

5) HelpOptimalLFBST: Chatterjee, B., Walulya, I. and Tsigas, P. (2016) Help-optimal and Language-portable Lock-free Concurrent Data Structures. Technical report - Department of Computer Science and Engineering, Chalmers University of Technology and Göteborg University, no: 2016-02 ISSN: 1652-926X.

6) HelpOptimalLocalRestartLFBST: Chatterjee, B., Walulya, I. and Tsigas, P. (2016) Help-optimal and Language-portable Lock-free Concurrent Data Structures. Technical report - Department of Computer Science and Engineering, Chalmers University of Technology and Göteborg University, no: 2016-02 ISSN: 1652-926X.

7) NMLFBST: Natarajan, A. and Mittal, N., 2014, February. Fast concurrent lock-free binary search trees. In ACM SIGPLAN Notices (Vol. 49, No. 8, pp. 317-328). ACM.

8) EFRBLFBST: Ellen, F., Fatourou, P., Ruppert, E. and van Breugel, F., 2010, July. Non-blocking binary search trees. In Proceedings of the 29th ACM SIGACT-SIGOPS symposium on Principles of distributed computing (pp. 131-140). ACM.

9) LFSkipList: A wrapper for java.util.concurrent.ConcurrentSkipListSet for comparison purpose.

10) HelpAwareInternalLFBST: A modified version of the lock-free internal BST algorithm of Chatterjee, B., Nguyen, N. and Tsigas, P. (2014) Efficient Lock-free Binary Search Trees in PODC 2014 (p.p. 322-331). 

This is an Apache Ant managed project. To build, run ant in the project directory. For an optimized run, following command containing flags can be used: java -d64 -Xms4G -Xmx8G -cp ./dist/ConcurrentSet.jar se.chalmers.dcs.bapic.concurrentset.test.BenchMark (Assuming that the application is run on a 64-bit environment).

Further, to bind the jvm threads on a NUMA machine, numactl --physcpubind=<nodes> can be used. For further info on numactl see its manpages.  
