package emo.problems.example;

import emo.problems.MOP;
import ga.Individual;

public class ExampleMOP2 extends MOP<Individual<Integer>> {
	// ************************************************************
	int objectiveNum = 2;

	// ************************************************************

	// ************************************************************

	@Override
	public void evaluate(Individual<Integer> individual) {
		double[] fitness = new double[objectiveNum];
		fitness[0] = (double)individual.getGene(0) + (double)individual.getGene(1) + 2;
		fitness[1] = (double)individual.getGene(0) - (double)individual.getGene(1) + 2;
		individual.setFitness(fitness);
	}

	@Override
	public void evaluateParallel(Individual<Integer> individual) {
		double[] fitness = new double[objectiveNum];
		fitness[0] = (double)individual.getGene(0) + (double)individual.getGene(1) + 2;
		fitness[1] = (double)individual.getGene(0) - (double)individual.getGene(1) + 2;
		individual.setFitness(fitness);
	}

}
