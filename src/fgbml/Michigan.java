package fgbml;

import java.util.concurrent.ForkJoinPool;

import data.DataSetInfo;
import data.Pattern;
import emo.algorithms.nsga2.Individual_nsga2;
import fuzzy.Rule;
import fuzzy.StaticFuzzyFunc;
import main.Consts;
import method.MersenneTwisterFast;

@SuppressWarnings("rawtypes")
public abstract class Michigan<T extends Rule> extends Individual_nsga2<Integer>{
	// ************************************************************
	protected T rule;

	// ************************************************************
	public Michigan() {
		super();
	}

	public Michigan(int geneNum, int objectiveNum) {
		super(geneNum, objectiveNum);
	}

	public Michigan(Michigan individual) {
		deepCopy(individual);
	}

	// ************************************************************

	/**
	 * GAの遺伝子表現からMichiganファジィルール(Ruleクラス)を生成<br>
	 * ただし，このとき結論部の計算は行わない．<br>
	 *
	 */
	public abstract void gene2rule();

	public abstract Michigan<T> newInstance();

	public abstract Michigan<T> newInstance(int geneNum, int objectiveNum);

	public abstract Michigan<T> newInstance(Object individual);


	@Override
	public void deepCopySpecific(Object individual) {
		if( ((Michigan)individual).getRule() != null ) {
			setRule( ((Michigan)individual).getRule() );
		}
	}

	/**
	 * Fuzzy Rule(Rule Class)からGAの遺伝子表現を生成<br>
	 */
	public void rule2gene() {
		this.geneNum = rule.getRule().length;
		initGene();
		for(int i = 0; i < geneNum; i++) {
			setGene(i, rule.getRule(i));
		}
	}

	/**
	 * Initialize gene with Random<br>
	 * @param rnd
	 */
	public void initRand(MersenneTwisterFast rnd) {
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		boolean isProb = Consts.IS_PROBABILITY_DONT_CARE;	//Don't Care適用確率(定数)を使用するかどうか
		double dcRate;
		if(isProb) {
			//Don't Care適用確率
			dcRate = Consts.DONT_CARE_RT;
		} else {
			// (Ndim - Const) / Ndim
			dcRate = (double)(((double)geneNum - (double)Consts.ANTECEDENT_LEN)/(double)geneNum);
		}

		for(int n = 0; n < geneNum; n++) {
			if(uniqueRnd.nextDoubleIE() < dcRate) {
				//Don't Care
				setGene(n, 0);
			} else {
				//Fuzzy Set
				setGene(n, uniqueRnd.nextInt(Consts.FUZZY_SET_NUM) + 1);
			}
		}
	}

	/**
	 * Initialize gene with Heuristic Rule Generation Method<br>
	 * @param line
	 * @param rnd
	 */
	public void initHeuristic(Pattern line, MersenneTwisterFast rnd) {
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(rnd.nextInt());

		//Heuristic Rule Generation Method
		int[] rule = StaticFuzzyFunc.heuristicGeneration(line, uniqueRnd);

		//Rule to Gene
		for(int i = 0; i < geneNum; i++) {
			setGene(i, rule[i]);
		}

	}

	/**
	 * Ruleクラスの結論部を計算する<br>
	 * @param train : DataSetInfo
	 * @param forkJoinPool
	 */
	public void calcRuleConc(DataSetInfo train, ForkJoinPool forkJoinPool) {
		rule.calcRuleConc(train, forkJoinPool);
	}

	/**
	 * Deep Copy
	 * @param rule
	 */
	@SuppressWarnings("unchecked")
	public void setRule(Rule rule) {
		try {
			Class<?> entity = rule.getClass();
			T clone = (T)entity.newInstance();
			clone.deepCopy(rule);
			this.rule = clone;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public T getRule() {
		return this.rule;
	}

}
