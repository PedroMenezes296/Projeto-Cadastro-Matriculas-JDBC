package index;

class BTreeNode {
    final int t;
    int n;
    boolean leaf;
    long[] keys;          // <- long[]
    long[] ptrs;
    BTreeNode[] children;

    BTreeNode(int t, boolean leaf) {
        this.t = t;
        this.leaf = leaf;
        this.keys = new long[2 * t - 1];   // <- long
        this.ptrs = new long[2 * t - 1];
        this.children = new BTreeNode[2 * t];
        this.n = 0;
    }
}
