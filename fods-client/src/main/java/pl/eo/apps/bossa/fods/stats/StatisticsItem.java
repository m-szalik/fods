package pl.eo.apps.bossa.fods.stats;

public class StatisticsItem {
	private long get, release;
	private int breakTimes;
	private long breakTime;
	
	private long lastBreakTS = -1;
	
	public void addBreak() {
		lastBreakTS = System.currentTimeMillis();
		breakTimes++;
	}
	
	public void addRecovery() {
		breakTime = breakTime + System.currentTimeMillis() - lastBreakTS;
		lastBreakTS = 0;
	}
	
	public void addGet() {
		get++;
	}
	
	public void addRelease() {
		release++;
	}
	
	
	
	public long getBreakTime() {
		return breakTime;
	}
	
	public int getBreakTimes() {
		return breakTimes;
	}
	
	public long getGet() {
		return get;
	}
	
	public long getRelease() {
		return release;
	}
	
	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("get/release:").append(get);
		out.append('/').append(release);
		out.append(" (").append(get-release).append(")\n");
		out.append("breakTimes:").append(breakTimes).append('\n');
		out.append("breakTime:").append(breakTime/1000).append("s.\n");
		return out.toString();
	}
}
