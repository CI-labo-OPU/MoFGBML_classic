package fuzzy;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

import data.DataSetInfo;
import data.Pattern;

public abstract class Rule {
	// ************************************************************
	protected int[] rule;		//条件部ファジィ集合
	protected int[] conclusion;	//結論部クラス
	protected double cf;		//ルール重み

	protected int ruleLength;	//ルール長

	protected int ncp = 0;		//#of Correct Patterns
	protected int nmp = 0;		//#of Miss Patterns
	protected double fitness = 0.0;	//Count of Win

	// ************************************************************


	// ************************************************************

	public abstract Rule newInstance();

	@SuppressWarnings("rawtypes")
	public abstract void calcRuleConc(DataSetInfo train, ForkJoinPool forkJoinPool);

	public abstract void deepCopySpecific(Object rule);

	public Class<?> getEntity(){
		return this.getClass();
	}

	/**
	 * Deep Copy
	 * @param rule
	 */
	public void deepCopy(Object rule) {
		Rule cast = (Rule)rule;

		this.rule = Arrays.copyOf(cast.rule, cast.rule.length);
		this.conclusion = Arrays.copyOf(cast.conclusion, cast.conclusion.length);

		this.cf = cast.cf;
		this.ruleLength = cast.ruleLength;
		this.ncp = cast.ncp;
		this.nmp = cast.nmp;
		this.fitness = cast.fitness;

		deepCopySpecific(rule);
	}

	/**
	 * <h1>Calculation Condition Length</h1>
	 * @return : int : Condition length of own rule.
	 */
	public int calcRuleLength() {
		int length = 0;
		for(int i = 0; i < rule.length; i++) {
			if(rule[i] != 0) {	//Not "don't care"
				length++;
			}
		}
		this.ruleLength = length;
		return length;
	}

	/**
	 * <h1>The Compatibility Grade between "an antecedent part" and "a pattern".</h1>
	 * @param line : Pattern :
	 * @return double : Compatibility grade
	 */
	public double calcAdaptationPure(Pattern line) {
		return StaticFuzzyFunc.memberMulPure(line, rule);
	}

	/**
	 *
	 * @param dim : int : Attribute Index
	 * @param x : double : input value
	 * @return double : Membership value
	 */
	public double calcMembershipValue(int dim, double x) {
		return StaticFuzzyFunc.calcMembership(dim, rule[dim], x);
	}

	public void addNCP() {
		this.ncp++;
	}

	public int getNCP() {
		return this.ncp;
	}

	public void addNMP() {
		this.nmp++;
	}

	public int getNMP() {
		return this.nmp;
	}

	public void addFitness() {
		this.fitness++;
	}

	public double getFitness() {
		return this.fitness;
	}

	public void clearFitness() {
		this.fitness = 0.0;
	}

	public int getRuleLength() {
		return this.ruleLength;
	}

	public void setRuleLength(int ruleLength) {
		this.ruleLength = ruleLength;
	}

	public void setRule(int index, int fuzzySet) {
		this.rule[index] = fuzzySet;
	}

	public void setRule(int[] rule) {
		this.rule = Arrays.copyOf(rule, rule.length);
	}

	/**
	 * index次元目のファジィ集合を返すメソッド
	 * @param index : int : 次元
	 * @return int : index次元目のファジィ集合
	 */
	public int getRule(int index) {
		return this.rule[index];
	}

	public int[] getRule() {
		return this.rule;
	}

	public double getCf() {
		return this.cf;
	}



}
