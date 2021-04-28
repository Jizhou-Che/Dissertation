package jonto.indexing.labelling;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TopologicalSorting {
    private LinkedList<Node> nodes;
    private Set<Node> visited;

    public TopologicalSorting() {
        reset();
    }

    public void reset() {
        nodes = new LinkedList<Node>();
        visited = new HashSet<Node>();
    }

    public List<Node> sort(Node root) {
        visit(root);
        return nodes;
    }

    private void visit(Node node) {
        if (!visited.contains(node)) {
            visited.add(node);

            for (Node child : node.getChildren()) {
                visit(child);
            }

            nodes.addFirst(node);
        }
    }
}
