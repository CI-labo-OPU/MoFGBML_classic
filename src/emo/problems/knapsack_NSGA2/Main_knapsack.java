package emo.problems.knapsack_NSGA2;

import java.io.File;
import java.util.ArrayList;

import emo.algorithms.nsga2.Individual_nsga2;
import emo.algorithms.nsga2.NSGA2;
import ga.GAFunctions;
import ga.Population;
import ga.PopulationManager;
import method.MersenneTwisterFast;
import method.Output;

public class Main_knapsack {

	//Experimental Settings
//	static CNSGA2_knapsack algorithm = new CNSGA2_knapsack();
	static NSGA2 algorithm = new NSGA2();
	static int generation = 400;	//#of Generation
	static int timing = 100;	//Timing of Outputing Population
	static int pSize = 100;	//Population Size
	static int qSize = 100;	//Offspring Size
	static int objectiveNum = 2;
	static int[] optimizer = new int[] {-1, -1};	//-1: Maximize
	static int itemNum = 500;	//= #of Gene

	static int tournamentSize = 2;	//Size of Tournament
	static double p_X = 0.8;	//Crossover Probability
	static double p_u = 0.5;	//Probability of Uniform Crossover
	static double pm = 2.0 / (double)itemNum;

	public static void makeDirs(String nowPath) {
		Output.makeDir(nowPath, "population");
	}

	public static void main(String[] args) {
		String sep = File.separator;
		String path = System.getProperty("user.dir");
		path += sep + "result_knapsack";	//Result Top Directory
		Output.mkdirs(path);

		int trial = 10;
		for(int seed = 0; seed < trial; seed++) {
			String nowPath = path + sep + "seed" + String.valueOf(seed);
			makeDirs(nowPath);
			mainFrame(seed, nowPath);
		}

	}

	public static void mainFrame(int seed, String nowPath) {
		String sep = File.separator;
		String fileName;
		MersenneTwisterFast rnd = new MersenneTwisterFast(seed);

		//Generate Problem
		Knapsack mop = new Knapsack(objectiveNum, itemNum, rnd);
		mop.init();	//Initialize Items
//		mop.initFromData();
		mop.outputProblem(nowPath + sep + "Knapsack.csv");

		//Step 1. Population Initialization and Evaluation
		PopulationManager<Population<Individual_knapsack>> manager = new PopulationManager<Population<Individual_knapsack>>();
		Population<Individual_knapsack> population = new Population<Individual_knapsack>();
		for(int i = 0; i < pSize; i++) {
			Individual_knapsack individual = new Individual_knapsack(itemNum, objectiveNum);
			Integer[] gene = initGene(rnd);
			gene = repairMethod(gene, mop);
			individual.setGene(gene);
			individual.setID(i);
			mop.evaluate(individual);
			population.addIndividual(individual);
		}
		manager.setPopulation(population);

		// Non dominated Sort
		@SuppressWarnings("rawtypes")
		ArrayList<ArrayList<Individual_nsga2>> F_ = algorithm.nonDominatedSort(manager.getPopulation(), optimizer);
		for(int i = 0; i < F_.size(); i++) {
			algorithm.crowdingDistanceAssignment(F_.get(i));
		}
		//Output Initial Population
		fileName = nowPath + sep + "population" + sep + "generation0" + ".csv";
		Output_knapsack.population(fileName, manager.getPopulation());

		//Step 2. NSGA-II Main Frame
		int t = 0;	//Generation Count
		while(t < generation) {
			offspringGeneration(manager, mop, rnd);
			algorithm.mainFrame(manager, optimizer);
			t++;
			if(t % timing == 0) {
				//Output Population
				fileName = nowPath + sep + "population" + sep + "generation" + String.valueOf(t) + ".csv";
				Output_knapsack.population(fileName, manager.getPopulation());
			}
		}

	}

	public static Integer[] initGene(MersenneTwisterFast rnd) {
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		Integer[] gene = new Integer[itemNum];
		for(int i = 0; i < itemNum; i++) {
			if(uniqueRnd.nextBoolean()) {
				gene[i] = 0;
			}
			else {
				gene[i] = 1;
			}
		}
		return gene;
	}

	public static Integer[] repairMethod(Integer[] gene, Knapsack mop) {
		Integer[] newGene = new Integer[gene.length];
		for(int i = 0; i < gene.length; i++) {
			newGene[i] = (int)gene[i];
		}
		while(!mop.geneFeasible(newGene)) {
			double min = Double.MAX_VALUE;
			int throwItem = 0;

			for(int i = 0; i < itemNum; i++) {
				if(newGene[i] == 0) {
					continue;
				}

				double a = mop.getThrowPreference()[i];
				if(min >= a) {
					min = a;
					throwItem = i;
				}

			}
			newGene[throwItem] = 0;
		}
		return newGene;
	}

	public static void offspringGeneration(PopulationManager<Population<Individual_knapsack>> manager, Knapsack mop, MersenneTwisterFast rnd) {
		manager.setOffspring(new Population<Individual_knapsack>());
		for(int q = 0; q < qSize; q++) {
			//Step 1. Mating Selection
			@SuppressWarnings("rawtypes")
			Individual_nsga2[] parents = algorithm.tournamentSelection(manager.getPopulation(), 2, tournamentSize, rnd);

			//Step 2. Crossover
			Integer[] gene = GAFunctions.uniformCrossover(parents[0], parents[1], p_X, p_u, rnd);

			//Step 3. Mutation
			GAFunctions.bitFlipMutation(gene, pm, rnd);

			//Step 4. Repair Method
			gene = repairMethod(gene, mop);

			//Step 5. Child Evaluation
			Individual_knapsack child = new Individual_knapsack(itemNum, objectiveNum);
			child.setGene(gene);
			mop.evaluate(child);

			manager.getOffspring().addIndividual(child);
		}
	}


}
