// Each Bitonic[2K] consists of 2 Bitonic[K]
// networks followed by a Merger[2K]. The top
// Bitonic[K] is connected to top-half inputs, and
// bottom Bitonic[K] is connected to bottom-half
// inputs. Outputs of both Bitonic[K] are connected
// directly to Merger[2K]. Bitonic[2] networks
// consists of a single Balancer.
class PeriodicNetwork implements CountingNetwork {
  CountingNetwork[] blocks;
  final int width;
  // halves: top, bottom Bitonic[K]
  // merger: Merger[2K] connected to both Bitonic[K]
  // width: number of inputs/outputs

  public PeriodicNetwork(int w) {
    int B = Integer.numberOfLeadingZeros(w);
    blocks = new CountingNetwork[B];
    for (int i=0; i<B; i++)
      blocks[i] = new BlockNetwork(w);
    width = w;
  }

  // 1. Find connected Bitonic[K].
  // 2. Traverse connected Bitonic[K].
  // 3. Traverse connected Merger[2K].
  @Override
  public int traverse(int x) {
    for (CountingNetwork b : blocks)
      x = b.traverse(x);
    return x;
  }
}
