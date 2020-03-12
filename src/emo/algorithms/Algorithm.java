package emo.algorithms;

import fgbml.Pittsburgh;
import fgbml.problem.FGBML;
import fgbml.problem.OutputClass;
import ga.Population;
import method.MersenneTwisterFast;
import method.ResultMaster;
import time.TimeWatcher;

@SuppressWarnings("rawtypes")
public abstract class Algorithm<T extends Pittsburgh> {
	// ************************************************************


	// ************************************************************


	// ************************************************************

	public abstract Population<T> main(	FGBML mop, OutputClass output, T instance,
								ResultMaster resultMaster, MersenneTwisterFast rnd,
								TimeWatcher timeWatcher, TimeWatcher evaWatcher);

}
