package jonto.indexing.labelling;

public class PostNode extends Node {

    private final Interval descOrderInterval;
    private final Interval ascOrderInterval;

    public PostNode(int classId) {
        super(classId);
        descOrderInterval = new Interval(-1, -1);
        descIntervals.add(descOrderInterval);
        ascOrderInterval = new Interval(-1, -1);
        ascIntervals.add(ascOrderInterval);
    }

    @Override
    public void setDescOrder(int postorder) {
        descOrderInterval.setRightBound(postorder);
    }

    @Override
    public void setDescChildOrder(int minPostorder) {
        descOrderInterval.setLeftBound(minPostorder);
    }

    @Override
    public int getDescOrder() {
        return descOrderInterval.getRightBound();
    }

    @Override
    public int getDescChildOrder() {
        return descOrderInterval.getLeftBound();
    }

    @Override
    public Interval getDescOrderInterval() {
        return descOrderInterval;
    }

    @Override
    public void setAscOrder(int postorder) {
        ascOrderInterval.setRightBound(postorder);
    }

    @Override
    public void setAscChildOrder(int minPostorder) {
        ascOrderInterval.setLeftBound(minPostorder);
    }

    @Override
    public int getAscOrder() {
        return ascOrderInterval.getRightBound();
    }

    @Override
    public int getAscChildOrder() {
        return ascOrderInterval.getLeftBound();
    }

    @Override
    public Interval getAscOrderInterval() {
        return ascOrderInterval;
    }
}
