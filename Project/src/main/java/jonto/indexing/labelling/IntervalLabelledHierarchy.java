package jonto.indexing.labelling;

import java.util.*;
import java.util.Map.Entry;

public abstract class IntervalLabelledHierarchy {
    public static final int ROOT_LABEL = -100;
    public static final int LEAF_LABEL = -99;

    protected HashMap<Integer, Set<Integer>> ontoHierarchy;
    protected HashMap<Integer, Node> classesToNodesMap;
    protected Node root;
    protected Node leaf;
    protected List<Node> topoSortedNodes;

    public IntervalLabelledHierarchy(HashMap<Integer, Set<Integer>> ontoHierarchy, boolean clearStructures) {
        this.ontoHierarchy = ontoHierarchy;
        createClassesToNodesMap();
        createHierarchy();

        obtainRoot();
        obtainLeaf();

        if (leaf.getClassId().equals(LEAF_LABEL)) {
            leaf.unattach();
        }

        obtainDescSpanningTree();
        walkDescendant();
        computeDescIntervals();

        if (root.getClassId().equals(ROOT_LABEL)) {
            root.unattach();
        }
        if (leaf.getClassId().equals(LEAF_LABEL)) {
            leaf.reattach();
        }

        obtainAscSpanningTree();
        walkAscendant();
        computeAscIntervals();

        if (leaf.getClassId().equals(LEAF_LABEL)) {
            leaf.unattach();
        }

        if (clearStructures) {
            clearNodeStructures();
        }
    }

    public IntervalLabelledHierarchy(HashMap<Integer, Set<Integer>> ontoHierarchy) {
        this(ontoHierarchy, true);
    }


    protected abstract Node createNode(int classId);


    protected abstract void walkDescendant();


    protected abstract void walkAscendant();


    public Map<Integer, Node> getClassesToNodesMap() {
        return classesToNodesMap;
    }

    public boolean hasDescendant(int parentClassId, int childClassId) {
        Node parent = classesToNodesMap.get(parentClassId);
        Node child = classesToNodesMap.get(childClassId);
        if (parent == null) {
            throw new IllegalArgumentException("Parent class not found.");
        }
        if (child == null) {
            throw new IllegalArgumentException("Child class not found.");
        }

        int index = child.getDescOrder();

        for (Interval interval : parent.getDescIntervals()) {
            if (interval.containsIndex(index)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasAncestor(int childClassId, int parentClassId) {
        Node child = classesToNodesMap.get(childClassId);
        Node parent = classesToNodesMap.get(parentClassId);
        if (parent == null) {
            throw new IllegalArgumentException("Parent class not found.");
        }
        if (child == null) {
            throw new IllegalArgumentException("Child class not found.");
        }

        int index = parent.getAscOrder();

        for (Interval interval : child.getAscIntervals()) {
            if (interval.containsIndex(index)) {
                return true;
            }
        }

        return false;
    }

    private void createClassesToNodesMap() {
        classesToNodesMap = new HashMap<Integer, Node>();
        assert ontoHierarchy != null;
        // Map nodes with children.
        for (int classId : ontoHierarchy.keySet()) {
            classesToNodesMap.put(classId, createNode(classId));
        }
        // Map leaf nodes.
        for (Set<Integer> children : ontoHierarchy.values()) {
            for (int childId : children) {
                if (!classesToNodesMap.containsKey(childId)) {
                    classesToNodesMap.put(childId, createNode(childId));
                }
            }
        }
    }

    private void createHierarchy() {
        for (Entry<Integer, Set<Integer>> entry : ontoHierarchy.entrySet()) {
            int classId = entry.getKey();
            Set<Integer> childrenIds = entry.getValue();
            Node node = classesToNodesMap.get(classId);
            for (int childId : childrenIds) {
                Node child = classesToNodesMap.get(childId);
                assert child != null;
                node.addChild(child);
            }
        }
    }

    private void obtainRoot() {
        Set<Node> roots = new HashSet<Node>();
        for (Node node : classesToNodesMap.values()) {
            if (node.isRoot()) {
                roots.add(node);
            }
        }
        if (roots.size() > 1) {
            root = createNode(ROOT_LABEL);
            for (Node node : roots) {
                root.addChild(node);
            }
        } else {
            root = roots.iterator().next();
        }
    }

    private void obtainLeaf() {
        Set<Node> leaves = new HashSet<Node>();
        for (Node node : classesToNodesMap.values()) {
            if (node.isLeaf()) {
                leaves.add(node);
            }
        }
        if (leaves.size() > 1) {
            leaf = createNode(LEAF_LABEL);
            for (Node node : leaves) {
                node.addChild(leaf);
            }
        } else {
            leaf = leaves.iterator().next();
        }
    }

    private void obtainDescSpanningTree() {
        doTopologicalSort();
        for (Node node : topoSortedNodes) {
            Node maxPredsParent = null;
            int maxPreds = -1;
            for (Node parent : node.getParents()) {
                if (parent.getParents().size() > maxPreds) {
                    maxPredsParent = parent;
                    maxPreds = parent.getParents().size();
                }
            }
            node.setDescTreeParent(maxPredsParent);
        }
    }

    private void obtainAscSpanningTree() {
        doTopologicalSort();

        ListIterator<Node> iter = topoSortedNodes.listIterator(topoSortedNodes.size());

        while (iter.hasPrevious()) {
            Node node = iter.previous();
            Node maxPredsParent = null;
            int maxPreds = -1;
            for (Node parent : node.getChildren()) {
                // Invert edges.
                if (parent.getChildren().size() > maxPreds) {
                    maxPredsParent = parent;
                    maxPreds = parent.getChildren().size();
                }
            }
            node.setAscTreeParent(maxPredsParent);
        }
    }

    private void computeDescIntervals() {
        doTopologicalSort();
        ListIterator<Node> iter = topoSortedNodes.listIterator(topoSortedNodes.size());
        while (iter.hasPrevious()) {
            Node node = iter.previous();
            for (Node child : node.getChildren()) {
                if (node.equals(child)) {
                    System.out.println("Same node as children: computeDescIntervals.");
                    continue;
                }
                for (Interval interval : child.getDescIntervals()) {
                    node.addDescInterval(interval);
                }
            }
        }
    }

    private void computeAscIntervals() {
        doTopologicalSort();
        for (Node node : topoSortedNodes) {
            for (Node child : node.getParents()) { //Inverting edges!
                if (node.equals(child)) {
                    System.out.println("Same node as children: computeAscIntervals.");
                    continue;
                }
                for (Interval interval : child.getAscIntervals()) {
                    node.addAscInterval(interval);
                }
            }
        }
    }

    private void clearNodeStructures() {
        for (Node node : classesToNodesMap.values()) {
            node.clearAuxiliarStructures();
        }
        ontoHierarchy.clear();
        ontoHierarchy = null;
    }

    protected void doTopologicalSort() {
        // Obtain topologically ordered list of nodes.
        if (topoSortedNodes == null) {
            topoSortedNodes = new TopologicalSorting().sort(root);
        }
    }
}
