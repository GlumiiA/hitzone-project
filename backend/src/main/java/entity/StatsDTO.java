package entity;

public class StatsDTO {
    public int totalClicks;
    public int misses;
    public double missRatio;

    public StatsDTO(int totalClicks, int misses, double missRatio) {
        this.totalClicks = totalClicks;
        this.misses = misses;
        this.missRatio = missRatio;
    }
}
