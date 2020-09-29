Bitonic network is a balanced counting network that
allows processes to decompose operations, like
counting, and reduce memory contention.

```java
Balancer:
A Balancer is a simple switch with two input
and two output wires. Tokens arrive on the
Balancer’s input wires at arbitrary times,
and emerge on their output wires, at some later
Given a stream of input tokens, it sends one
token to the top output wire, and the next to
the bottom, and so on, effectively balancing the
number of tokens between the two wires.

Balancer.traverse():
1. Flip balancer state.
2. Return the updated state.
```

```java
MergerNetwork:
Each Merger[2K] consists of 2 Merger[K] networks
followed by a layer of Balancers. The top
Merger[K] takes even inputs from top-half and
odd inputs from bottom-half. The bottom
Merger[K] takes odd inputs from top-half and
even inputs form bottom half. The respective
output of each Merger[K] is then combined with
a Balancer. When K is 1, Merger[2K] is a single
Balancer.

MergerNetwork.traverse():
1. Find connected Merger[K].
2. Traverse connected Merger[K].
3. Traverse connected balancer.
```

```java
BitonicNetwork:
Each Bitonic[2K] consists of 2 Bitonic[K]
networks followed by a Merger[2K]. The top
Bitonic[K] is connected to top-half inputs, and
bottom Bitonic[K] is connected to bottom-half
inputs. Outputs of both Bitonic[K] are connected
directly to Merger[2K]. Bitonic[2] networks
consists of a single Balancer.

BitonicNetwork.traverse():
1. Find connected Bitonic[K].
2. Traverse connected Bitonic[K].
3. Traverse connected Merger[2K].
```

See [Balancer.java], [MergerNetwork.java],
[BitonicNetwork.java] for code, [Main.java] for
test, and [repl.it] for output.

[Balancer.java]: https://repl.it/@wolfram77/bitonic-network#Balancer.java
[MergerNetwork.java]: https://repl.it/@wolfram77/bitonic-network#MergerNetwork.java
[BitonicNetwork.java]: https://repl.it/@wolfram77/bitonic-network#BitonicNetwork.java
[Main.java]: https://repl.it/@wolfram77/bitonic-network#Main.java
[repl.it]: https://bitonic-network.wolfram77.repl.run


### references

- [The Art of Multiprocessor Programming :: Maurice Herlihy, Nir Shavit](https://dl.acm.org/doi/book/10.5555/2385452)
