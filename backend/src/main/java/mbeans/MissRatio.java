package mbeans;

public class MissRatio implements MissRatioMBean {
    private final ClickStats stats;

    public MissRatio(ClickStats stats) {
        this.stats = stats;
    }

    @Override
    public double getMissRatio() {
        int total = stats.getTotalClicks();
        if (total == 0) return 0.0;
        return (stats.getMisses() * 100.0) / total;
    }
}