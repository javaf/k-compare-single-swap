k-compare single-swap (KCSS) is an extension of CAS
that enables atomically checking multiple addresses
before making an update.

AtomicLong63Array provides an array capable of
storing 63-bit long values, and supports basic
accessor functions like `get()`, `set()` and atomic
operations like `compareAndSet()` with either one
comparision or k-comparisions. The main purpose
of this class is to demonstrate k-compare
single-swap (KCSS).

> **Course**: [Concurrent Data Structures], Monsoon 2020\
> **Taught by**: Prof. Govindarajulu Regeti

[Concurrent Data Structures]: https://github.com/iiithf/concurrent-data-structures

```java
get(i):
Gets value at index i.
1. Read value at i (reset if needed).
```

```java
set(i, v):
Sets value at index i.
1. Convert target value to marked value.
2. Store into internal array.
```

```java
compareAndSet(i, e, y):
Performs compare-and-set at index i.
1. Convert expected value to marked value.
2. Convert target value to marked value.
3. Perform CAS.
```

```java
compareAndSet(i[], e[], y):
Performs k-compare-and-set at indices i.
1. Convert expected values to marked values.
2. Convert target value to marked value.
3. Perform KCSS.
```

```java
-kcss(i[], e[], y):
Performs k-compare-single-swap at indices i.
1. Load linked value at i0.
2. Snapshot values at i1-rest.
3. Check if captured values match expected.
3a. If a value didnt match, restore (fail).
3b. Otherwise, store conditional new value.
3c. Retry if that failed.
```

```java
-snapshot(i[], i0, i1, V[]):
Collects snapshot at indices i.
1. Collect old tags at i.
2. Collect old values at i.
3. Collect new values at i.
4. Collect new tags at i.
5. Check if both tags and values match.
5a. If they match, return values.
5b. Otherwise, retry.
```

```java
-collectTags(i[], i0, i1, T[]):
Reads tags at indices i.
1. For each index, read its tag.
```

```java
-collectValues(i[], i0, i1, V[]:
Reads values at indices is.
1. For each index, read its value.
```

```java
-sc(i, y):
Store conditional if item at i is tag.
1. Try replace tag at i with item.
```

```java
-ll(i):
Load linked value at i.
1. Increment current tag.
2. Read value at i.
3. Save the value.
4. Try replacing it with tag.
5. Otherwise, retry.
```

```java
-read(i):
Reads value at i.
1. Get item at i.
2. If its not a tag, return its value.
3. Otherwise, reset it and retry.
```

```java
-reset(i):
Resets item at i to value.
1. Check if item is a tag.
1a. If so, try replacing with saved value.
```

```java
-cas(i, e, y):
Simulates CAS operation.
1. Check if expected value is present.
1a. If not present, exit (fail).
1b. Otherwise, update value (success).
```

```java
-incTag():
Increments this thread's tag.
1. Get current tag id.
2. Increment tag id.
```

```bash
## OUTPUT
Starting 25 threads without KCSS ...
5: done
6: done
20: done
1: done
9: done
8: done
3: done
2: done
22: done
18: done
16: done
21: done
4: done
13: done
0: done
11: done
12: done
17: done
14: done
10: done
19: done
7: done
24: done
15: done
23: done
{19, 25, 24, 24, 24, 24, 24, 24, 24, 24}
Updates were atomic? false

Starting 25 threads with KCSS ...
0: done
1: done
2: done
3: done
5: done
4: done
6: done
7: done
8: done
9: done
10: done
19: done
18: done
17: done
16: done
15: done
14: done
13: done
12: done
11: done
21: done
20: done
22: done
23: done
24: done
{25, 25, 25, 25, 25, 25, 25, 25, 25, 25}
Updates were atomic? true
```

See [AtomicLong63Array.java] for code, [Main.java] for
test, and [repl.it] for output.

[AtomicLong63Array.java]: https://repl.it/@wolfram77/k-compare-single-swap#AtomicLong63Array.java
[Main.java]: https://repl.it/@wolfram77/k-compare-single-swap#Main.java
[repl.it]: https://k-compare-single-swap.wolfram77.repl.run


### references

- [Nonblocking k-compare-single-swap :: V. Luchangco, M. Moir, and N. Shavit](https://dl.acm.org/doi/10.1145/777412.777468)

![](https://ga-beacon.deno.dev/G-G1E8HNDZYY:v51jklKGTLmC3LAZ4rJbIQ/github.com/javaf/k-compare-single-swap)
![](https://ga-beacon.deno.dev/G-G1E8HNDZYY:v51jklKGTLmC3LAZ4rJbIQ/github.com/moocf/k-compare-single-swap.java)
