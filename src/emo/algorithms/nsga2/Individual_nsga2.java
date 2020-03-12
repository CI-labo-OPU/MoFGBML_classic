package emo.algorithms.nsga2;

import ga.Individual;
/**
 *
 *
 * @param <T> : The Type of Gene
 */

public abstract class Individual_nsga2<T> extends Individual<T>{
	// ************************************************************
	int rank;
	double crowding = 0;

	// ************************************************************
	public Individual_nsga2() {
		super();
	}

	public Individual_nsga2(int geneNum, int objectiveNum) {
		super(geneNum, objectiveNum);
	}

	public Individual_nsga2(Individual_nsga2<T> individual) {
		super(individual);
	}

	// ************************************************************
	@SuppressWarnings("unchecked")
	@Override
	public void deepCopy(Object individual) {
		Individual_nsga2<T> pop = (Individual_nsga2<T>)individual;
		this.setGene(pop.getGene());
		this.fitness = new double[pop.getFitness().length];
		for(int i = 0; i < this.fitness.length; i++) {
			this.fitness[i] = pop.getFitness(i);
		}
		this.geneNum = pop.getGeneNum();
		this.objectiveNum = pop.getObjectiveNum();

		this.constraint = pop.getConstraint();
		this.feasible = pop.isFeasible();

		//NSGA-II Specific
		this.rank = pop.getRank();
		this.crowding = pop.getCrowding();

		deepCopySpecific(individual);
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getRank() {
		return this.rank;
	}

	public void setCrowding(double crowding) {
		this.crowding = crowding;
	}

	public void addCrowding(double crowding) {
		this.crowding += crowding;
	}

	public double getCrowding() {
		return this.crowding;
	}


}
