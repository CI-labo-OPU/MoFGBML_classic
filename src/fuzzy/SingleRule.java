package fuzzy;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

import data.DataSetInfo;
import data.SingleDataSetInfo;

public class SingleRule extends Rule{
	// ************************************************************

	// ************************************************************
	public SingleRule() {
		this.conclusion = new int[1];
	}

	/**
	 * Initialize
	 * @param Ndim int : 次元数
	 */
	public SingleRule(int Ndim) {
		this.conclusion = new int[1];
		this.rule = new int[Ndim];
	}

	/**
	 * Initialize
	 * @param rule int[] : 前件部
	 */
	public SingleRule(int[] rule) {
		this.conclusion = new int[1];
		this.rule = Arrays.copyOf(rule, rule.length);
	}

	/**
	 * Deep Copy
	 * @param rule
	 */
	public SingleRule(SingleRule rule) {
		deepCopy(rule);
	}

	// ************************************************************

	@Override
	public Rule newInstance() {
		return new SingleRule();
	}

	public Rule newInstance(int Ndim) {
		return new SingleRule(Ndim);
	}

	public Rule newInstance(int[] rule) {
		return new SingleRule(rule);
	}

	public Rule newInstance(SingleRule rule) {
		return new SingleRule(rule);
	}

	@Override
	public void deepCopySpecific(Object rule) {}


	//結論部の計算
	@SuppressWarnings("rawtypes")
	@Override
	public void calcRuleConc(DataSetInfo train, ForkJoinPool forkJoinPool) {
		double[] trust = StaticFuzzyFunc.calcTrust((SingleDataSetInfo)train, rule, forkJoinPool);
		conclusion[0] = StaticFuzzyFunc.calcConclusion(trust);
		cf = StaticFuzzyFunc.calcCf(conclusion[0], trust);
		ruleLength = calcRuleLength();
	}

	public int getConc() {
		return this.conclusion[0];
	}

}







