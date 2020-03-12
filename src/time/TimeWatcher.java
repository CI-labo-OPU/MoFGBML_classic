package time;

public class TimeWatcher {
	// ************************************************************
	double start;
	double stop;

	double time = 0.0;

	// ************************************************************
	public TimeWatcher() {}

	// ************************************************************
	public void start() {
		start = System.nanoTime();
	}

	public void stop() {
		stop = System.nanoTime();
		time += (stop - start);
	}

	public void clearTime() {
		time = 0.0;
	}

	public double getSec() {
		return (time/1000000000.0);
	}

	public double getNano() {
		return time;
	}

}
