package jonto.indexing;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class LightTarjan implements Serializable {
    @Serial
    private static final long serialVersionUID = -3317618668787472814L;
    private int index = 0;
    private final ArrayList<Integer> stack = new ArrayList<>();
    private final Map<Integer, Set<Integer>> SCCs = new HashMap<>();
    private int[] idx;
    private int[] lowlink;

    private void tarjan(Integer v, HashMap<Integer, Set<Integer>> graph) {
        idx[v] = index;
        lowlink[v] = index;

        ++index;
        stack.add(0, v);

        if (graph.containsKey(v)) {
            for (Integer n : graph.get(v)) {
                if (idx[n] == -1) {
                    tarjan(n, graph);
                    lowlink[v] = Math.min(lowlink[v], lowlink[n]);
                } else if (stack.contains(n)) {
                    lowlink[v] = Math.min(lowlink[v], idx[n]);
                }
            }
        }

        if (lowlink[v] == idx[v]) {
            Integer n;
            Set<Integer> component = new HashSet<>();
            do {
                n = stack.remove(0);
                component.add(n);
            } while (!n.equals(v));
            for (Integer i : component) {
                SCCs.put(i, component);
            }
        }
    }

    public Map<Integer, Set<Integer>> executeTarjan(HashMap<Integer,
            Set<Integer>> graph) {
        SCCs.clear();
        index = 0;
        stack.clear();
        assert graph != null && !graph.isEmpty();

        Set<Integer> nodeList = new HashSet<>(graph.keySet());
        for (Set<Integer> children : graph.values()) {
            nodeList.addAll(children);
        }
        assert !nodeList.isEmpty();
        int maxId = Collections.max(nodeList) + 1;

        idx = new int[maxId];
        Arrays.fill(idx, -1);
        lowlink = new int[maxId];
        Arrays.fill(lowlink, -1);

        for (Integer node : nodeList) {
            if (idx[node] == -1) {
                tarjan(node, graph);
            }
        }

        return SCCs;
    }
}
