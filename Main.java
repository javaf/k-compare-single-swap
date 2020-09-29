import java.util.*;
import java.util.concurrent.atomic.*;

class Main {
  static CountingNetwork bitonic;
  static AtomicInteger[] counts;
  static int WIDTH = 16;
  static int OPS = 1000;
  // bitonic: bitonic counting network of WIDTH
  // counts: atomic integers incremented by threads
  // WIDTH: number of threads / width of network
  // OPS: number of increments

  // Each unbalanced thread tries to increment a
  // random count. At the end, the counts would
  // not be balanced.
  // 
  // Each balanced thread tries to increment a
  // random count, but this time, selected through
  // a bitonic network. At the end, the counts
  // should be balanced.
  static Thread thread(int id, boolean balance) {
    return new Thread(() -> {
      for (int i=0; i<OPS; i++) {
        int c = (int) (WIDTH * Math.random());
        if (balance) c = bitonic.traverse(c);
        counts[c].incrementAndGet();
        Thread.yield();
      }
      log(id+": done");
    });
  }

  // Initialize bitonic network and counts.
  static void setup() {
    bitonic = new PeriodicNetwork(WIDTH);
    counts = new AtomicInteger[WIDTH];
    for (int i=0; i<WIDTH; i++)
      counts[i] = new AtomicInteger(0);
  }

  // Test either unbalanced or balanced threads.
  static void testThreads(boolean balance) {
    setup();
    Thread[] t = new Thread[WIDTH];
    for (int i=0; i<WIDTH; i++) {
      t[i] = thread(i, balance);
      t[i].start();
    }
    try {
    for (int i=0; i<WIDTH; i++)
      t[i].join();
    }
    catch(InterruptedException e) {}
  }

  // Check if counts are balanced. At maximum
  // counts should be separated by 1.
  static boolean isBalanced() {
    int v = counts[0].get();
    for (int i=0; i<WIDTH; i++)
      if (v-counts[i].get() > 1) return false;
    return true;
  }

  // Test both unbalanced and balanced threads
  // to check if counts stay balanced after they
  // run their increments.
  public static void main(String[] args) {
    log("Starting unbalanced threads ...");
    testThreads(false);
    log(Arrays.deepToString(counts));
    log("Counts balanced? "+isBalanced());
    log("");
    log("Starting balanced threads ...");
    testThreads(true);
    log(Arrays.deepToString(counts));
    log("Counts balanced? "+isBalanced());
  }

  static void log(String x) {
    System.out.println(x);
  }
}
