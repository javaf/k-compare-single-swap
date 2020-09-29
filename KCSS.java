import java.util.*;

// Each Bitonic[2K] consists of 2 Bitonic[K]
// networks followed by a Merger[2K]. The top
// Bitonic[K] is connected to top-half inputs, and
// bottom Bitonic[K] is connected to bottom-half
// inputs. Outputs of both Bitonic[K] are connected
// directly to Merger[2K]. Bitonic[2] networks
// consists of a single Balancer.
class AtomicLong63Array {
  long[] data;
  ThreadLocal<Long> tag;
  ThreadLocal<Long> save;
  // width: number of inputs/outputs

  public AtomicLong63Array(int length) {
    data = new long[length];
    tag = new ThreadLocal<>() {
      protected Long initialValue() {
        return newTag(0);
      }
    };
  }

  // Gets value at index i.
  public long get(int i) {
    return read(i);
  }

  // Sets value at index i.
  public void set(int i, long v) {
    data[i] = newValue(v);
  }

  public boolean compareAndSet
    (int i, long e, long y) {
    return cas(i, newValue(e), newValue(y));
  }

  public boolean compareAndSet
    (int[] i, long[] e, long y) {
    int N = i.length;
    long[] x = new long[N];
    for (int n=0; n<N; n++)
      x[n] = newValue(e[n]);
    return kcss(i, x, y);
  }


  // Performs k-compare-single-swap at indices i.
  // 1. Load linked value at i0.
  // 2. Snapshot values at i1-rest.
  // 3. Check if captured values match expected.
  // 3a. If a value didnt match, restore (fail).
  // 3b. Otherwise, store conditional new value.
  // 3c. Retry if that failed.
  private boolean kcss(int[] i, long[] e, long y) {
    int N = i.length;
    int[] i1 = Arrays.copyOfRange(i, 1, N);
    long[] e1 = Arrays.copyOfRange(e, 1, N);
    while (true) {
      long x0 = ll(i[0]);       // 1
      long[] x1 = snapshot(i1); // 2
      if (x0 != e[0] ||                  // 3
          Arrays.compare(x1, e1) != 0) { // 3
        sc(i[0], x0); // 3a
        return false; // 3a
      }
      if (sc(i[0], y)) return true; // 3b
    } // 3c
  }

  // Collects snapshot at indices i.
  // 1. Collect old tags at i.
  // 2. Collect old values at i.
  // 3. Collect new values at i.
  // 4. Collect new tags at i.
  // 5. Check if both tags and values match.
  // 5a. If they match, return values.
  // 5b. Otherwise, retry.
  private long[] snapshot(int[] i) {
    while (true) {
      long[] ta = collectTags(i);
      long[] va = collectValues(i);
      long[] vb = collectValues(i);
      long[] tb = collectTags(i);
      if (Arrays.compare(ta, tb) == 0 &&
          Arrays.compare(va, vb) == 0)
        return va;
    }
  }

  // Reads tags at indices i.
  // 1. For each index, read its tag.
  private long[] collectTags(int[] i) {
    int N = i.length;
    long[] t = new long[N];
    for (int n=0; n<N; n++) // 1
      t[n] = data[i[n]];  // 1
    return t;
  }

  // Reads values at indices is.
  // 1. For each index, read its value.
  private long[] collectValues(int[] i) {
    int N = i.length;
    long[] v = new long[N];
    for (int n=0; n<N; n++) // 1
      v[n] = read(i[n]);  // 1
    return v;
  }

  // Store conditional if item at i is tag.
  // 1. Try replace tag at i with item.
  private boolean sc(int i, long y) {
    return cas(i, tag.get(), y); // 1
  }

  // Load linked value at i.
  // 1. Increment current tag.
  // 2. Read value at i.
  // 3. Save the value.
  // 4. Try replacing it with tag.
  // 5. Otherwise, retry.
  private long ll(int i) {
    while (true) {
      incTag();
      long x = read(i);
      save.set(x);
      if (cas(i, x, tag.get())) return x;
    }
  }

  // Reads value at i.
  // 1. Get item at i.
  // 2. If its not a tag, return its value.
  // 3. Otherwise, reset it and retry.
  private long read(int i) {
    while (true) {
      long x = data[i];
      if (!isTag(x)) return value(x);
      reset(i);
    }
  }

  // Resets item at i to value.
  // 1. Check if item is a tag.
  // 1a. If so, try replacing with saved value.
  private void reset(int i) {
    long x = data[i];
    if (isTag(x)) cas(i, x, save.get());
  }

  // Simulates CAS operation.
  // 1. Check if expected value is present.
  // 1a. If not present, exit (fail).
  // 1b. Otherwise, update value (success).
  private boolean cas(int i, long e, long y) {
    synchronized (data) {
      if (data[i] != e) return false;
      data[i] = y;
      return true;
    }
  }

  // Increments this thread's tag.
  // 1. Get current tag id.
  // 2. Increment tag id.
  private void incTag() {
    long id = tagId(tag.get()); // 1
    tag.set(newTag(id+1));      // 2
  }


  // Creates new value.
  // 1. Clear b63.
  private long newValue(long v) {
    return (v<<1) >>> 1;
  }

  // Checks if item is a value.
  // 1. Check is b63 is not set.
  private boolean isValue(long x) {
    return x >= 0; // 1
  }

  // Gets value from item (value).
  // 1. Copy sign from b62.
  private long value(long x) {
    return (x<<1) >> 1; // 1
  }

  // Creates a new tag.
  // 1. Set b63.
  // 2. Set thread-id from b62-b48.
  // 3. Set tag-id from b47-b0.
  private long newTag(long id) {
    long th = Thread.currentThread().getId();
    return (1<<63) | (th<<48) | id; // 1, 2, 3
  }

  // Checks if item is a tag.
  // 1. Check if b63 is set.
  private boolean isTag(long x) {
    return x < 0; // 1
  }

  // Gets thread-id from item (tag).
  // 1. Get 15-bits from b62-b48.
  private long threadId(long x) {
    return (x>>>48) & 0x7FFF; // 1
  }

  // Gets tag-id from item (tag).
  // 1. Get 48-bits from b47-b0.
  private long tagId(long x) {
    return x & 0xFFFFFFFFFFFFL; // 1
  }
}
