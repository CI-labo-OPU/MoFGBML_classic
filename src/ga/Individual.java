package ga;

import java.util.Arrays;

/**
 *	Individualクラスを継承するときは，遺伝子座の型(Intger or Double, etc)を{@literal <T>}として設定する．<br>
 *	各問題において特別な個体の変数値があるとき，メンバ変数を追加し，deepCopySpecificをオーバーライドする．<br>
 *
 * @param <T> Generics 遺伝子座の型を指定して継承する(Integer or Double, etc)
 *
 */
public abstract class Individual<T> {
	// ************************************************************
	protected int id;
	protected Object[] gene;
	protected double[] fitness;

	protected int geneNum;
	protected int objectiveNum;

	protected double[] constraint;
	protected boolean feasible = true;

	// ************************************************************
	public Individual() {}

	public Individual(int geneNum, int objectiveNum) {
		this.geneNum = geneNum;
		this.objectiveNum = objectiveNum;
		this.fitness = new double[objectiveNum];
		this.initGene();
	}

	public Individual(Individual<T> individual) {
		deepCopy(individual);
	}

	// ************************************************************

	/**
	 * 遺伝子座の型が不明の状態でもPopulationクラスでaddIndividual()するためのメソッド．<br>
	 * This method is implemented for a method "addIndividual()" of class "Population".<br>
	 * <br>
	 * @return Class<?>
	 */
	public Class<?> getEntity(){
		return this.getClass();
	}

	/**
	 * Individualクラス内のフィールドのDeep Copy<br>
	 * Individualを継承したクラス特有のフィールドがある場合は，
	 * deepCopySpecificメソッドでオーバーライドする．<br>
	 * @param individual Object Individualクラスを継承したクラス
	 */
	@SuppressWarnings("unchecked")
	public void deepCopy(Object individual) {
		Individual<T> pop = (Individual<T>)individual;
		this.id = pop.getID();
		this.setGene(pop.getGene());
		this.fitness = new double[pop.getFitness().length];
		for(int i = 0; i < this.fitness.length; i++) {
			this.fitness[i] = pop.getFitness(i);
		}
		this.geneNum = pop.getGeneNum();
		this.objectiveNum = pop.getObjectiveNum();

		this.constraint = Arrays.copyOf(pop.getConstraint(), pop.getConstraint().length);
		this.feasible = pop.isFeasible();

		deepCopySpecific(individual);
	}

	/**
	 * 問題特有の個体のメンバ変数があるとき，このメソッドでDeep Copyする．<br>
	 * If individual has a problem-specific field, you have to override this method as Deep Copy. <br>
	 * <br>
	 * 問題特融のメンバ変数がないときは，空メソッドでオーバライドすればよい．<br>
	 * If individual has no problem-specific field, you should override this method as Empty method.<br>
	 * @param individual DeepCopy元の個体
	 *
	 */
	public abstract void deepCopySpecific(Object individual);

	public void initGene() {
		this.gene = new Object[geneNum];
	}

	public void setGene(T[] gene) {
		this.gene = new Object[gene.length];
		for(int i = 0; i < gene.length; i++) {
			this.gene[i] = gene[i];
		}
	}

	/**
	 *
	 * @param index
	 * @param gene
	 */
	public void setGene(int index, T gene) {
		this.gene[index] = gene;
	}

	@SuppressWarnings("unchecked")
	public T getGene(int index) {
		return (T)this.gene[index];
	}

	@SuppressWarnings("unchecked")
	public T[] getGene() {
		return (T[])this.gene;
	}

	public int getGeneNum() {
		return this.geneNum;
	}

	public void setGeneNum(int geneNum) {
		this.geneNum = geneNum;
	}

	public int getObjectiveNum() {
		return this.objectiveNum;
	}

	public void setObjectiveNum(int objectiveNum) {
		this.objectiveNum = objectiveNum;
	}

	public double getFitness(int index) {
		return this.fitness[index];
	}

	public double[] getFitness() {
		return this.fitness;
	}

	public void setFitness(int index, double fitness) {
		this.fitness[index] = fitness;
	}

	public void setFitness(double[] fitness) {
		this.fitness = Arrays.copyOf(fitness, fitness.length);
	}

	public void setID(int id) {
		this.id = id;
	}

	public int getID() {
		return this.id;
	}

	public void setConstraint(double[] constraint) {
		this.constraint = Arrays.copyOf(constraint, constraint.length);
	}

	public double[] getConstraint() {
		return this.constraint;
	}

	public double getConstraint(int index) {
		return this.constraint[index];
	}

	public void setFeasible(boolean feasible) {
		this.feasible = feasible;
	}

	public boolean isFeasible() {
		return this.feasible;
	}

}
