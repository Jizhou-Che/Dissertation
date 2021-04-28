package jonto.indexing.labelling;

import java.util.HashSet;
import java.util.Set;

public abstract class Node {
    protected int classId;

    protected Set<Node> parents;
    protected Set<Node> children;

    protected Node descTreeParent;
    protected Set<Node> descTreeChildren;

    protected Node ascTreeParent;
    protected Set<Node> ascTreeChildren;

    protected Set<Interval> descIntervals;
    protected Set<Interval> ascIntervals;

    public abstract void setDescOrder(int orderIndex);

    public abstract int getDescOrder();

    public abstract void setAscOrder(int orderIndex);

    public abstract int getAscOrder();

    public abstract void setDescChildOrder(int childOrderIndex);

    public abstract int getDescChildOrder();

    public abstract void setAscChildOrder(int childOrderIndex);

    public abstract int getAscChildOrder();

    public abstract Interval getDescOrderInterval();

    public abstract Interval getAscOrderInterval();

    public Node(Integer classId) {
        this.classId = classId;
        parents = new HashSet<Node>();
        children = new HashSet<Node>();
        descTreeChildren = new HashSet<Node>();
        ascTreeChildren = new HashSet<Node>();
        descIntervals = new HashSet<Interval>();
        ascIntervals = new HashSet<Interval>();
    }

    public void clearAuxiliarStructures() {
        parents.clear();
        parents = null;
        children.clear();
        children = null;
        descTreeParent = null;
        descTreeChildren.clear();
        descTreeChildren = null;
        ascTreeParent = null;
        ascTreeChildren.clear();
        ascTreeChildren = null;
    }

    public void clearIntervalStructures() {
        descIntervals.clear();
        ascIntervals.clear();
    }

    public Integer getClassId() {
        return classId;
    }

    public Set<Interval> getDescIntervals() {
        return descIntervals;
    }

    public Set<Interval> getAscIntervals() {
        return ascIntervals;
    }

    public Set<Interval> setDescIntervals(Set<Interval> intervals) {
        return descIntervals = intervals;
    }

    public Set<Interval> setAncIntervals(Set<Interval> intervals) {
        return ascIntervals = intervals;
    }

    Set<Node> getChildren() {
        return children;
    }

    Set<Node> getParents() {
        return parents;
    }

    Node getDescTreeParent() {
        return descTreeParent;
    }

    Set<Node> getDescTreeChildren() {
        return descTreeChildren;
    }

    Node getAscTreeParent() {
        return ascTreeParent;
    }

    Set<Node> getAscTreeChildren() {
        return ascTreeChildren;
    }

    public void addChild(Node child) {
        children.add(child);
        descTreeChildren.add(child);
        child.parents.add(this);
        child.ascTreeChildren.add(this);
    }

    public void setDescTreeParent(Node node) {
        assert parents.contains(node);
        descTreeParent = node;
        for (Node parent : parents) {
            if (parent != descTreeParent) {
                parent.getDescTreeChildren().remove(this);
            }
        }
    }

    public void setAscTreeParent(Node node) {
        assert children.contains(node);
        ascTreeParent = node;
        for (Node parent : children) {
            if (parent != ascTreeParent) {
                parent.getAscTreeChildren().remove(this);
            }
        }
    }

    public void addDescInterval(Interval interval) {
        for (Interval ownInterval : descIntervals) {
            if (ownInterval.isSuperIntervalOf(interval)) {
                return;
            }
            if (interval.isSuperIntervalOf(ownInterval)) {
                descIntervals.remove(ownInterval);
                descIntervals.add(interval);
                return;
            }
        }

        if (interval.leftbound >= 0 && interval.rightbound >= 0) {
            descIntervals.add(interval);
        }
    }

    public void addAscInterval(Interval interval) {
        for (Interval ownInterval : ascIntervals) {
            if (ownInterval.isSuperIntervalOf(interval)) {
                return;
            }
            if (interval.isSuperIntervalOf(ownInterval)) {
                ascIntervals.remove(ownInterval);
                ascIntervals.add(interval);
                return;
            } else if (ownInterval.isAdjacentTo(interval)) {
                Interval unionInterval = new Interval(
                        Math.min(ownInterval.getLeftBound(), interval.getLeftBound()),
                        Math.max(ownInterval.getRightBound(), interval.getRightBound()));
                ascIntervals.remove(ownInterval);
                ascIntervals.add(unionInterval);
                return;
            }
        }
        ascIntervals.add(interval);
    }

    public boolean isRoot() {
        return parents.size() == 0;
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    public void unattach() {
        for (Node parent : parents) {
            parent.children.remove(this);
            parent.descTreeChildren.remove(this);
        }
        for (Node child : children) {
            child.parents.remove(this);
            if (child.descTreeParent.equals(this)) {
                child.descTreeParent = null;
            }
        }
        if (descTreeParent != null) {
            descTreeParent.descTreeChildren.remove(this);
        }
        for (Node descTreeChild : descTreeChildren) {
            descTreeChild.descTreeParent = null;
        }
        if (ascTreeParent != null) {
            ascTreeParent.ascTreeChildren.remove(this);
        }
        for (Node ascTreeChild : ascTreeChildren) {
            ascTreeChild.ascTreeParent = null;
        }
    }

    public void reattach() {
        for (Node parent : parents) {
            parent.children.add(this);
        }
        for (Node child : children) {
            child.parents.add(this);
        }
        if (descTreeParent != null) {
            descTreeParent.descTreeChildren.add(this);
        }
        for (Node descTreeChild : descTreeChildren) {
            descTreeChild.descTreeParent = this;
        }
        if (ascTreeParent != null) {
            ascTreeParent.ascTreeChildren.add(this);
        }
        for (Node ascTreeChild : ascTreeChildren) {
            ascTreeChild.ascTreeParent = this;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(getClassId());
    }
}
