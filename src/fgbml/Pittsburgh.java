package fgbml;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ForkJoinPool;

import data.DataSetInfo;
import emo.algorithms.nsga2.Individual_nsga2;
import fuzzy.Rule;
import fuzzy.RuleSet;
import method.MersenneTwisterFast;

@SuppressWarnings("rawtypes")
public abstract class Pittsburgh<T extends RuleSet> extends Individual_nsga2<Integer>{
	// ************************************************************
	protected T ruleSet;
	protected Michigan[] michigan;
	protected int ruleNum;
	protected int Ndim;

	/** 0:Dtra, 1:Dtst, 2:Dsubtra, 3:Dvalid */
	protected double[] appendix;

	// ************************************************************
	public Pittsburgh() {
		super();
	}

	public Pittsburgh(int geneNum, int objectiveNum) {
		super(geneNum, objectiveNum);
	}

	public Pittsburgh(Pittsburgh individual) {
		deepCopy(individual);
	}

	// ************************************************************

	public abstract Pittsburgh<T> newInstance();

	public abstract Pittsburgh<T> newInstance(int Ndim, int ruleNum, int objectiveNum);

	public abstract Pittsburgh<T> newInstance(Object individual);

	/**
	 * Deep Copy<br>
	 * @param michigan
	 */
	public abstract void setMichigan(Michigan[] michigan);

	/**
	 * Deep Copy<br>
	 * For one rule
	 * @param index
	 * @param michigan
	 */
	public abstract void setMichigan(int index, Michigan michigan);

	/**
	 * Deep Copy
	 * @param ruleset
	 */
	public abstract void setRuleSet(T ruleSet);

	public abstract void michigan2ruleset();

	public abstract void ruleset2michigan();

	public abstract void gene2ruleset();


	@SuppressWarnings("unchecked")
	@Override
	public void deepCopySpecific(Object individual) {
		Pittsburgh<T> cast = (Pittsburgh<T>)individual;
		this.ruleNum = cast.getRuleNum();
		this.Ndim = cast.getNdim();

		if( cast.getRuleSet() != null ) {
			setRuleSet( cast.getRuleSet() );
		}
		if( cast.getMichigan() != null ) {
			setMichigan( cast.getMichigan() );
		}

		if( cast.getAppendix() != null) {
			this.appendix = Arrays.copyOf(cast.getAppendix(), cast.getAppendix().length);
		}
	}

	/**
	 * Initialize gene with Random
	 * @param rnd
	 */
	public abstract void initRand(MersenneTwisterFast rnd);

	/**
	 * Initialize gene with Heuristic Rule Generation Method<br>
	 * <br>
	 * 学習用データセット(train)から，非復元抽出でパターンを選択し，
	 * ヒューリスティック生成法を用いてルールを生成する．<br>
	 * <br>
	 * もしも，(パターン数 < ルール数)の場合は，
	 * 復元抽出とする．
	 *
	 * @param train
	 * @param rnd
	 */
	public abstract void initHeuristic(DataSetInfo train, MersenneTwisterFast rnd);

	/**
	 * Michigan[]配列からPittsburgh型遺伝子表現を生成<br>
	 */
	public void michigan2pittsburgh() {
		this.geneNum = ruleNum * Ndim;
		initGene();
		for(int i = 0; i < ruleNum; i++) {
			for(int j = 0; j < Ndim; j++) {
				setGene((i*Ndim + j), (Integer)michigan[i].getGene(j));
			}
		}
	}

	/**
	 * RuleSetクラス内の各Ruleクラスに対して，結論部の学習を行わせるメソッド<br>
	 * @param train : DataSetInfo : 学習用データセット
	 * @param forkJoinPool
	 */
	public void learning(DataSetInfo train, ForkJoinPool forkJoinPool) {
		this.ruleSet.learning(train, forkJoinPool);
	}

	/**
	 * RuleSetクラスとMichiganクラスをRule.fitnessの降順にソートする．<br>
	 */
	@SuppressWarnings("unchecked")
	public void sortMichiganByFitness() {
		Collections.sort(ruleSet.getMicRules(), new Comparator<Rule>() {
			@Override
			public int compare(Rule o1, Rule o2) {
				double no1 = o1.getFitness();
				double no2 = o2.getFitness();
				 //降順でソート
		        if (no1 < no2) {
		            return 1;

		        } else if (no1 == no2) {
		            return 0;

		        } else {
		            return -1;

		        }
			}

		});
	}

	public T getRuleSet() {
		return this.ruleSet;
	}

	public Michigan[] getMichigan() {
		return this.michigan;
	}

	public Michigan getMichigan(int index) {
		return this.michigan[index];
	}

	public void setRuleNum(int ruleNum) {
		this.ruleNum = ruleNum;
	}

	public int getRuleNum() {
		return this.ruleNum;
	}

	public void setNdim(int Ndim) {
		this.Ndim = Ndim;
	}

	public int getNdim() {
		return this.Ndim;
	}

	/**
	 * Appendixとして，欲しい配列のサイズを指定する．
	 * @param num : int
	 */
	public void initAppendix(int num) {
		this.appendix = new double[num];
	}

	/**
	 * <h1>Deep Copy</h1>
	 * 0:Dtra, 1:Dtst, 2:Dsubtra, 3:Dvalid
	 * */
	public void setAppendix(double[] appendix) {
		this.appendix = Arrays.copyOf(appendix, appendix.length);
	}

	/** 0:Dtra, 1:Dtst, 2:Dsubtra, 3:Dvalid */
	public void setAppendix(int index, double appendix) {
		this.appendix[index] = appendix;
	}

	/** 0:Dtra, 1:Dtst, 2:Dsubtra, 3:Dvalid */
	public double getAppendix(int index) {
		return this.appendix[index];
	}

	public double[] getAppendix() {
		return this.appendix;
	}

}
