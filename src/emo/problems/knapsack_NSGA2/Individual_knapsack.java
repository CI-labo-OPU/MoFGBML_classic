package emo.problems.knapsack_NSGA2;

import emo.algorithms.nsga2.Individual_nsga2;
import method.MersenneTwisterFast;

public class Individual_knapsack extends Individual_nsga2<Integer>{
	// ************************************************************
	double sumConst = 0;

	// ************************************************************
	public Individual_knapsack() {}

	public Individual_knapsack(int geneNum, int objectiveNum) {
		super(geneNum, objectiveNum);
		sumConst = 0;
	}

	public Individual_knapsack(Individual_knapsack individual) {
		super(individual);
	}

	// ************************************************************
	@Override
	public void deepCopySpecific(Object individual) {
		Individual_knapsack cast = (Individual_knapsack)individual;
		this.sumConst = cast.getSumConst();
	}

	public void initWithRandom(MersenneTwisterFast rnd) {
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		for(int i = 0; i < this.getGeneNum(); i++) {
			if(uniqueRnd.nextBoolean()) {
				this.setGene(i, 0);
			}
			else {
				this.setGene(i, 1);
			}
		}
	}

	public void setSumConst(double sumConst) {
		this.sumConst = sumConst;
	}

	public double getSumConst() {
		return this.sumConst;
	}

}
