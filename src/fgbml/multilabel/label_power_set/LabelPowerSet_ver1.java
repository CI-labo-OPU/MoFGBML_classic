package fgbml.multilabel.label_power_set;

import java.io.File;
import java.util.ArrayList;

import data.Input;
import data.MultiDataSetInfo;
import data.SingleDataSetInfo;
import data.SinglePattern;
import emo.algorithms.Algorithm;
import emo.algorithms.moead.MOEA_D;
import emo.algorithms.nsga2.NSGA2;
import fgbml.SinglePittsburgh;
import fuzzy.StaticFuzzyFunc;
import ga.Population;
import main.Consts;
import main.Experiment;
import main.Setting;
import method.MersenneTwisterFast;
import method.Output;
import method.ResultMaster;
import time.TimeWatcher;

public class LabelPowerSet_ver1 implements Experiment {
	public void startExperiment(String[] args, String traFile, String tstFile,
								MersenneTwisterFast rnd, ResultMaster resultMaster) {
		/* ********************************************************* */
		//START:

		/* ********************************************************* */
		//Load Dataset
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		MultiDataSetInfo Dtst = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, traFile);
		Input.inputMultiLabel(Dtst, tstFile);

		//Generate Problem
		Problem_LPS_multi mop_multi = new MOP_LPS_multi(Dtra, Dtst);

		/* ********************************************************* */
		//Make result directry
		String sep = File.separator;
		String resultRoot = resultMaster.getRootDir();

		String trialRoot = resultMaster.getTrialRoot();
		Output.makeDir(trialRoot, Consts.POPULATION);
		Output.makeDir(trialRoot, Consts.OFFSPRING);

		String aggregationDir = trialRoot + sep + "aggregation";
		Output.mkdirs(aggregationDir);

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
		//Data Transform
		SingleDataSetInfo Dtra_single = transformLPS(Dtra);
		SingleDataSetInfo Dtst_single = transformLPS(Dtst);

		//Generate Problem
		Problem_LPS_single mop = new MOP_LPS_single(Dtra_single);

		/* ********************************************************* */
		//Generate OutputClass
		Output_LPS_single output = new Output_LPS_single();

		/* ********************************************************* */
		//Generate Algorithm
		Algorithm<SinglePittsburgh> algorithm;

		/* ********************************************************* */
		//Generate Individual Instance
		SinglePittsburgh instance = new SinglePittsburgh();

		/* ********************************************************* */
		//Population in Final Generation
		Population<SinglePittsburgh> population = new Population<>();

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
			population = algorithm.main(mop, output, instance,
										resultMaster, rnd,
										timeWatcher, evaWatcher);
		}
		else if(Setting.emoType == Consts.WS ||
				Setting.emoType == Consts.TCHEBY ||
				Setting.emoType == Consts.PBI ||
				Setting.emoType == Consts.AOF) {
			algorithm = new MOEA_D<SinglePittsburgh>();
			population = algorithm.main(mop, output, instance,
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


		/* ********************************************************* */
		//Final Best Classifier
		SinglePittsburgh best = getBestSinglePittsburgh(population);
		mop_multi.setAppendix(best);
		Output_LPS_multi output_multi = new Output_LPS_multi();
		String individualStr = output_multi.outputPittsburgh(best);
		String fileName = aggregationDir + sep + "individual.csv";
		Output.writeln(fileName, individualStr);

		System.out.println();

	}

	public SingleDataSetInfo transformLPS(MultiDataSetInfo multi) {
		SingleDataSetInfo single = new SingleDataSetInfo();
		single.setDataSize(multi.getDataSize());
		single.setNdim(multi.getNdim());

		ArrayList<Integer> distincts = new ArrayList<>();

		for(int p = 0; p < multi.getDataSize(); p++) {
			double[] x = new double[multi.getNdim() + 1];
			int conClass = 0;
			for(int n = 0; n < multi.getNdim(); n++) {
				x[n] = multi.getPattern(p).getDimValue(n);
			}

			for(int c = 0; c < multi.getCnum(); c++) {
				int bit = 1;
				for(int cc = 0; cc < c; cc++) {
					bit *= 2;
				}
				conClass += bit * multi.getPattern(p).getConClass((multi.getCnum()-1) - c);
			}
			x[multi.getNdim()] = conClass;
			if(!distincts.contains(conClass)) {
				distincts.add(conClass);
			}

			SinglePattern pattern = new SinglePattern(p, x);
			single.addPattern(pattern);
		}

		single.setCnum(distincts.size());

		return single;
	}

	public static SinglePittsburgh getBestSinglePittsburgh(Population<SinglePittsburgh> population) {

		double min = Double.MAX_VALUE;
		int minIndex = 0;

		for(int p = 0; p < population.getIndividuals().size(); p++) {
			double f = population.getIndividual(p).getFitness(0);
			if(f < min) {
				min = f;
				minIndex = p;
			}
		}

		SinglePittsburgh best = population.getIndividual(minIndex);
		return best;
	}
}























