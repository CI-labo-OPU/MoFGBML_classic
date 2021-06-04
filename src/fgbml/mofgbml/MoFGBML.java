package fgbml.mofgbml;

import java.io.File;

import data.Input;
import data.SingleDataSetInfo;
import emo.algorithms.Algorithm;
import emo.algorithms.moead.MOEA_D;
import emo.algorithms.nsga2.NSGA2;
import fgbml.SinglePittsburgh;
import fgbml.problem.OutputClass;
import fuzzy.StaticFuzzyFunc;
import main.Consts;
import main.Experiment;
import main.Setting;
import method.MersenneTwisterFast;
import method.Output;
import method.ResultMaster;
import time.TimeWatcher;

public class MoFGBML implements Experiment {

	public void startExperiment(String[] args,
								String traFile, String tstFile,
								MersenneTwisterFast rnd, ResultMaster resultMaster) {
		/* ********************************************************* */
		//START:

		/* ********************************************************* */
		//Load Dataset
		SingleDataSetInfo Dtra = new SingleDataSetInfo();
		SingleDataSetInfo Dtst = new SingleDataSetInfo();
		Input.inputFile(Dtra, traFile);
		Input.inputFile(Dtst, tstFile);

		/* ********************************************************* */
		//Make result directry
		String sep = File.separator;
		String resultRoot = resultMaster.getRootDir();

		String trialRoot = resultMaster.getTrialRoot();
		Output.makeDir(trialRoot, Consts.POPULATION);
		Output.makeDir(trialRoot, Consts.OFFSPRING);

		String populationDir = resultMaster.getTrialRoot() + sep + Consts.POPULATION;
		String offspringDir = resultMaster.getTrialRoot() + sep + Consts.OFFSPRING;
		Output.makeDir(populationDir, Consts.INDIVIDUAL);
		Output.makeDir(populationDir, Consts.RULESET);
		Output.makeDir(offspringDir, Consts.INDIVIDUAL);
		Output.makeDir(offspringDir, Consts.RULESET);

		/* ********************************************************* */
		//Initialize Fuzzy Sets
		StaticFuzzyFunc.initFuzzy(Dtra);

		/* ********************************************************* */
		//MOP No.
		int mopNo;
		if(args.length < 7) {
			mopNo = 1;	//MOP1
		} else {
			mopNo = Integer.parseInt(args[6]);
		}

		/* ********************************************************* */
		//Generate Problem
		Problem_MoFGBML mop = getMOP(mopNo, Dtra, Dtst);

		/* ********************************************************* */
		//Generate Algorithm
		Algorithm<SinglePittsburgh> algorithm;

		/* ********************************************************* */
		//Generate OutputClass
		OutputClass<SinglePittsburgh> output = new Output_MoFGBML();

		/* ********************************************************* */
		//Generate Individual Instance
		SinglePittsburgh instance = new SinglePittsburgh();

		/* ********************************************************* */
		//Timer start
		TimeWatcher timeWatcher = new TimeWatcher();	//All Exprimeint executing time
		TimeWatcher evaWatcher = new TimeWatcher();		//Evaluating time
		timeWatcher.start();

		/* ********************************************************* */
		/* ********************************************************* */
		//GA Start
		if(Setting.emoType == Consts.NSGA2) {
			algorithm = new NSGA2<SinglePittsburgh>();
			algorithm.main( mop, output, instance,
							resultMaster, rnd,
							timeWatcher, evaWatcher);
		}
		else if(Setting.emoType == Consts.WS ||
				Setting.emoType == Consts.TCHEBY ||
				Setting.emoType == Consts.PBI ||
				Setting.emoType == Consts.AOF) {
			algorithm = new MOEA_D<SinglePittsburgh>();
			algorithm.main( mop, output, instance,
							resultMaster, rnd,
							timeWatcher, evaWatcher);
		}
		/* ********************************************************* */
		/* ********************************************************* */

		//GA End
		timeWatcher.stop();
		resultMaster.addTimes( timeWatcher.getSec() );
		resultMaster.addEvaTimes( evaWatcher.getSec() );

		//Output One Trial Information
		resultMaster.outputIndividual(populationDir, offspringDir);
		resultMaster.population.clear();
		resultMaster.ruleSetPopulation.clear();
		resultMaster.offspring.clear();
		resultMaster.ruleSetOffspring.clear();

		System.out.println();
	}


	/**
	 * Get MOPx Class<br>
	 * @param mopNo : int : MOPx
	 * @param Dtra
	 * @param Dtst
	 * @return
	 */
	public Problem_MoFGBML getMOP(int mopNo, SingleDataSetInfo Dtra, SingleDataSetInfo Dtst) {
		Problem_MoFGBML mop = null;
		switch(mopNo) {
		case 1:
			mop = new MOP1(Dtra, Dtst);
			break;
		}
		return mop;
	}
}
