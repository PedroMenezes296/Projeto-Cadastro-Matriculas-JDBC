package index;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class BTree<K extends Comparable<K>, V> {
    private final int t;
    private BTreeNode root;
    private int count = 0;


    public BTree(int t) { this.t = t; this.root = new BTreeNode(t, true); }

    public void clear() { this.root = new BTreeNode(t, true); this.count = 0; }
    public int size() { return count; }

    public Optional<Long> search(long matricula) {
        return search(root, matricula);
    }


    private Optional<Long> search(BTreeNode x, long k) {
        if (x == null) return Optional.empty();

        int i = 0;
        while (i < x.n && k > x.keys[i]) i++;

        // Achou a chave neste nó (folha OU interno)
        if (i < x.n && k == x.keys[i]) {
            return Optional.of(x.ptrs[i]);
        }


        // Se não achou e já é folha, acabou
        if (x.leaf) return Optional.empty();

        // Caso contrário, desce para o filho adequado
        return search(x.children[i], k);
    }


    public void insert(long matricula, long pointerId) {   // <- long
        var found = search(root, matricula);
        if (found.isPresent()) { updatePointer(root, matricula, pointerId); return; }

        BTreeNode r = root;
        if (r.n == 2 * t - 1) {
            BTreeNode s = new BTreeNode(t, false);
            s.children[0] = r;
            splitChild(s, 0, r);
            root = s;
            insertNonFull(s, matricula, pointerId);
        } else {
            insertNonFull(r, matricula, pointerId);
        }
    }

    private void updatePointer(BTreeNode x, long k, long p) {
        int i = 0;
        while (i < x.n && k > x.keys[i]) i++;
        if (i < x.n && k == x.keys[i]) {
            if (x.leaf) x.ptrs[i] = p; else updatePointer(x.children[i + 1], k, p);
            return;
        }
        if (x.leaf) return;
        updatePointer(x.children[i], k, p);
    }

    private void insertNonFull(BTreeNode x, long k, long p) {
        int i = x.n - 1;
        if (x.leaf) {
            while (i >= 0 && k < x.keys[i]) {
                x.keys[i + 1] = x.keys[i];
                x.ptrs[i + 1] = x.ptrs[i];
                i--;
            }
            x.keys[i + 1] = k;
            x.ptrs[i + 1] = p;
            x.n++;
        } else {
            while (i >= 0 && k < x.keys[i]) i--;
            i++;
            if (x.children[i].n == 2 * t - 1) {
                splitChild(x, i, x.children[i]);
                if (k > x.keys[i]) i++;
            }
            insertNonFull(x.children[i], k, p);
        }
    }
    public void savePrettyToFile(String path) {
        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of(path), printPretty());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public java.util.List<KeyPointer> entriesInOrder() {
        var out = new java.util.ArrayList<KeyPointer>();
        collectInOrder(root, out);
        return out;
    }
    private void collectInOrder(BTreeNode x, java.util.List<KeyPointer> out) {
        int i;
        for (i = 0; i < x.n; i++) {
            if (!x.leaf) collectInOrder(x.children[i], out);
            if (x.leaf) out.add(new KeyPointer(x.keys[i], x.ptrs[i]));
            // se não folha, ponteiro “válido” fica na folha correspondente
        }
        if (!x.leaf) collectInOrder(x.children[i], out);
    }



    private void splitChild(BTreeNode x, int i, BTreeNode y) {
        BTreeNode z = new BTreeNode(t, y.leaf);
        z.n = t - 1;

        // Copia as últimas (t-1) chaves de y para z
        for (int j = 0; j < t - 1; j++) {
            z.keys[j] = y.keys[j + t];
            z.ptrs[j] = y.ptrs[j + t];   // ✅ COPIA O PONTEIRO SEMPRE
        }

        // Copia filhos se não for folha
        if (!y.leaf) {
            for (int j = 0; j < t; j++) {
                z.children[j] = y.children[j + t];
            }
        }

        // Reduz y para t-1 chaves
        y.n = t - 1;

        // Abre espaço no pai para novo filho
        for (int j = x.n; j >= i + 1; j--) {
            x.children[j + 1] = x.children[j];
        }
        x.children[i + 1] = z;

        // Abre espaço no pai para a chave promovida
        for (int j = x.n - 1; j >= i; j--) {
            x.keys[j + 1] = x.keys[j];
            x.ptrs[j + 1] = x.ptrs[j];   // ✅ SHIFT DO PTR TAMBÉM
        }

        // Promove chave e ponteiro do meio
        x.keys[i] = y.keys[t - 1];
        x.ptrs[i] = y.ptrs[t - 1];       // ✅ ESSA LINHA É A MAIS IMPORTANTE

        x.n++;
    }


    public String printInOrder() {
        StringBuilder sb = new StringBuilder();
        inOrder(root, sb);
        return sb.toString().trim();
    }
    private void inOrder(BTreeNode x, StringBuilder sb) {
        int i;
        for (i = 0; i < x.n; i++) {
            if (!x.leaf) inOrder(x.children[i], sb);
            sb.append(x.keys[i]).append(' ');
        }
        if (!x.leaf) inOrder(x.children[i], sb);
    }
    public java.util.List<KeyPointer> range(long fromInclusive, long toInclusive) {
        var out = new java.util.ArrayList<KeyPointer>();
        rangeInOrder(root, fromInclusive, toInclusive, out);
        return out;
    }

    private void rangeInOrder(BTreeNode x, long a, long b, java.util.List<KeyPointer> acc) {
        int i;
        for (i = 0; i < x.n; i++) {
            // visita filho da esquerda quando ainda pode haver valores >= a
            if (!x.leaf) {
                // Só desce se existe possibilidade de encontrar algo >= a no filho
                if (i == 0 || x.keys[i - 1] >= a) rangeInOrder(x.children[i], a, b, acc);
            }
            long k = x.keys[i];
            if (k >= a && k <= b) {
                if (x.leaf) acc.add(new KeyPointer(k, x.ptrs[i]));
                else {
                    // ponteiro válido está na folha; desce para a direita para localizar
                    rangeInOrder(x.children[i + 1], k, k, acc);
                }
            }
        }
        if (!x.leaf) rangeInOrder(x.children[i], a, b, acc);
    }

    // 2) ASCII por níveis (nós entre colchetes)
    public String printAscii() {
        var sb = new StringBuilder();
        var q = new java.util.ArrayDeque<java.util.AbstractMap.SimpleEntry<BTreeNode,Integer>>();
        q.add(new java.util.AbstractMap.SimpleEntry<>(root, 0));
        int curLvl = 0;
        while (!q.isEmpty()) {
            var e = q.poll();
            BTreeNode n = e.getKey();
            int lvl = e.getValue();
            if (lvl != curLvl) { sb.append('\n'); curLvl = lvl; }
            sb.append('[');
            for (int i = 0; i < n.n; i++) {
                sb.append(n.keys[i]);
                if (i < n.n - 1) sb.append(" | ");
            }
            sb.append(']').append(' ');
            if (!n.leaf) {
                for (int i = 0; i <= n.n; i++) q.add(new java.util.AbstractMap.SimpleEntry<>(n.children[i], lvl + 1));
            }
        }
        return sb.toString().trim();
    }
    // ======= REMOÇÃO COMPLETA =======

    public boolean remove(long k) {
        boolean removed = remove(root, k);
        // Se a raiz ficou vazia e não é folha, "desce" a raiz.
        if (root.n == 0 && !root.leaf) {
            root = root.children[0];
        }
        if (removed && count > 0) count--; // (se você utiliza count)
        return removed;
    }

    private boolean remove(BTreeNode x, long k) {
        int idx = findKeyIndex(x, k);

        // Caso 1: chave presente neste nó em idx
        if (idx < x.n && x.keys[idx] == k) {
            if (x.leaf) {
                removeFromLeaf(x, idx);
                return true;
            } else {
                removeFromNonLeaf(x, idx);
                return true;
            }
        } else {
            // Caso 2: chave não presente neste nó
            if (x.leaf) return false; // não existe

            // Determina o filho que pode conter k
            boolean lastChild = (idx == x.n);
            BTreeNode child = x.children[idx];

            // Se o filho tem apenas t-1 chaves, precisamos garantir t antes de descer
            if (child.n == t - 1) {
                fill(x, idx);
                // Se fizemos merge com o filho da direita, o "idx" pode ter mudado
                if (lastChild && idx > x.n) idx = x.n;
            }
            return remove(x.children[idx], k);
        }
    }

    public String printPretty() {
        StringBuilder sb = new StringBuilder();
        buildPretty(root, "", true, sb);
        return sb.toString();
    }

    private void buildPretty(BTreeNode node, String prefix, boolean isTail, StringBuilder sb) {
        // 1) imprime o nó atual
        sb.append(prefix)
                .append(isTail ? "└── " : "├── ")
                .append('[');
        for (int i = 0; i < node.n; i++) {
            sb.append(node.keys[i]);
            if (i < node.n - 1) sb.append(" | ");
        }
        sb.append(']').append('\n');

        // 2) imprime os filhos (0..n) se não for folha
        if (!node.leaf) {
            for (int i = 0; i <= node.n; i++) {
                boolean last = (i == node.n);
                String childPrefix = prefix + (isTail ? "    " : "│   ");
                buildPretty(node.children[i], childPrefix, last, sb);
            }
        }
    }

    private int findKeyIndex(BTreeNode x, long k) {
        int idx = 0;
        while (idx < x.n && x.keys[idx] < k) idx++;
        return idx;
    }

    // Remove chave de posição idx em folha
    private void removeFromLeaf(BTreeNode x, int idx) {
        for (int i = idx + 1; i < x.n; i++) {
            x.keys[i - 1] = x.keys[i];
            x.ptrs[i - 1] = x.ptrs[i];
        }
        x.n--;
    }

    // Remove chave em nó interno (casos: predecessor/sucessor/merge)
    private void removeFromNonLeaf(BTreeNode x, int idx) {
        long k = x.keys[idx];

        BTreeNode left = x.children[idx];
        BTreeNode right = x.children[idx + 1];

        // Caso A: filho esquerdo tem >= t chaves -> substitui por predecessor
        if (left.n >= t) {
            long pred = getPredecessor(left);
            x.keys[idx] = pred;                 // ponteiros válidos ficam nas folhas
            remove(left, pred);
        }
        // Caso B: filho direito tem >= t chaves -> substitui por sucessor
        else if (right.n >= t) {
            long succ = getSuccessor(right);
            x.keys[idx] = succ;
            remove(right, succ);
        }
        // Caso C: ambos têm t-1 -> merge e desce
        else {
            merge(x, idx);
            remove(left, k);
        }
    }

    // Maior chave da subárvore (vai até a folha mais à direita)
    private long getPredecessor(BTreeNode x) {
        while (!x.leaf) x = x.children[x.n];
        return x.keys[x.n - 1];
    }

    // Menor chave da subárvore (vai até a folha mais à esquerda)
    private long getSuccessor(BTreeNode x) {
        while (!x.leaf) x = x.children[0];
        return x.keys[0];
    }

    // Garante que o filho idx tenha pelo menos t chaves
    private void fill(BTreeNode x, int idx) {
        if (idx > 0 && x.children[idx - 1].n >= t) {
            borrowFromPrev(x, idx);
        } else if (idx < x.n && x.children[idx + 1].n >= t) {
            borrowFromNext(x, idx);
        } else {
            // Faz merge com irmão esquerdo (se possível), senão com direito
            if (idx < x.n) merge(x, idx);
            else merge(x, idx - 1);
        }
    }

    // Empréstimo do irmão esquerdo
    private void borrowFromPrev(BTreeNode x, int idx) {
        BTreeNode child = x.children[idx];
        BTreeNode sibling = x.children[idx - 1];

        // abre espaço no filho
        for (int i = child.n - 1; i >= 0; i--) {
            child.keys[i + 1] = child.keys[i];
            child.ptrs[i + 1] = child.ptrs[i];
        }
        if (!child.leaf) {
            for (int i = child.n; i >= 0; i--) {
                child.children[i + 1] = child.children[i];
            }
        }

        // move chave do pai para filho
        child.keys[0] = x.keys[idx - 1];
        if (!child.leaf) child.children[0] = sibling.children[sibling.n];

        // sobe a última chave do irmão para o pai
        x.keys[idx - 1] = sibling.keys[sibling.n - 1];

        // se folha, traz o ponteiro também
        if (child.leaf) child.ptrs[0] = sibling.ptrs[sibling.n - 1];

        child.n++;
        sibling.n--;
    }

    // Empréstimo do irmão direito
    private void borrowFromNext(BTreeNode x, int idx) {
        BTreeNode child = x.children[idx];
        BTreeNode sibling = x.children[idx + 1];

        // chave do pai desce para o fim de child
        child.keys[child.n] = x.keys[idx];
        if (!child.leaf) child.children[child.n + 1] = sibling.children[0];
        if (child.leaf) child.ptrs[child.n] = sibling.ptrs[0];

        // primeira chave do irmão sobe para o pai
        x.keys[idx] = sibling.keys[0];

        // shift no irmão
        for (int i = 1; i < sibling.n; i++) {
            sibling.keys[i - 1] = sibling.keys[i];
            sibling.ptrs[i - 1] = sibling.ptrs[i];
        }
        if (!sibling.leaf) {
            for (int i = 1; i <= sibling.n; i++) {
                sibling.children[i - 1] = sibling.children[i];
            }
        }

        child.n++;
        sibling.n--;
    }

    // Merge child[idx] com child[idx+1], puxando chave do pai
    private void merge(BTreeNode x, int idx) {
        BTreeNode left = x.children[idx];
        BTreeNode right = x.children[idx + 1];

        // puxa chave separadora do pai para o meio de left
        left.keys[left.n] = x.keys[idx];
        // (internal nodes não usam ptrs; ponteiros válidos residem nas folhas)

        // copia chaves/ptrs do irmão da direita
        for (int i = 0; i < right.n; i++) {
            left.keys[left.n + 1 + i] = right.keys[i];
            if (left.leaf) left.ptrs[left.n + 1 + i] = right.ptrs[i];
        }

        // copia filhos se não for folha
        if (!left.leaf) {
            for (int i = 0; i <= right.n; i++) {
                left.children[left.n + 1 + i] = right.children[i];
            }
        }

        left.n = left.n + 1 + right.n;

        // remove a chave do pai e compacta filhos do pai
        for (int i = idx + 1; i < x.n; i++) x.keys[i - 1] = x.keys[i];
        for (int i = idx + 2; i <= x.n; i++) x.children[i - 1] = x.children[i];
        x.n--;
    }
    //------------------------------ Listar por faixa opção 4 -----------------------------------
    private static class Node<K, V> {
        int n;                 // quantidade de chaves no nó
        boolean leaf;
        Object[] keys;         // K[]
        Object[] values;       // V[] (se você guarda valores)
        Node<K, V>[] children; // filhos (n+1)

        @SuppressWarnings("unchecked")
        K keyAt(int i) { return (K) keys[i]; }

        @SuppressWarnings("unchecked")
        V valueAt(int i) { return (V) values[i]; }
    }

    private Node<K, V> root1;

    // =============================
    // LISTAR POR FAIXA (range query)
    // =============================
    public List<V> rangeValues(K ini, K fim) {
        if (ini == null || fim == null) throw new IllegalArgumentException("Intervalo inválido.");
        if (ini.compareTo(fim) > 0) {
            // se vier invertido, você pode trocar ou lançar erro
            K tmp = ini; ini = fim; fim = tmp;
        }

        List<V> out = new ArrayList<>();
        rangeValuesRec(root1, ini, fim, out);
        return out;
    }

    private void rangeValuesRec(Node<K, V> x, K ini, K fim, List<V> out) {
        if (x == null) return;

        // Percurso em ordem:
        // para cada chave i:
        //   visita filho i
        //   processa chave i
        // no fim visita último filho
        for (int i = 0; i < x.n; i++) {
            if (!x.leaf) {
                rangeValuesRec(x.children[i], ini, fim, out);
            }

            K k = x.keyAt(i);

            // Se k > fim, posso parar cedo (otimiza e mantém simples)
            if (k.compareTo(fim) > 0) return;

            if (k.compareTo(ini) >= 0 && k.compareTo(fim) <= 0) {
                out.add(x.valueAt(i));
            }
        }

        if (!x.leaf) {
            rangeValuesRec(x.children[x.n], ini, fim, out);
        }
    }




    public String printLevelOrder() { return "(LevelOrder ainda não implementado)"; }
}
