package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import data.Input;
import data.MultiDataSetInfo;
import data.SingleDataSetInfo;
import emo.algorithms.nsga2.Individual_nsga2;
import emo.algorithms.nsga2.NSGA2;
import emo.problems.MOP;
import emo.problems.example.ExampleMOP1;
import emo.problems.example.ExampleMOP2;
import emo.problems.knapsack_NSGA2.Individual_knapsack;
import fgbml.SingleMichigan;
import fgbml.SinglePittsburgh;
import fgbml.multilabel.MOP_ExactMatchError;
import fgbml.multilabel.MOP_Fmeasure;
import fgbml.multilabel.MultiPittsburgh;
import fuzzy.FuzzyPartitioning;
import fuzzy.SingleRule;
import fuzzy.SingleRuleSet;
import fuzzy.StaticFuzzyFunc;
import fuzzy.fml.FuzzySet;
import fuzzy.fml.KB;
import fuzzy.multi_label.MultiRule;
import fuzzy.multi_label.MultiRuleSet;
import ga.GAFunctions;
import ga.Individual;
import ga.Population;
import ga.RealGene;
import gui.ISIS2019_Main;
import jfml.term.FuzzyTermType;
import method.MersenneTwisterFast;
import method.Output;
import method.QuickSort;
import method.ResultMaster;
import method.StaticFunction;
import time.TimeWatcher;

public class test {
	static int now = 0;

	public static void main(String[] args) {
//		loadPropertyTest(args);

//		calcMembershipTest(args);
//		checkShapeMembershipTest(args);

//		allCombinationClassifierTest(args);

//		checkIndividual(args);

//		checkMOP(args);

//		dominateTest(args);

//		checkGenerics(args);

//		checkQuickSort(args);

//		checkKnapsack(args);

//		checkInputFML(args);

//		checkShapeNewthyroid(args);

//		checkEx4(args);

//		checkInitPittsburgh(args);

//		checkEx4Pittsburgh(args);

//		checkPittsburghMOP(args);

//		checkDominatedSort(args);

//		checkTimeWatcher(args);

//		checkCalendar(args);

//		checkSettingValues(args);

//		checkStaticIncrement(args);

//		checkSettingVariables(args);

//		checkMultiDataSetInfo(args);

//		checkSameAntecedent(args);

//		checkFuzzifyPartition(args);

//		checkMakeTrapezoids(args);

//		checkPartitioning(args);

//		checkOutputFML(args);

//		checkISIS(args);

//		checkMultiRule(args);

//		checkMultiRuleSet(args);

//		checkMultiPittsburgh(args);

//		checkRule(args);

//		checkHammingDistance(args);

//		checkMultiMOP(args);

//		checkManyRule(args);

		checkRadixSortForRule(args);
	}

	public static void checkRadixSortForRule(String[] args) {
		int[] a1 = new int[] {1, 2, 3};
		int[] a2 = new int[] {4, 5, 6};
		int[] a3 = new int[] {7, 8, 9};

		SingleRule rule1 = new SingleRule(a1);
		SingleRule rule2 = new SingleRule(a2);
		SingleRule rule3 = new SingleRule(a3);

		SingleRuleSet ruleSet1 = new SingleRuleSet();
		SingleRuleSet ruleSet2 = new SingleRuleSet();

		ruleSet1.addRule(rule1);
		ruleSet1.addRule(rule2);
		ruleSet1.addRule(rule3);;

		ruleSet2.addRule(rule3);
		ruleSet2.addRule(rule1);
		ruleSet2.addRule(rule2);

		String s1 = ruleSet1.getAntecedentSetString();
		String s2 = ruleSet2.getAntecedentSetString();

		System.out.println(s1.equals(s2));

		ruleSet1.radixSort();
		ruleSet2.radixSort();

		s1 = ruleSet1.getAntecedentSetString();
		s2 = ruleSet2.getAntecedentSetString();

		System.out.println(s1.equals(s2));


		System.out.println();


	}

	public static void checkManyRule(String[] args) {
		ForkJoinPool forkJoinPool = new ForkJoinPool(3);

		int seed = 3;
		MersenneTwisterFast rnd = new MersenneTwisterFast(seed);

		String sep = File.separator;
		String dataset = "flags";
		String fileName = "dataset" + sep + dataset + sep + "a0_0_" + dataset + "-10tra.dat";
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, fileName);

		fileName = "dataset" + sep + dataset + sep + "a0_0_" + dataset + "-10tst.dat";
		MultiDataSetInfo Dtst = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtst, fileName);

		StaticFuzzyFunc.homogeneousInit(Dtra.getNdim());

		MOP_ExactMatchError mop = new MOP_ExactMatchError(Dtra, Dtst);

		int ruleNum = 30;
		int objectiveNum = 2;
		int popSize = 2;
		Population<MultiPittsburgh> population = new Population<>();
		MultiPittsburgh[] parent = new MultiPittsburgh[popSize];
		for(int p = 0; p < popSize; p++) {
			MultiPittsburgh individual = new MultiPittsburgh(Dtra.getNdim(), ruleNum, objectiveNum);
			individual.setCnum(Dtra.getCnum());
			individual.initHeuristic(Dtra, rnd);
			individual.learning(Dtra, forkJoinPool);

			mop.evaluate(individual);

			parent[p] = individual;
			population.addIndividual(individual);
		}

		MultiPittsburgh child = (MultiPittsburgh) GAFunctions.michiganCrossover(mop, parent[0], rnd);
		GAFunctions.pittsburghMutation(child, Dtra, rnd);
		child.learning(Dtra, forkJoinPool);
		child.getRuleSet().removeRule();
		child.getRuleSet().calcRuleLength();
		mop.evaluate(child);

		int[] optimizer = new int[] {Consts.MINIMIZE, Consts.MINIMIZE};
		System.out.println(StaticFunction.isDominate(parent[0], child, optimizer));
		System.out.println();
	}


	public static void checkMultiMOP(String[] args) {
		ForkJoinPool forkJoinPool = new ForkJoinPool(3);

		MersenneTwisterFast rnd = new MersenneTwisterFast(0);

		String sep = File.separator;
		String fileName = "dataset" + sep + "flags" + sep + "a0_0_flags-10tra.dat";
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, fileName);

		fileName = "dataset" + sep + "flags" + sep + "a0_0_flags-10tst.dat";
		MultiDataSetInfo Dtst = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtst, fileName);

		StaticFuzzyFunc.homogeneousInit(Dtra.getNdim());

		int ruleNum = 30;
		int objectiveNum = 2;
		MultiPittsburgh individual = new MultiPittsburgh(Dtra.getNdim(), ruleNum, objectiveNum);
		individual.setCnum(Dtra.getCnum());
		individual.initHeuristic(Dtra, rnd);
		individual.learning(Dtra, forkJoinPool);

		int dataID = 0;
//		MOP_ExactMatchError mop = new MOP_ExactMatchError(Dtra, Dtst);
//		MOP_HammingLoss mop = new MOP_HammingLoss(Dtra, Dtst);
		MOP_Fmeasure mop = new MOP_Fmeasure(Dtra, Dtst);

		mop.evaluate(individual);

		System.out.println(individual.getFitness());

		System.out.println();
	}

	public static void checkHammingDistance(String[] args) {
		ForkJoinPool forkJoinPool = new ForkJoinPool(3);

		MersenneTwisterFast rnd = new MersenneTwisterFast(0);

		String sep = File.separator;
		String fileName = "dataset" + sep + "flags" + sep + "a0_0_flags-10tra.dat";
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, fileName);

		fileName = "dataset" + sep + "flags" + sep + "a0_0_flags-10tst.dat";
		MultiDataSetInfo Dtst = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtst, fileName);

		StaticFuzzyFunc.homogeneousInit(Dtra.getNdim());

		int ruleNum = 30;
		int objectiveNum = 2;
		MultiPittsburgh individual = new MultiPittsburgh(Dtra.getNdim(), ruleNum, objectiveNum);
		individual.setCnum(Dtra.getCnum());
		individual.initHeuristic(Dtra, rnd);
		individual.learning(Dtra, forkJoinPool);

		int dataID = 0;
		MOP_ExactMatchError mop = new MOP_ExactMatchError(Dtra, Dtst);
		int[][] classified = mop.getClassified(0, individual);
//		double hamming = mop.calcHammingLoss(0, individual);
		double hammingTest = mop.calcHammingLoss(0, classified);
//		double Fmeasure = mop.calcFmeasure(0, individual);
		double FmeasureTest = mop.calcFmeasures(0, classified)[0];
//		double exact = mop.calcExactMatchError(0, individual);
		double exactTest = mop.calcExactMatchError(0, classified);

//		System.out.println(hamming);
		System.out.println(hammingTest);
		System.out.println();
//		System.out.println(Fmeasure);
		System.out.println(FmeasureTest);
		System.out.println();
//		System.out.println(exact);
		System.out.println(exactTest);

		System.out.println();
	}

	public static void checkRule(String[] args) {
		ForkJoinPool forkJoinPool = new ForkJoinPool(3);

		String sep = File.separator;
		String fileName = "dataset" + sep + "newthyroid" + sep + "a0_0_newthyroid-10tra.dat";
		SingleDataSetInfo Dtra = new SingleDataSetInfo();
		Input.inputFile(Dtra, fileName);

		fileName = "dataset" + sep + "newthyroid" + sep + "a0_0_newthyroid-10tst.dat";
		SingleDataSetInfo Dtst = new SingleDataSetInfo();
		Input.inputFile(Dtst, fileName);

		StaticFuzzyFunc.homogeneousInit(Dtra.getNdim());

		int[] antecedent = new int[] {0, 0, 5, 0, 3};
		SingleRule rule = new SingleRule();
		rule.setRule(antecedent);
		rule.calcRuleConc(Dtra, forkJoinPool);

		System.out.println();

	}

	public static void checkMultiPittsburgh(String[] args) {
		ForkJoinPool forkJoinPool = new ForkJoinPool(3);

		MersenneTwisterFast rnd = new MersenneTwisterFast(0);

		String sep = File.separator;
		String fileName = "dataset" + sep + "flags" + sep + "a0_0_flags-10tra.dat";
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, fileName);

		fileName = "dataset" + sep + "flags" + sep + "a0_0_flags-10tst.dat";
		MultiDataSetInfo Dtst = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtst, fileName);

		StaticFuzzyFunc.homogeneousInit(Dtra.getNdim());

		int ruleNum = 30;
		int objectiveNum = 2;
		MultiPittsburgh individual = new MultiPittsburgh(Dtra.getNdim(), ruleNum, objectiveNum);
		individual.setCnum(Dtra.getCnum());
		individual.initHeuristic(Dtra, rnd);
		individual.learning(Dtra, forkJoinPool);

		double traRate = individual.getRuleSet().calcMissRateParallel(Dtra, false);
		double tstRate = individual.getRuleSet().calcMissRateParallel(Dtst, false);


		System.out.println();
	}

	public static void checkMultiRuleSet(String[] args) {
		ForkJoinPool forkJoinPool = new ForkJoinPool(3);

		MersenneTwisterFast rnd = new MersenneTwisterFast(0);

		String sep = File.separator;
		String fileName = "dataset" + sep + "flags" + sep + "a0_0_flags-10tra.dat";
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, fileName);

		fileName = "dataset" + sep + "flags" + sep + "a0_0_flags-10tst.dat";
		MultiDataSetInfo Dtst = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtst, fileName);

		StaticFuzzyFunc.homogeneousInit(Dtra.getNdim());

		int[] antecedent1 = StaticFuzzyFunc.heuristicGeneration(Dtra.getPattern(0), rnd);
		int[] antecedent2 = StaticFuzzyFunc.heuristicGeneration(Dtra.getPattern(10), rnd);
		int[] antecedent3 = StaticFuzzyFunc.heuristicGeneration(Dtra.getPattern(20), rnd);
		int[] antecedent4 = StaticFuzzyFunc.heuristicGeneration(Dtra.getPattern(30), rnd);

		MultiRule rule1 = new MultiRule(antecedent1, Dtra.getCnum());
		MultiRule rule2 = new MultiRule(antecedent2, Dtra.getCnum());
		MultiRule rule3 = new MultiRule(antecedent3, Dtra.getCnum());
		MultiRule rule4 = new MultiRule(antecedent4, Dtra.getCnum());

		MultiRuleSet ruleset = new MultiRuleSet();
		ruleset.addRule(rule1);
		ruleset.addRule(rule2);
		ruleset.addRule(rule3);
		ruleset.addRule(rule4);

		ruleset.learning(Dtra, forkJoinPool);
		ruleset.calcRuleLength();

		boolean doMemorize = true;
		double traRate = ruleset.calcMissRate(Dtra, doMemorize);
		double tstRate = ruleset.calcMissRateParallel(Dtst, false);

		System.out.println();

	}


	public static void checkMultiRule(String[] args) {
		ForkJoinPool forkJoinPool = new ForkJoinPool(3);

		MersenneTwisterFast rnd = new MersenneTwisterFast(0);

		String sep = File.separator;
		String fileName = "dataset" + sep + "flags" + sep + "a0_0_flags-10tra.dat";
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, fileName);

		fileName = "dataset" + sep + "flags" + sep + "a0_0_flags-10tst.dat";
		MultiDataSetInfo Dtst = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtst, fileName);

		int[] antecedent1 = new int[Dtra.getNdim()];
		int[] antecedent2 = new int[Dtra.getNdim()];
		int[] antecedent3 = new int[Dtra.getNdim()];
		int[] antecedent4 = new int[Dtra.getNdim()];
		for(int i = 0; i < Dtra.getNdim(); i++) {
			antecedent1[i] = 0;
			antecedent2[i] = 0;
			antecedent3[i] = 0;
			antecedent4[i] = 0;
		}

		antecedent2[2] = 1;
		antecedent3[2] = 2;
		antecedent4[2] = 3;

		MultiRule rule1 = new MultiRule(antecedent1, Dtra.getCnum());
		MultiRule rule2 = new MultiRule(antecedent2, Dtra.getCnum());
		MultiRule rule3 = new MultiRule(antecedent3, Dtra.getCnum());
		MultiRule rule4 = new MultiRule(antecedent4, Dtra.getCnum());

		StaticFuzzyFunc.homogeneousInit(Dtra.getNdim());

		rule1.calcRuleConc(Dtra, forkJoinPool);
		rule2.calcRuleConc(Dtra, forkJoinPool);
		rule3.calcRuleConc(Dtra, forkJoinPool);
		rule4.calcRuleConc(Dtra, forkJoinPool);

		System.out.println();

	}

	public static void checkISIS(String[] args) {
		String sep = File.separator;
		String traFile = "dataset" + sep + "iris" + sep + "a0_0_iris-10tra.dat";
		String tstFile = "dataset" + sep + "iris" + sep + "a0_0_iris-10tst.dat";

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmm");
		String id = format.format(calendar.getTime());
		//format: ".\result\iris_20191021-1255"
		String resultRoot = System.getProperty("user.dir") + sep + "result" + sep
							+ Setting.saveDir + sep + Setting.dataName + "_" + id;

		ResultMaster resultMaster = new ResultMaster(resultRoot, id);
		resultMaster.setNowCV(0);
		resultMaster.setNowRep(0);
		resultMaster.setTrialRoot(resultRoot + sep + "trial" + 0 + 0);

		int seed = 0;
		MersenneTwisterFast rnd = new MersenneTwisterFast(seed);

		ISIS2019_Main.startExperiment(args, traFile, tstFile, rnd, resultMaster);
	}

	public static void checkOutputFML(String[] args) {
//		int Ndim = 2;
//		StaticFuzzyFunc.threeTriangle(Ndim);

		Setting.forkJoinPool = new ForkJoinPool(3);
		SingleDataSetInfo tra = new SingleDataSetInfo();
		String sep = File.separator;
		String fileName = "dataset" + sep + "iris" + sep + "a0_0_iris-10tra.dat";
		Input.inputFile(tra, fileName);

		int K = 4;
		double F = 1.0;
		StaticFuzzyFunc.classEntropyInit(tra, K, F);

		fileName = "checkOutputFML_K-" + K + "_F-" + F + ".xml";
		StaticFuzzyFunc.outputFML(fileName);
	}

	public static void checkMakeTrapezoids(String[] args) {
		double F = 1;
		ArrayList<Double> partitions = new ArrayList<>();
		partitions.add(0.0);
		partitions.add(1.0/3.0);
		partitions.add(2.0/3.0);
		partitions.add(1.0);


		ArrayList<double[]> trapezoids = FuzzyPartitioning.makeTrapezoids(partitions, F);

		System.out.println(trapezoids);
		System.out.println();
	}

	public static void checkPartitioning(String[] args) {
		Setting.forkJoinPool = new ForkJoinPool(3);
		SingleDataSetInfo tra = new SingleDataSetInfo();
		String sep = File.separator;
//		String fileName = "dataset" + sep + "iris" + sep + "a0_0_iris-10tra.dat";
		String fileName = "dataset" + sep + "partition" + sep + "partitions.dat";
//		String fileName = "dataset" + sep + "partition" + sep + "duplicate.dat";
		Input.inputFile(tra, fileName);

		int K = 5;
		double F = 0.5;
		FuzzyPartitioning.startPartition(tra, K, F);


	}

	public static void checkFuzzifyPartition(String[] args) {
		double left = 0.0;
		double right = 2.0;
		double point = 1.5;
		double F = 0.5;

		ArrayList<Double> two = FuzzyPartitioning.fuzzify(left, point, right, F);

		System.out.println(two);
		System.out.println();
	}


	public static void checkSameAntecedent(String[] args) {
		ArrayList<int[]> list = new ArrayList<int[]>();
		list.add(new int[] {0,0});
		list.add(new int[] {1,0});
		list.add(new int[] {0,1});
		list.add(new int[] {0,0});
		list.add(new int[] {0,1});
		list.add(new int[] {1,1});


		//Same Antecedent Judge
		ArrayList<Integer> sameList = new ArrayList<Integer>();
		for(int i = 0; i < list.size(); i++) {
			for(int j = 0; j < i; j++) {
				if(!sameList.contains(j)) {
					if(Arrays.equals(list.get(i), list.get(j))) {
						sameList.add(i);
					}
				}
			}
		}
		System.out.println();
	}

	public static void checkMultiDataSetInfo(String[] args) {
		String sep = File.separator;
		String dataset = "flags";
		String fileName = "dataset" + sep + dataset + sep + "a0_0_" + dataset + "-10tra.dat";
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, fileName);

		System.out.println();
	}

	public static void checkSettingVariables(String[] args) {
		System.out.println(Setting.calclationType);
		Setting.calclationType = 55;
		System.out.println(Setting.calclationType);
	}

	public static void checkStaticIncrement(String[] args) {
		System.out.println(now++);
		System.out.println(now++);
		System.out.println(now++);
	}

	public static void checkSettingValues(String[] args) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmm");
		String sep = File.separator;
		String id = format.format(calendar.getTime());
		//format: ".\result\iris_20191021-1255"
		String resultRoot = System.getProperty("user.dir") + sep + "result" + sep + Setting.dataName + "_" + id;
		Output.mkdirs(resultRoot);
		Output.makeDir(resultRoot, Consts.POPULATION + "_" + id);

		//Output "Experimental Settings"
		String consts = (new Consts()).getStaticValues();
		String settings = (new Setting()).getStaticValues();
		String fileName = resultRoot + sep + "Consts_" + id + ".txt";
		Output.writeln(fileName, consts);
		fileName = resultRoot + sep + "Setting_" + id + ".txt";
		Output.writeln(fileName, settings);
	}

	public static void checkCalendar(String[] args) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmm");
		String sep = File.separator;
		String resultRoot = System.getProperty("user.dir") + sep + "result" + sep + Setting.dataName + "_" + format.format(calendar.getTime());
		System.out.println(resultRoot);
	}

	public static void checkTimeWatcher(String[] args) {
		TimeWatcher time = new TimeWatcher();

		time.start();

		loadPropertyTest(args);

		time.stop();

		System.out.println("Time: " + time.getSec());
		System.out.println();

		loadPropertyTest(args);
		time.start();
		time.stop();

		System.out.println("Time: " + time.getSec());
		System.out.println();
	}

	@SuppressWarnings("rawtypes")
	public static void checkDominatedSort(String[] args) {
		String fileName = "data.dat";
		List<double[]> lines = Output.input(fileName);

		int popSize = lines.size();
		Population<Individual_knapsack> population = new Population<Individual_knapsack>();
		int nowID = 0;
		for(int i = 0; i < popSize; i++) {
			Individual_knapsack individual = new Individual_knapsack(2, 2);
			double[] fitness = new double[2];
			fitness[0] = lines.get(i)[0];
			fitness[1] = lines.get(i)[1];
			individual.setFitness(fitness);
			population.addIndividual(individual);
		}

		NSGA2 nsga2 = new NSGA2();
		int[] optimizer = new int[] {1, 1};
		ArrayList<ArrayList<Individual_nsga2>> F_ = nsga2.nonDominatedSort(population, optimizer);
		for(int i = 0; i < F_.size(); i++) {
			nsga2.crowdingDistanceAssignment(F_.get(i));
		}

		ArrayList<String> strs = new ArrayList<String>();
		String str;
		str = "f1,f2,rank,crowding";
		strs.add(str);

		fileName = "RankAndCrowding.csv";
		for(int p = 0; p < popSize; p++) {
			str = "";
			str += population.getIndividual(p).getFitness(0);
			str += "," + population.getIndividual(p).getFitness(1);
			str += "," + population.getIndividual(p).getRank();
			str += "," + population.getIndividual(p).getCrowding();
			strs.add(str);
		}
		Output.writeln(fileName, strs);

	}

//	public static void checkPittsburghMOP(String[] args) {
//		MersenneTwisterFast rnd = new MersenneTwisterFast(0);
//		ForkJoinPool forkJoinPool = new ForkJoinPool(3);
//
////		String fileName = "kadai3_pattern1.txt";
//		String fileName = "kadai3_pattern2.txt";
//		DataSetInfo kadai3 = new DataSetInfo();
//		Input.inputFile(kadai3, fileName);
//		StaticFuzzyFunc.threeTriangle(kadai3.getNdim());
//
//		PittsburghFGBML mop = new PittsburghFGBML(kadai3, forkJoinPool);
////		int ruleNum = 5;	//pattern1
//		int ruleNum = 3;	//pattern2
//		Pittsburgh individual = new Pittsburgh(kadai3.getNdim(), ruleNum, mop.getObjectiveNum());
//		pittsburghInitTest(individual);
//		individual.gene2ruleset();
//		individual.learning(kadai3, forkJoinPool);
//
//		mop.evaluate(individual);
//		System.out.println("f1: " + (100.0 - individual.getFitness(0)));
//		System.out.println("f2: " + individual.getFitness(1));
//		System.out.println();
//		System.out.println("ruleNum: " + individual.getRuleSet().getRuleNum());
//		System.out.println("ruleLength: " + individual.getRuleSet().getRuleLength());
//		System.out.println();
//	}

	public static void pittsburghInitTest(SinglePittsburgh individual) {
		SingleMichigan[] michigan = new SingleMichigan[individual.getRuleNum()];
		for(int i = 0; i < michigan.length; i++) {
			michigan[i] = new SingleMichigan(2, 1);
		}

//		//kadai3 pattern1
//		Integer[] gene = new Integer[] {0, 3};
//		michigan[0].setGene(gene);
//		michigan[0].gene2rule();
//
//		gene = new Integer[] {1, 2};
//		michigan[1].setGene(gene);
//		michigan[1].gene2rule();
//
//		gene = new Integer[] {2, 1};
//		michigan[2].setGene(gene);
//		michigan[2].gene2rule();
//
//		gene = new Integer[] {2, 2};
//		michigan[3].setGene(gene);
//		michigan[3].gene2rule();
//
//		gene = new Integer[] {3, 0};
//		michigan[4].setGene(gene);
//		michigan[4].gene2rule();

		//kadai3 pattern2
		Integer[] gene = new Integer[] {0, 1};
		michigan[0].setGene(gene);
		michigan[0].gene2rule();

		gene = new Integer[] {0, 3};
		michigan[1].setGene(gene);
		michigan[1].gene2rule();

		gene = new Integer[] {3, 0};
		michigan[2].setGene(gene);
		michigan[2].gene2rule();

		individual.setMichigan(michigan);


		for(int i = 0; i < individual.getRuleNum(); i++) {
			for(int j = 0; j < individual.getNdim(); j++) {
				individual.setGene((i*individual.getNdim() + j), michigan[i].getGene(j));
			}
		}
	}

	public static void checkEx4Pittsburgh(String[] args) {
		MersenneTwisterFast rnd = new MersenneTwisterFast(0);
		ForkJoinPool forkJoinPool = new ForkJoinPool(3);
		String fileName = "kadai3_pattern2.txt";
		SingleDataSetInfo kadai3 = new SingleDataSetInfo();
		Input.inputFile(kadai3, fileName);
		StaticFuzzyFunc.threeTriangle(kadai3.getNdim());

		int ruleNum = 3;
		SinglePittsburgh individual = new SinglePittsburgh(kadai3.getNdim(), ruleNum, 1);
		pittsburghInitTest(individual);
		individual.gene2ruleset();
		individual.learning(kadai3, forkJoinPool);

		for(int i = 0; i < ruleNum; i++) {
			System.out.println("Class " + (1 + individual.getRuleSet().getMicRule(i).getConc()));
		}
		System.out.println();


	}

	public static void checkInitPittsburgh(String[] args) {
		MersenneTwisterFast rnd = new MersenneTwisterFast(0);
		int Ndim = 3;
		int ruleNum  = 5;
		int objectiveNum = 2;

		SinglePittsburgh individual = new SinglePittsburgh(Ndim, ruleNum, objectiveNum);
		individual.initRand(rnd);

	}

	public static void checkEx4(String[] args) {
		ForkJoinPool forkJoinPool = new ForkJoinPool(3);
		String fileName = "kadai3_pattern2.txt";
		SingleDataSetInfo kadai3 = new SingleDataSetInfo();
		Input.inputFile(kadai3, fileName);
		StaticFuzzyFunc.threeTriangle(kadai3.getNdim());

		SingleMichigan rule = new SingleMichigan(kadai3.getNdim(), 1);
		Integer[] gene = new Integer[] {3, 0};
		rule.setGene(gene);
		rule.gene2rule();
		rule.calcRuleConc(kadai3, forkJoinPool);
		System.out.println("Class: " + ((SingleRule)rule.getRule()).getConc());
	}

	public static void checkInputFML(String[] args) {
		KB k = new KB();
		String fileName = "newthyroid.xml";
		k.inputFML(fileName);
	}

	public static void checkGenerics(String[] args) {
//		IntPopManager_nsga2 manager = new IntPopManager_nsga2();
//		IntPopulation_nsga2 population = new IntPopulation_nsga2(5);
//		manager.setCurrentPopulation(population);
//		manager.populationUpdate();
//		IntGene a = new IntGene(5, 2);
//		a.getEntitiy();
	}

	public static void checkQuickSort(String[] args) {
		double[] a = new double[] {5, 3, 9, 9, 0};
		int[] index = new int[a.length];
		for(int i = 0; i < a.length; i++) {
			index[i] = i;
		}
		QuickSort.sort(a, index, 1);

		double[] b = new double[a.length];
		for(int i = 0; i < a.length; i++) {
			b[i] = a[index[i]];
		}

		System.out.println();
	}

	public static void checkMOP(String[] args) {
		Double[] a = new Double[] {5.0, 6.0};
		Double[] b = new Double[] {7.5, 2.0};

		int objectiveNum = 2;
		RealGene popA = new RealGene(a.length, objectiveNum);
		RealGene popB = new RealGene(b.length, objectiveNum);
		popA.setGene(a);
		popB.setGene(b);

		ExampleMOP1 mop1 = new ExampleMOP1();
		ExampleMOP2 mop2 = new ExampleMOP2();

		evaluate(popA, mop1);
		evaluate(popB, mop1);
		System.out.println("MOP1");
		System.out.println(popA.getFitness(0) + ", " + popA.getFitness(1));
		System.out.println(popB.getFitness(0) + ", " + popB.getFitness(1));
		System.out.println();

		evaluate(popA, mop2);
		evaluate(popB, mop2);
		System.out.println("MOP2");
		System.out.println(popA.getFitness(0) + ", " + popA.getFitness(1));
		System.out.println(popB.getFitness(0) + ", " + popB.getFitness(1));
		System.out.println();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void evaluate(Individual a, MOP mop) {
		mop.evaluate(a);
	}

	public static void checkIndividual(String[] args) {
		Double[] a = new Double[] {5.0, 6.0};
		Double[] b = new Double[] {7.5, 2.0};

		int objectiveNum = 2;
		RealGene popA = new RealGene(a.length, objectiveNum);
		RealGene popB = new RealGene(b.length, objectiveNum);

		popA.setGene(a);
		popB.setGene(b);

		//evaluate
		double[] fitA = testEvaluate(popA);
		double[] fitB = testEvaluate(popB);

		popA.setFitness(fitA);
		popB.setFitness(fitB);


		System.out.println(popA.getFitness(0) + ", " + popA.getFitness(1));
		System.out.println(popB.getFitness(0) + ", " + popB.getFitness(1));
		System.out.println();

	}

	@SuppressWarnings("rawtypes")
	public static double[] testEvaluate(Individual pop) {
		double[] fitness = new double[pop.getObjectiveNum()];
		fitness[0] = (double)pop.getGene(0) + (double)pop.getGene(1);
		fitness[1] = (double)pop.getGene(0) - (double)pop.getGene(1);
		return fitness;
	}

//	public static void allCombinationClassifierTest(String[] args) {
//		String sep = File.separator;
//		String data = "iris";
//		String fileName = "dataset" + sep + data + sep + "a0_0_" + data + "-10tra.dat";
//		DataSetInfo dataset = new DataSetInfo();
//
//		inputFile(dataset, fileName);
//
//		MersenneTwisterFast rnd = new MersenneTwisterFast(Setting.seed);
//
//		StaticFuzzyFunc.homogeneousInit(dataset.getNdim());
//
//		AllCombiRuleSet allRuleSet = new AllCombiRuleSet();
//		allRuleSet.init(dataset, Setting.forkJoinPool);
//		double acc = allRuleSet.calcMissRate(dataset, true);
//		System.out.println(acc);
//		System.out.println();
//	}

	public static void checkShapeNewthyroid(String[] args) {

		KB k = new KB();
		String fileName = "newthyroid.xml";
		StaticFuzzyFunc.initFML(fileName);
		k.inputFML(fileName);

		int h = 100;

		double[] x = new double[h+1];
		for(int i = 0; i < h; i++) {
			x[i] = (double)i/(double)h;
		}
		x[h] = 1;

		double[][][] y = new double[k.getFSs().length][][];

		for(int dim_i = 0; dim_i < k.getFSs().length; dim_i++) {
			y[dim_i] = new double[k.getFSs()[dim_i].length][h+1];

			for(int i = 0; i < y[dim_i].length; i++) {
				for(int j = 0; j < x.length; j++) {
					y[dim_i][i][j] = StaticFuzzyFunc.calcMembership(dim_i, i, x[j]);
				}
			}
		}

		//output
		for(int dim_i = 0; dim_i < k.getFSs().length; dim_i++) {
			fileName = "inputFML_dim" + dim_i + ".csv";
			ArrayList<String> strs = new ArrayList<String>();
			String str;
			str = "x,";
			for(int i = 0; i < k.getFSs()[dim_i].length; i++) {
				str += String.valueOf(i) + ",";
			}
			strs.add(str);

			for(int j = 0; j < x.length; j++) {
				str = String.valueOf(x[j]);
				for(int i = 0; i < k.getFSs()[dim_i].length; i++) {
					str += "," + String.valueOf(y[dim_i][i][j]);
				}
				strs.add(str);
			}
			writeln(fileName, strs);

		}

	}

	public static void checkShapeMembershipTest(String[] args) {
		StaticFuzzyFunc.homogeneousInit(1);

		int h = 100;

		double[] x = new double[h+1];
		for(int i = 0; i < h; i++) {
			x[i] = (double)i/(double)h;
		}
		x[h] = 1;

		double[][] y = new double[15][h+1];

		for(int i = 0; i < 15; i++) {
			for(int j = 0; j < x.length; j++) {
				y[i][j] = StaticFuzzyFunc.calcMembership(0, i, x[j]);
			}
		}

		//output
		String sep = File.separator;
		String fileName = "homogeneous.csv";
		ArrayList<String> strs = new ArrayList<String>();
		String str;

		str = "x,";
		for(int i = 0; i < 15; i ++) {
			str += String.valueOf(i) + ",";
		}
		strs.add(str);

		for(int j = 0; j < x.length; j++) {
			str = String.valueOf(x[j]);
			for(int i = 0; i < 15; i++) {
				str += "," + String.valueOf(y[i][j]);
			}
			strs.add(str);
		}

		String[] array = (String[]) strs.toArray(new String[0]);
		writeln(fileName, array);

	}

	public static void calcMembershipTest(String[] args) {
		FuzzySet fs = new FuzzySet("1", FuzzyTermType.TYPE_triangularShape, new float[] {0f, 0.0f, 1f});
		System.out.println("x=0.0, y=" + fs.calcMembership(0.0));
		System.out.println("x=0.5, y=" + fs.calcMembership(0.5));
		System.out.println("x=1.0, y=" + fs.calcMembership(1.0));
	}

	public static void loadPropertyTest(String[] args) {
		String dir = args[0];

		String constsSource = args[1];
		String settingsSource = args[2];

		Consts.setConsts(dir, constsSource);
		Setting.setSettings(dir, settingsSource);


		Setting.dataName = args[3];
		Setting.parallelCores = Integer.parseInt(args[4]);
		Setting.saveDir = args[5];
		//並列用fork join pool 生成
		if(Setting.calclationType == 0) {
			Setting.forkJoinPool = new ForkJoinPool(Setting.parallelCores);
		}


		ForkJoinPool forkJoinPool = Setting.forkJoinPool;
		System.out.println(Setting.parallelCores);
		System.out.println(Setting.forkJoinPool.getParallelism());
		System.out.println(forkJoinPool.getParallelism());
		System.out.println();

//		System.out.println("DATA: " + Consts.DATA);
//		System.out.println("calclationType: " + Setting.calclationType);
	}

	//list用
	public static void writeln(String fileName, ArrayList<String> strs) {
		String[] array = (String[]) strs.toArray(new String[0]);
		writeln(fileName, array);
	}

	//配列用
	public static void writeln(String fileName, String array[]){

		try {
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );
			for(int i=0; i<array.length; i++){
				 pw.println(array[i]);
			}
			pw.close();
	    }
		catch (IOException ex){
			ex.printStackTrace();
	    }
	}

}
