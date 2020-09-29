import java.util.*;

// k-compare single-swap (KCSS) is an extension of
// CAS that enables atomically checking multiple
// addresses before making an update.
// 
// AtomicLong63Array provides an array capable of
// storing 63-bit long values, and supports basic
// accessor functions like get(), set() and atomic
// operations like compareAndSet() with either one
// comparision or k-comparisions. The main purpose
// of this class is to demonstrate k-compare
// single-swap (KCSS).
class AtomicLong63Array implements Iterable<Long> {
  static final int MAX_THREAD_ID = 1024;
  long[] data;
  long[] tag;
  long[] save;
  // data: contains marked values / tags
  // tag: current tag of respective thread
  // save: current saved value of respective thread

  public AtomicLong63Array(int length) {
    int T = MAX_THREAD_ID;
    data = new long[length];
    tag = new long[T];
    save = new long[T];
  }

  // Gets value at index i.
  // 1. Read value at i (reset if needed).
  public long get(int i) {
    return read(i);
  }

  // Sets value at index i.
  // 1. Convert target value to marked value.
  // 2. Store into internal array.
  public void set(int i, long v) {
    data[i] = newValue(v);
  }

  // Performs compare-and-set at index i.
  // 1. Convert expected value to marked value.
  // 2. Convert target value to marked value.
  // 3. Perform CAS.
  public boolean compareAndSet
    (int i, long e, long y) {
    return cas(i, newValue(e), newValue(y)); // 1, 2, 3
  }

  // Performs k-compare-and-set at indices i.
  // 1. Convert expected values to marked values.
  // 2. Convert target value to marked value.
  // 3. Perform KCSS.
  public boolean compareAndSet
    (int[] i, long[] e, long y) {
    int I = i.length;        // 1
    long[] x = new long[I];  // 1
    for (int o=0; o<I; o++)  // 1
      x[o] = newValue(e[o]); // 1
    return kcss(i, x, newValue(y)); // 2, 3
  }


  // Performs k-compare-single-swap at indices i.
  // 1. Load linked value at i0.
  // 2. Snapshot values at i1-rest.
  // 3. Check if captured values match expected.
  // 3a. If a value didnt match, restore (fail).
  // 3b. Otherwise, store conditional new value.
  // 3c. Retry if that failed.
  private boolean kcss(int[] i, long[] e, long y) {
    int I = i.length;
    long[] x = new long[I];
    while (true) {
      x[0] = ll(i[0]);      // 1
      snapshot(i, 1, I, x); // 2
      if (Arrays.compare(x, e) != 0) { // 3
        sc(i[0], x[0]); // 3a
        return false;   // 3a
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
  private void snapshot
    (int[] i, int i0, int i1, long[] V) {
    int I = i.length;
    long[] va = V;
    long[] vb = new long[I];
    long[] ta = new long[I];
    long[] tb = new long[I];
    do {
      collectTags(i, i0, i1, ta);
      collectValues(i, i0, i1, va);
      collectValues(i, i0, i1, vb);
      collectTags(i, i0, i1, tb);
    } while(
      Arrays.compare(ta, i0, i1, tb, i0, i1) != 0 ||
      Arrays.compare(va, i0, i1, vb, i0, i1) != 0
    );
  }

  // Reads tags at indices i.
  // 1. For each index, read its tag.
  private void collectTags
    (int[] i, int i0, int i1, long[] T) {
    for (int o=i0; o<i1; o++) // 1
      T[o] = data[i[o]];    // 1
  }

  // Reads values at indices is.
  // 1. For each index, read its value.
  private void collectValues
    (int[] i, int i0, int i1, long[] V) {
    for (int o=i0; o<i1; o++) // 1
      V[o] = read(i[o]);    // 1
  }

  // Store conditional if item at i is tag.
  // 1. Try replace tag at i with item.
  private boolean sc(int i, long y) {
    return cas(i, tag[th()], y); // 1
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
      save[th()] = x;
      if (cas(i, x, tag[th()])) return x;
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
    if (isTag(x))
      cas(i, x, save[threadId(x)]);
  }

  // Simulates CAS operation.
  // 1. Check if expected value is present.
  // 1a. If not present, exit (fail).
  // 1b. Otherwise, update value (success).
  private boolean cas(int i, long e, long y) {
    synchronized (data) {
      if (data[i] != e) return false; // 1, 1a
      data[i] = y; // 1b
      return true; // 1b
    }
  }

  // Increments this thread's tag.
  // 1. Get current tag id.
  // 2. Increment tag id.
  private void incTag() {
    long id = tagId(tag[th()]); // 1
    tag[th()] = newTag(id+1);      // 2
  }

  // Gets current thread-id as integer.
  private static int th() {
    return (int) Thread.currentThread().getId();
  }


  // SUPPORT
  // -------
  // Gets length of array.
  public int length() {
    return data.length;
  }

  // Gets iterator to values in array.
  @Override
  public Iterator<Long> iterator() {
    Collection<Long> c = new ArrayList<>();
    synchronized (data) {
      for (int i=0; i<data.length; i++)
        c.add(get(i));
    }
    return c.iterator();
  }

  // Converts array to string.
  @Override
  public String toString() {
    StringBuilder s = new StringBuilder("{");
    for (Long v : this)
      s.append(v).append(", ");
    if (s.length()>1) s.setLength(s.length()-2);
    return s.append("}").toString();
  }


  // VALUE
  // -----
  // Creates new value.
  // 1. Clear b63.
  private static long newValue(long v) {
    return (v<<1) >>> 1; // 1
  }

  // Checks if item is a value.
  // 1. Check is b63 is not set.
  private static boolean isValue(long x) {
    return x >= 0L; // 1
  }

  // Gets value from item (value).
  // 1. Copy sign from b62.
  private static long value(long x) {
    return (x<<1) >> 1; // 1
  }


  // TAG
  // ---
  // Creates a new tag.
  // 1. Set b63.
  // 2. Set thread-id from b62-b48.
  // 3. Set tag-id from b47-b0.
  private static long newTag(long id) {
    long th = Thread.currentThread().getId();
    return (1L<<63) | (th<<48) | id; // 1, 2, 3
  }

  // Checks if item is a tag.
  // 1. Check if b63 is set.
  private static boolean isTag(long x) {
    return x < 0L; // 1
  }

  // Gets thread-id from item (tag).
  // 1. Get 15-bits from b62-b48.
  private static int threadId(long x) {
    return (int) ((x>>>48) & 0x7FFFL); // 1
  }

  // Gets tag-id from item (tag).
  // 1. Get 48-bits from b47-b0.
  private static long tagId(long x) {
    return x & 0xFFFFFFFFFFFFL; // 1
  }
}
