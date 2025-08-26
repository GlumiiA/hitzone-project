package mbeans;

public interface ClickStatsMBean {
    void addClick(boolean isHit);
    int getTotalClicks();
    int getMisses();
}