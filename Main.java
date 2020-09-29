import java.util.*;
import java.util.concurrent.atomic.*;

class Main {
  static AtomicLong63Array shared;
  static int TS = 25;
  static int K = 10;
  // shared: shared array of length K
  // TS: number of threads
  // K: number of comparisions


  // Each thread without KCSS updates all entries
  // of `shared` array to `id+1`.
  static Thread withoutKCSS(int id) {
    long y = id+1;
    return new Thread(() -> {
      try {
      for (int n=0; n<K; n++) {
        shared.set(n, y);
        Thread.sleep(1);
      }
      log(id+": done");
      }
      catch (InterruptedException e) {}
    });
  } 

  // Each thread with KCSS updates all entries
  // of `shared` array to `id+1` only if all
  // entries/ are currently set to `id`
  // (k-comparisions).
  static Thread withKCSS(int id) {
    int[] I = new int[K];
    long[] E = new long[K];
    for (int n=0; n<K; n++) {
      I[n] = n;
      E[n] = id;
    }
    long y = id+1;
    return new Thread(() -> {
      try {
      for (int n=0; n<K; n++) {
        int[] i = Arrays.copyOfRange(I, n, K);
        long[] e = Arrays.copyOfRange(E, n, K);
        while (!shared.compareAndSet(i, e, y))
          Thread.sleep(1);
      }
      log(id+": done");
      }
      catch (InterruptedException e) {}
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

  // Test both threads without and with KCSS
  // to check if shared data was updated atomically.
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
