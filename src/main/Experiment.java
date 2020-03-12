package main;

import method.MersenneTwisterFast;
import method.ResultMaster;

public interface Experiment {

	public void startExperiment(String[] args, String traFile, String tstFile,
								MersenneTwisterFast rnd, ResultMaster resultMaster);
}
