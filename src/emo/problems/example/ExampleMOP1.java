package emo.problems.example;

import emo.problems.MOP;
import ga.Individual;

/**
 *
 * 新たなMOPを作成する例
 *
 */
public class ExampleMOP1 extends MOP<Individual<Integer>> {
	// ************************************************************
	int objectiveNum = 2;

	// ************************************************************

	// ************************************************************

	@Override
	public void evaluate(Individual<Integer> individual) {
		double[] fitness = new double[objectiveNum];
		fitness[0] = (double)individual.getGene(0) + (double)individual.getGene(1);
		fitness[1] = (double)individual.getGene(0) - (double)individual.getGene(1);
		individual.setFitness(fitness);
	}

	@Override
	public void evaluateParallel(Individual<Integer> individual) {
		double[] fitness = new double[objectiveNum];
		fitness[0] = (double)individual.getGene(0) + (double)individual.getGene(1);
		fitness[1] = (double)individual.getGene(0) - (double)individual.getGene(1);
		individual.setFitness(fitness);
	}

}
