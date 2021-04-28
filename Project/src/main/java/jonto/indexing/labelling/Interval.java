package jonto.indexing.labelling;

import java.util.ArrayList;
import java.util.List;

public class Interval {
    int leftbound;
    int rightbound;

    public Interval(int leftb, int rightb) {
        leftbound = leftb;
        rightbound = rightb;
    }

    public Interval(String serialized_interval) {
        if (serialized_interval.indexOf(",") > 0) {
            leftbound = Integer.parseInt(serialized_interval.split(",")[0]);
            rightbound = Integer.parseInt(serialized_interval.split(",")[1]);
        } else {
            leftbound = -1;
            rightbound = -1;
        }
    }

    public int getLeftBound() {
        return leftbound;
    }

    public int getRightBound() {
        return rightbound;
    }

    public void setLeftBound(int leftbound) {
        this.leftbound = leftbound;
    }

    public void setRightBound(int rightbound) {
        this.rightbound = rightbound;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof Interval i)) {
            return false;
        }

        return equals(i);
    }

    public boolean equals(Interval i) {
        return leftbound == i.getLeftBound() && rightbound == i.getRightBound();
    }

    public boolean isSuperIntervalOf(Interval i) {
        return i.getLeftBound() >= leftbound && i.getRightBound() <= rightbound;
    }

    public boolean isSubIntervalOf(Interval i) {
        return i.getLeftBound() <= leftbound && i.getRightBound() >= rightbound;
    }

    public boolean containsIndex(int index) {
        return index <= rightbound && index >= leftbound;
    }

    public boolean hasNonEmptyIntersectionWith(Interval i) {
        return rightbound >= i.leftbound && i.rightbound >= leftbound;
    }

    public Interval getIntersectionWith(Interval i) {
        if (hasNonEmptyIntersectionWith(i)) {
            return new Interval(
                    Math.max(leftbound, i.getLeftBound()),
                    Math.min(rightbound, i.getRightBound())
            );
        }
        return new Interval(-1, -1);
    }

    public boolean isAdjacentTo(Interval i) {
        return hasNonEmptyIntersectionWith(i) || i.rightbound == leftbound - 1 || rightbound == i.leftbound - 1;
    }

    public Interval getUnionWith(Interval i) {
        return new Interval(Math.min(leftbound, i.getLeftBound()), Math.max(rightbound, i.getRightBound()));
    }

    public List<Interval> getUnionWithList(Interval i) {
        List<Interval> unionList = new ArrayList<Interval>();

        if (isAdjacentTo(i)) {
            unionList.add(new Interval(Math.min(leftbound, i.getLeftBound()), Math.max(rightbound, i.getRightBound())));
        } else {
            unionList.add(this);
            unionList.add(i);
        }

        return unionList;
    }

    public boolean hasLowerLeftBoundThan(Interval i) {
        return leftbound < i.getLeftBound();
    }

    public boolean hasGreaterLeftBoundThan(Interval i) {
        return leftbound > i.getLeftBound();
    }

    public boolean isEmptyInterval() {
        return leftbound >= 0;
    }

    public String toString() {
        return "<" + leftbound + ", " + rightbound + ">";
    }

    public String serialize() {
        return leftbound + "," + rightbound;
    }

    public int hashCode() {
        int code = 10;
        code = 40 * code + leftbound;
        code = 40 * code + rightbound;
        return code;
    }
}
