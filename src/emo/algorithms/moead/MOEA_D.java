package emo.algorithms.moead;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import emo.algorithms.Algorithm;
import fgbml.Pittsburgh;
import fgbml.problem.FGBML;
import fgbml.problem.OutputClass;
import ga.GAFunctions;
import ga.Population;
import ga.PopulationManager;
import main.Consts;
import main.Setting;
import method.MersenneTwisterFast;
import method.Output;
import method.ResultMaster;
import method.StaticFunction;
import time.TimeWatcher;

@SuppressWarnings("rawtypes")
public class MOEA_D<T extends Pittsburgh> extends Algorithm<T>{
	// ************************************************************

	// ************************************************************
	public MOEA_D() {}

	// ************************************************************


	@SuppressWarnings("unchecked")
	/**
	 * Reference paper:<br>
	 *   Title: "MOEA/D: A Multiobjective Evolutionary Algorithm Based on Decomposition"<br>
	 *   Authors: Q. Zhang, and H. Li.<br>
	 *   Journal: IEEE Transactions on Evolutionary Computation, vol. 11, no. 6.<br>
	 *   Year: 2007<br>
	 *
	 * @param mop
	 * @param resultMaster
	 * @param rnd
	 * @param timeWatcher
	 * @param evaWatcher
	 */
	@Override
	public Population<T> main(	FGBML mop, OutputClass output, T instance,
						ResultMaster resultMaster, MersenneTwisterFast rnd,
						TimeWatcher timeWatcher, TimeWatcher evaWatcher) {
		/* ********************************************************* */
		//START:

		//For output directories.
		String sep = File.separator;
		String EPDir = resultMaster.getTrialRoot() + sep + "EP";
		Output.mkdirs(EPDir);
		Output.makeDir(EPDir, Consts.INDIVIDUAL);
		Output.makeDir(EPDir, Consts.RULESET);
		ArrayList<String> individualEP = new ArrayList<>();
		ArrayList<String> ruleSetEP = new ArrayList<>();
		ArrayList<String> saveZ = new ArrayList<>();
		String[] detail;

		int Ndim = mop.getTrain().getNdim();
		int genCount = 0;	//Generation Count
		int evaCount = 0;	//Evaluation Count

		/* ********************************************************* */
		// MOEA/D parameters
//		/** #of deviding objective space */
//		int H = Consts.VECTOR_DIVIDE_NUM[mop.getObjectiveNum()];
//		/** Neighbor size */
//		int T = Consts.NEIGHBOR_SIZE;
		/** #of weight vectors */
		int vecSize;
		/** ideal point */
		double[] z = new double[mop.getObjectiveNum()];
		Arrays.fill(z, Double.MAX_VALUE);
		/** nadir point */
		double[] e = new double[mop.getObjectiveNum()];
		Arrays.fill(e, -Double.MAX_VALUE);
		/** SubProblem Instances */
		ScalarizeFunction<T>[] functions;
		/* ********************************************************* */

		//Preparing Population Manager
		PopulationManager<Population<T>> manager = new PopulationManager<>();

		/* ********************************************************* */
		//Step 1. Initialization

		//Step 1.1. Initialize External Population
		/** External Populatoin, which has non-dominated solutions found so far. */
//		Population<T> EP = new Population<>();

		//Step 1.2. Initialize weight vectors & Calculate neighbor vectors.
		functions = StaticMOEAD.initScalarizeFunctions(mop.getObjectiveNum(), mop.getTrain().getDataSize());
		for(int vec = 0; vec < functions.length; vec++) {
			functions[vec].calcMatingNeighbors(functions);
			functions[vec].calcUpdateNeighbors(functions);
		}
		vecSize = functions.length;

		//Step 1.3. Population Initialization & Evaluation & Assignment into SubProblems
		//Population Initialization
		Population<T> population = new Population<>();
		for(int vec = 0; vec < vecSize; vec++) {
			//Initialization
			T individual = (T)instance.newInstance(Ndim, Consts.INITIATION_RULE_NUM, mop.getObjectiveNum());

			while(true) {
				//RuleSet Generation
				if(Consts.DO_HEURISTIC_GENERATION) {
					//Heuristic Rule Generation
					individual.initHeuristic(mop.getTrain(), rnd);
				}
				else {
					//Random Antecedent Generation
					individual.initRand(rnd);
				}

				//Consequent Part Learning
				individual.getRuleSet().learning(mop.getTrain(), Setting.forkJoinPool);

				//Reducing Same Rules
				individual.getRuleSet().removeRule();
				individual.getRuleSet().radixSort();
				individual.getRuleSet().calcRuleLength();

				if(individual.getRuleSet().getMicRules().size() != 0) {
					break;
				}
			}

			individual.ruleset2michigan();
			individual.michigan2pittsburgh();
			individual.initAppendix(mop.getAppendixNum());
			population.addIndividual(individual);
		}
		manager.setPopulation(population);

		//Initial Population Evaluation
		evaWatcher.start();
		population.evaluateIndividuals(mop);
		evaWatcher.stop();
		evaCount += population.getIndividuals().size();

		//Assignment into SubProblems
		for(int vec = 0; vec < vecSize; vec++) {
			functions[vec].setBest(population.getIndividual(vec));
			double scalar = functions[vec].function(population.getIndividual(vec).getFitness());
			functions[vec].setBestScalar(scalar);
			functions[vec].setFV(population.getIndividual(vec).getFitness());
		}

		//Step 1.4. Ideal Point z initialization.
		//with Initial Population's value
		for(int p = 0; p < population.getIndividuals().size(); p++) {
			StaticMOEAD.updateIdeal(z, population.getIndividual(p));
			StaticMOEAD.updateNadir(e, population.getIndividual(p));
		}
		for(int vec = 0; vec < vecSize; vec++) {
			//Shallow Copy - don't need to set these points AGAIN.
			functions[vec].setZ(z);
			functions[vec].setE(e);
		}

		//Save Initial Population
		timeWatcher.stop();
		//Appendix Information
		mop.setAppendix(manager.getPopulation());
		output.savePopulationOrOffspring(manager, resultMaster, true);
		timeWatcher.start();

		//Step 1.5. EP Initialization
//		ArrayList<Individual_nsga2> nonDominated = StaticFunction.getNonDominatedSolution(population, mop.getOptimizer());
//		for(int i = 0; i < nonDominated.size(); i++) {
//			EP.addIndividual((T)nonDominated.get(i));
//		}

		//Step 1.6. Finish the first genration.
		System.out.print("0");
		genCount++;
		//Save EP
		timeWatcher.stop();
//		detail = transformStrings(output, EP);
//		individualEP.add(detail[0]);
//		ruleSetEP.add(detail[1]);
		saveZ.add(transformZ(z));
		timeWatcher.start();

		/* ********************************************************* */
		//Step 2. Update (GA Searching Frame)
		int detailCount = 1;
		while(true) {
			/* ********************************************************* */
			//Output "Period" per const interval.
			if(genCount % Consts.PER_SHOW_GENERATION_NUM == 0) {
				if(detailCount % Consts.PER_SHOW_GENERATION_DETAIL == 0) {
					System.out.print(genCount);
				} else {
					System.out.print(".");
				}
				detailCount++;
			}

			//The termination criteria judge
			if(StaticFunction.terminationJudge(genCount, evaCount)) {
				break;
			}

			/* ********************************************************* */
			//Offspring Generation
			Population<T> offspring = new Population<>();
			/** To randomly select vector in terms of the order. */
			Integer[] vecOrder = StaticFunction.sampringWithout(vecSize, vecSize, rnd);
			for(int q = 0; q < vecSize; q++) {
				//Step 2.1. Reproduction
				T child = null;
				while(true) {
					/**
					 * Step 1. Mating Selection
					 *   Select two indexes mom, dad from q-th vector's neighbors.
					 */
					Integer[] index = StaticFunction.sampringWithout(Consts.SELECTION_NEIGHBOR_NUM, 2, rnd);
					int mom = functions[vecOrder[q]].getMatingNeighbors()[index[0]];
					int dad = functions[vecOrder[q]].getMatingNeighbors()[index[1]];
					Pittsburgh[] parents = new Pittsburgh[2];
					parents[0] = functions[mom].getBest();
					parents[1] = functions[dad].getBest();

					//Step 2. Crossover
						//GA type Selection (Michigan or Pittsburgh)
					double p;
					if(StaticFunction.sameGeneInt(parents[0], parents[1])) {
						p = 1.0;
					} else {
						p = Consts.RULE_OPE_RT;
					}
					if(rnd.nextDouble() < p) {
						//Michigan Type Crossover (Child Generation)
						child = (T)GAFunctions.michiganCrossover(mop, parents[0], rnd);
					}
					else {
						//Pittsburgh Type Crossover (Child Generation)
						child = (T)GAFunctions.pittsburghCrossover(parents, rnd);
					}

					//Step 3. Mutation
					GAFunctions.pittsburghMutation(child, mop.getTrain(), rnd);

					//Step 4. Learning
					child.getRuleSet().learning(mop.getTrain(), Setting.forkJoinPool);

					//Step 5. Rule Deletion
					child.getRuleSet().removeRule();
					child.getRuleSet().radixSort();
					child.getRuleSet().calcRuleLength();

					//If child don't have any rule, the child should not be evaluated.
					//Then new child will be generated.
					if(child.getRuleSet().getMicRules().size() != 0) {
						break;
					}
				}
				child.ruleset2michigan();
				child.michigan2pittsburgh();
				child.initAppendix(mop.getAppendixNum());

				//Evaluate Child
				evaWatcher.start();
				mop.evaluateParallel(child);
				evaWatcher.stop();
				evaCount++;

				//Step 2.3. Update of z
				StaticMOEAD.updateIdeal(z, child);
				StaticMOEAD.updateNadir(e, child);

				//Step 2.4. Update of Neighboring Solutions
				for(int t = 0; t < Consts.UPDATE_NEIGHBOR_NUM; t++) {
					int j = functions[vecOrder[q]].getUpdateNeighbors()[t];
					functions[j].updateBest(child);
				}

				//Step 2.5. Update of EP
//				StaticMOEAD.updateEP(EP, child, mop.getOptimizer());

				offspring.addIndividual(child);
			}
			manager.setOffspring(offspring);

			genCount++;

			/* ********************************************************* */
			//Save current Population & new Offspring
			timeWatcher.stop();
			if(genCount % Setting.timingOutput == 0) {
				population.getIndividuals().clear();
				for(int vec = 0; vec < vecSize; vec++) {
					population.addIndividual(functions[vec].getBest());
				}
				manager.setPopulation(population);

				//Appendix Information
				mop.setAppendix(manager.getPopulation());
				mop.setAppendix(manager.getOffspring());

				//Save
				output.savePopulationOrOffspring(manager, resultMaster, true);
				output.savePopulationOrOffspring(manager, resultMaster, false);
//				detail = transformStrings(output, EP);
//				individualEP.add(detail[0]);
//				ruleSetEP.add(detail[1]);
				saveZ.add(transformZ(z));
			}
			timeWatcher.start();
		}
		// Generation End
		/* ********************************************************* */

		timeWatcher.stop();
		//Output method for EP.
//		outputEP(EPDir, individualEP, ruleSetEP);
		String fileName = resultMaster.getTrialRoot() + sep + "ideal.csv";
		Output.writeln(fileName, saveZ);
		timeWatcher.start();

//		return EP;
		return null;
	}

	/**
	 * Output EP
	 * @param dir
	 * @param individual
	 * @param ruleSet
	 */
	public void outputEP(String dir, ArrayList<String> individual, ArrayList<String> ruleSet) {
		String sep = File.separator;
		String fileName;

		for(int i = 0; i < individual.size(); i++) {
			int genCount = i * Setting.timingOutput;
			fileName = dir + sep + Consts.INDIVIDUAL + sep + "gen" + genCount + ".csv";
			Output.writeln(fileName, individual.get(i));
			fileName = dir + sep + Consts.RULESET + sep + "gen" + genCount + ".csv";
			Output.writeln(fileName, ruleSet.get(i));
		}
	}

	/**
	 * Transforming population to String[]
	 * @param population : {@literal Population<MultiPittsburgh}
	 * @return String[] : [0]:individual, [1]:ruleset
	 */
	@SuppressWarnings("unchecked")
	public String[] transformStrings(OutputClass output, Population population) {
		String[] details = new String[2];
		details[0] = output.outputPittsburgh(population);
		details[1] = output.outputRuleSet(population);

		return details;
	}

	/** Transforming z(ideal) to String[]
	 * @param z
	 * @return
	 */
	public String transformZ(double[] z) {
		String str = String.valueOf(z[0]);
		for(int o = 1; o < z.length; o++) {
			str += "," + String.valueOf(z[o]);
		}
		return str;
	}


}

















































