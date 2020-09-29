import java.util.*;
import java.util.concurrent.atomic.*;

class Main {
  static AtomicLong63Array shared;
  static int TS = 3;
  static int K = 6;
  // bitonic: bitonic counting network of WIDTH
  // counts: atomic integers incremented by threads
  // WIDTH: number of threads / width of network
  // OPS: number of increments


  // Each unbalanced thread tries to increment a
  // random count. At the end, the counts would
  // not be balanced.
  static Thread withoutKCSS(int id) {
    return new Thread(() -> {
      long y = id+1;
      for (int n=0; n<K; n++) {
        shared.set(n, y);
        Thread.yield();
      }
      log(id+": done");
    });
  } 

  // Each balanced thread tries to increment a
  // random count, but this time, selected through
  // a bitonic network. At the end, the counts
  // should be balanced.
  static Thread withKCSS(int id) {
    return new Thread(() -> {
      long th = Thread.currentThread().getId();
      log(id+": th="+th);
      int[] I = new int[K];
      long[] E = new long[K];
      for (int n=0; n<K; n++) {
        I[n] = n;
        E[n] = id;
      }
      long y = id+1;
      for (int n=0; n<K; n++) {
        int[] i = Arrays.copyOfRange(I, n, K);
        long[] e = Arrays.copyOfRange(E, n, K);
        log(id+"> "+n);
        log(id+"> "+shared);
        while (!shared.compareAndSet(i, e, y))          Thread.yield();
        log(id+"< "+shared);
        log(id+"< "+n);
      }
      log(id+": done");
    });
  }

  // Test with or without KCSS.
  static void testThreads(boolean kcss) {
    shared = new AtomicLong63Array(K);
    Thread[] t = new Thread[TS];
    for (int i=0; i<TS; i++) {
      t[i] = kcss? withKCSS(i) : withoutKCSS(i);
      t[i].start();
    }
    try {
    for (int i=0; i<TS; i++)
      t[i].join();
    }
    catch(InterruptedException e) {}
  }

  // Check if shared data was updated atomically.
  static boolean wasAtomic() {
    for (int n=0; n<K; n++)
      if (shared.get(n) != TS) return false;
    return true;
  }

  // Test both unbalanced and balanced threads
  // to check if counts stay balanced after they
  // run their increments.
  public static void main(String[] args) {
    log("Starting threads without KCSS ...");
    testThreads(false);
    log(""+shared);
    log("Updates were atomic? "+wasAtomic());
    log("");
    log("Starting threads with KCSS ...");
    testThreads(true);
    log(""+shared);
    log("Updates were atomic? "+wasAtomic());
  }

  static void log(String x) {
    System.out.println(x);
  }
}
