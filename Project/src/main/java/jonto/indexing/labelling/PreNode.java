package jonto.indexing.labelling;

public class PreNode extends Node {
    private final Interval descOrderInterval;
    private final Interval ascOrderInterval;

    public PreNode(int classId) {
        super(classId);
        //descOrderInterval = new Interval(-1,-1);
        descOrderInterval = new Interval(-classId, -classId); //We avoid wrong preorders in case not given
        descIntervals.add(descOrderInterval);
        ascOrderInterval = new Interval(-1, -1);
        ascIntervals.add(ascOrderInterval);
    }

    @Override
    public void setDescOrder(int preorder) {
        descOrderInterval.setLeftBound(preorder);
    }

    @Override
    public void setDescChildOrder(int maxPreorder) {
        descOrderInterval.setRightBound(maxPreorder);
    }

    @Override
    public int getDescOrder() {
        return descOrderInterval.getLeftBound();
    }

    @Override
    public int getDescChildOrder() {
        return descOrderInterval.getRightBound();
    }

    @Override
    public Interval getDescOrderInterval() {
        return descOrderInterval;
    }

    @Override
    public void setAscOrder(int preorder) {
        ascOrderInterval.setLeftBound(preorder);
    }

    @Override
    public void setAscChildOrder(int maxPreorder) {
        ascOrderInterval.setRightBound(maxPreorder);
    }

    @Override
    public int getAscOrder() {
        return ascOrderInterval.getLeftBound();
    }

    @Override
    public int getAscChildOrder() {
        return ascOrderInterval.getRightBound();
    }

    @Override
    public Interval getAscOrderInterval() {
        return ascOrderInterval;
    }
}
