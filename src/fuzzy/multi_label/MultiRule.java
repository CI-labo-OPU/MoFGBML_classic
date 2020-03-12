package fuzzy.multi_label;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

import data.DataSetInfo;
import data.MultiDataSetInfo;
import data.MultiPattern;
import fuzzy.Rule;
import fuzzy.StaticFuzzyFunc;

public class MultiRule extends Rule{
	// ************************************************************
	/**
	 * Rule weights for each label.<br>
	 * Origirnal "cf" variable (in Rule Class) means the mean of the CFs for each label.<br>
	 *
	 */
	double[] cfVector;

	// ************************************************************
	public MultiRule() {}

	/**
	 * Initialize
	 * @param rule : int[] : Antecedent Part
	 * @param Lnum : int : #of Labels
	 */
	public MultiRule(int[] rule, int Lnum) {
		this.rule = Arrays.copyOf(rule, rule.length);
		this.conclusion = new int[Lnum];
		this.cfVector = new double[Lnum];
	}

	/**
	 * Initialize
	 * @param Ndim : int : #of Features
	 * @param Lnum : int : #of Labels
	 */
	public MultiRule(int Ndim, int Lnum) {
		this.rule = new int[Ndim];
		this.conclusion = new int[Lnum];
		this.cfVector = new double[Lnum];
	}

	/**
	 * Deep Copy
	 * @param rule : MultiRule
	 */
	public MultiRule(MultiRule rule) {
		deepCopy(rule);
	}


	// ************************************************************

	@Override
	public Rule newInstance() {
		return new MultiRule();
	}

	public Rule newInstance(int Ndim, int Lnum) {
		return new MultiRule(Ndim, Lnum);
	}

	public Rule newInstance(int[] rule, int Lnum) {
		return new MultiRule(rule, Lnum);
	}

	public Rule newInstance(MultiRule rule) {
		return new MultiRule(rule);
	}

	@Override
	public void deepCopySpecific(Object rule) {
		MultiRule cast = (MultiRule)rule;
		this.cfVector = Arrays.copyOf(cast.getCFVector(), cast.getCFVector().length);
	}


	@SuppressWarnings("rawtypes")
	@Override
	public void calcRuleConc(DataSetInfo train, ForkJoinPool forkJoinPool) {
		int Cnum = ((MultiDataSetInfo)train).getCnum();
		this.conclusion = new int[Cnum];

		//Step 1. trust[Lnum][2], Lnum:#of Labels, 2:Associate or Non-associate
		double[][] trust = StaticFuzzyFunc.calcTrustMulti((MultiDataSetInfo)train, rule, forkJoinPool);

		//Step 2. Conclusion
		for(int l = 0; l < conclusion.length; l++) {
			conclusion[l] = StaticFuzzyFunc.calcConclusion(trust[l]);
		}

		//Step 3. Rule Weight
		cfVector = StaticFuzzyFunc.calcCfMulti(conclusion, trust);
		double sum = 0;
		for(int l = 0; l < cfVector.length; l++) {
			sum += cfVector[l];
		}
		this.cf = sum / (double)Cnum;

		ruleLength = calcRuleLength();
	}

	/**
	 * <h1>Add rule fitness by judging match for a pattern</h1>
	 * @param pattern
	 */
	public void addFitness(MultiPattern pattern) {
		double match = 0.0;
		int Lnum = pattern.getLnum();
		for(int l = 0; l < Lnum; l++) {
			if(this.conclusion[l] == pattern.getConClass(l)) {
				match += 1.0;
			}
		}

		this.fitness += match / (double)Lnum;

	}

	public void addFitnessVector(MultiPattern pattern, int k) {
		if(this.conclusion[k] == pattern.getConClass(k)) {
			this.fitness += 1.0;
		}
	}

	public int getConc(int index) {
		return this.conclusion[index];
	}

	public int[] getConc() {
		return this.conclusion;
	}

	/**
	 * Deep Copy<br>
	 * @param cfVector
	 */
	public void setCFVector(double[] cfVector) {
		this.cfVector = Arrays.copyOf(cfVector, cfVector.length);
	}

	public double[] getCFVector() {
		return this.cfVector;
	}

	public double getCFVector(int index) {
		return this.cfVector[index];
	}



}
