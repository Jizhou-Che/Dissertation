package jonto.indexing.labelling;

import java.util.HashMap;
import java.util.Set;

public class PreIntervalLabelledHierarchy extends IntervalLabelledHierarchy {
    // Root node gets preorder 1.
    public final static int BASE_INDEX = 1 - 1;
    public final static int INDEX_INCREMENT = 1;

    public PreIntervalLabelledHierarchy(HashMap<Integer, Set<Integer>> ontoHierarchy, boolean clearStructures) {
        super(ontoHierarchy, clearStructures);
    }

    public PreIntervalLabelledHierarchy(HashMap<Integer, Set<Integer>> ontoHierarchy) {
        super(ontoHierarchy);
    }

    @Override
    protected Node createNode(int classId) {
        return new PreNode(classId);
    }

    @Override
    protected void walkDescendant() {
        walkDescNode(root, BASE_INDEX);
    }

    private int walkDescNode(Node node, int orderIndex) {
        orderIndex++;
        node.setDescOrder(orderIndex);
        for (Node child : node.getDescTreeChildren()) {
            if (node.equals(child)) {
                System.out.println("Same node as children: walkDescNode.");
                continue;
            }
            orderIndex = walkDescNode(child, orderIndex);
        }
        node.setDescChildOrder(orderIndex);
        return orderIndex;
    }

    @Override
    protected void walkAscendant() {
        walkAscNode(leaf, BASE_INDEX);
    }

    private int walkAscNode(Node node, int orderIndex) {
        orderIndex++;
        node.setAscOrder(orderIndex);
        for (Node child : node.getAscTreeChildren()) {
            if (node.equals(child)) {
                System.out.println("Same node as children: walkAscNode.");
                continue;
            }
            orderIndex = walkAscNode(child, orderIndex);
        }
        node.setAscChildOrder(orderIndex);
        return orderIndex;
    }
}
