package fgbml.multilabel.binary_relevance;

import java.io.File;

import data.Input;
import data.MultiDataSetInfo;
import data.SingleDataSetInfo;
import emo.algorithms.Algorithm;
import emo.algorithms.moead.MOEA_D;
import emo.algorithms.nsga2.NSGA2;
import fgbml.SinglePittsburgh;
import fgbml.problem.OutputClass;
import fuzzy.SingleRuleSet;
import fuzzy.StaticFuzzyFunc;
import ga.Population;
import main.Consts;
import main.Experiment;
import main.Setting;
import method.MersenneTwisterFast;
import method.Output;
import method.ResultMaster;
import time.TimeWatcher;

public class BinaryRelevance_ver1 implements Experiment {

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

		/* ********************************************************* */
		//Make result directry
		String sep = File.separator;
		String resultRoot = resultMaster.getRootDir();

		String trialRoot = resultMaster.getTrialRoot();
		String aggregationDir = trialRoot + sep + "aggregation";
		Output.mkdirs(aggregationDir);

		/* ********************************************************* */
		//Initialize Fuzzy Sets
		StaticFuzzyFunc.initFuzzy(Dtra);

		//Generate Problem
		Problem_BR_Multi mop = new MOP_multi(Dtra, Dtst);

		/* ********************************************************* */
		//Timer start
		TimeWatcher timeWatcher = new TimeWatcher();	//All Exprimeint executing time
		TimeWatcher evaWatcher = new TimeWatcher();		//Evaluating time
		timeWatcher.start();

		/* ********************************************************* */
		//Generate OutputClass
		Output_BR_multi output = new Output_BR_multi();

		/* ********************************************************* */
		//Generate Aggrigation Array
		SingleRuleSet[] bestRuleSets = new SingleRuleSet[mop.getTrain().getCnum()];

		/* ********************************************************* */
		/* ********************************************************* */
		//Binary Relevance Start
		for(int c = 0; c < mop.getTrain().getCnum(); c++) {
			System.out.println("---");
			System.out.println("Label: " + c);

			/* ********************************************************* */
			//Make result directry
			String trialBRroot = trialRoot + sep + "Label-" + String.format("%03d", c);
			resultMaster.setTrialRoot(trialBRroot);

			String populationDir = trialBRroot + sep + Consts.POPULATION;
			Output.mkdirs(populationDir);
			String offspringDir = trialBRroot + sep + Consts.OFFSPRING;
			Output.mkdirs(offspringDir);

			Output.makeDir(populationDir, Consts.INDIVIDUAL);
			Output.makeDir(populationDir, Consts.RULESET);
			Output.makeDir(offspringDir, Consts.INDIVIDUAL);
			Output.makeDir(offspringDir, Consts.RULESET);

			/* ********************************************************* */
			//Transform Dataset
			SingleDataSetInfo Dtra_single = mop.getDtra_single(c);

			/* ********************************************************* */
			//Generate Problem
			Problem_BR_Single mop_single = new MOP_single(Dtra_single);

			/* ********************************************************* */
			//Generate Individual Instance
			SinglePittsburgh instance = new SinglePittsburgh();

			/* ********************************************************* */
			//Generate Algorithm
			Algorithm<SinglePittsburgh> algorithm;

			/* ********************************************************* */
			//Generate OutputClass
			OutputClass<SinglePittsburgh> output_single = new Output_BR_single();

			/* ********************************************************* */
			//Population in Final Generation
			Population<SinglePittsburgh> population = new Population<>();

			/* ********************************************************* */
			/* ********************************************************* */
			//GA Start
			if(Setting.emoType == Consts.NSGA2) {
				algorithm = new NSGA2<SinglePittsburgh>();
				population = algorithm.main(mop_single, output_single, instance,
											resultMaster, rnd,
											timeWatcher, evaWatcher);
			}
			else if(Setting.emoType == Consts.WS ||
					Setting.emoType == Consts.TCHEBY ||
					Setting.emoType == Consts.PBI ||
					Setting.emoType == Consts.AOF ||
					Setting.emoType == Consts.AOF2) {
				algorithm = new MOEA_D<SinglePittsburgh>();
				population = algorithm.main(mop_single, output_single, instance,
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

			//Get Best Single Classifier
			bestRuleSets[c] = getBestSingleRuleSet(population);
			bestRuleSets[c].radixSort();

			System.out.println();
		}
		/* ********************************************************* */
		/* ********************************************************* */

		/* ********************************************************* */
		//Generate Aggrigation RuleSet
		BRruleset ruleSet = new BRruleset(bestRuleSets);
		ruleSet.aggregationRuleNum();

		/* ********************************************************* */
		//Generate Aggrigation Pittsburgh Individual
		BRpittsburgh individual = new BRpittsburgh(ruleSet);
		individual.setObjectiveNum(mop.getObjectiveNum());
		individual.setNdim(Dtra.getNdim());

		//Appendix
		mop.evaluate(individual);
		mop.setAppendix(individual);

		//Output
		String individualStr = output.outputPittsburgh(individual);
		String fileName = aggregationDir + sep + "individual.csv";
		Output.writeln(fileName, individualStr);
		String ruleSetStr = output.outputRuleSet(individual);
		fileName = aggregationDir + sep + "ruleSet.txt";
		Output.writeln(fileName, ruleSetStr);

	}


	public static SingleRuleSet getBestSingleRuleSet(Population<SinglePittsburgh> population) {

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

		return best.getRuleSet();
	}
}









