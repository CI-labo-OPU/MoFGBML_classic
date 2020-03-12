package fgbml;

import java.util.Arrays;

import data.DataSetInfo;
import data.Pattern;
import fuzzy.SingleRule;
import fuzzy.SingleRuleSet;
import method.MersenneTwisterFast;
import method.StaticFunction;

public class SinglePittsburgh extends Pittsburgh<SingleRuleSet>{

	// ************************************************************
	boolean evaluated = false;
	// ************************************************************
	public SinglePittsburgh() {
		super();
	}

	/**
	 * 個体によってルール数が変わる = 遺伝子座の長さが変わる<br>
	 * つまり，各個体ごとに遺伝子座配列の初期化長が違うことのためのコンストラクタ．<br>
	 *
	 * @param Ndim : int : 次元数
	 * @param ruleNum : int : ルール数
	 * @param objectiveNum : int : 目的数
	 */
	public SinglePittsburgh(int Ndim, int ruleNum, int objectiveNum) {
		super(Ndim*ruleNum, objectiveNum);	//geneNum = Ndim*ruleNum
		setNdim(Ndim);
		setRuleNum(ruleNum);
		this.michigan = new SingleMichigan[ruleNum];
	}

	/**
	 * Deep Copy<br>
	 * @param individual
	 */
	public SinglePittsburgh(SinglePittsburgh individual) {
		deepCopy(individual);
	}

	// ************************************************************

	@Override
	public Pittsburgh<SingleRuleSet> newInstance() {
		SinglePittsburgh instance = new SinglePittsburgh();
		return instance;
	}

	@Override
	public Pittsburgh<SingleRuleSet> newInstance(int Ndim, int ruleNum, int objectiveNum) {
		SinglePittsburgh instance = new SinglePittsburgh(Ndim, ruleNum, objectiveNum);
		return instance;
	}

	@Override
	public Pittsburgh<SingleRuleSet> newInstance(Object individual) {
		SinglePittsburgh instance = new SinglePittsburgh((SinglePittsburgh)individual);
		return instance;
	}

	@Override
	public void deepCopySpecific(Object individual) {
		SinglePittsburgh cast = (SinglePittsburgh)individual;
		this.ruleNum = cast.getRuleNum();
		this.Ndim = cast.getNdim();
		this.evaluated = cast.getEvaluated();

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
	public void initRand(MersenneTwisterFast rnd) {
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(rnd.nextInt());

		//Initialize Each Fuzzy Rules with Random
		for(int i = 0; i < ruleNum; i++) {
			michigan[i] = new SingleMichigan(Ndim, objectiveNum);
			michigan[i].initRand(uniqueRnd);
			michigan[i].gene2rule();
		}

		//Get Pittsburgh type Gene from Michigan Individuals
		michigan2pittsburgh();
		gene2ruleset();
	}

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
	@SuppressWarnings("rawtypes")
	public void initHeuristic(DataSetInfo train, MersenneTwisterFast rnd) {
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		Integer[] index = new Integer[ruleNum];

		if(ruleNum > train.getDataSize()) {
			for(int i = 0; i < ruleNum; i++) {
				index[i] = uniqueRnd.nextInt(train.getDataSize());
			}
		}
		else {
			index = StaticFunction.sampringWithout(train.getDataSize(), ruleNum, uniqueRnd);
		}

		//Initialize Each Fuzzy Rules with Heuristic Rule Generation Method
		for(int i = 0; i < ruleNum; i++) {
			michigan[i] = new SingleMichigan(Ndim, objectiveNum);
			michigan[i].initHeuristic((Pattern)train.getPattern(index[i]), uniqueRnd);;
			michigan[i].gene2rule();
		}

		//Get Pittsburgh type Gene from Michigan Individual
		michigan2pittsburgh();
		gene2ruleset();
	}

//	/**
//	 * Michigan[]配列からPittsburgh型遺伝子表現を生成<br>
//	 */
//	public void michigan2pittsburgh() {
//		this.geneNum = ruleNum * Ndim;
//		initGene();
//		for(int i = 0; i < ruleNum; i++) {
//			for(int j = 0; j < Ndim; j++) {
//				setGene((i*Ndim + j), michigan[i].getGene(j));
//			}
//		}
//	}

	/**
	 * GAの遺伝子表現からPittsburghファジィルール集合(RuleSetクラス)を生成<br>
	 * ただし，このとき，各ルールの結論部は学習されない．<br>
	 * learningメソッドなどで学習させる必要がある．<br>
	 *
	 */
	public void gene2ruleset() {
		this.ruleSet = new SingleRuleSet();
		for(int i = 0; i < ruleNum; i++) {
			michigan[i].gene2rule();
			this.ruleSet.addRule((SingleRule)michigan[i].getRule());
		}
		this.ruleSet.calcRuleLength();
	}

	/**
	 * RuleSetクラスからMichiganクラスへRuleクラスをセットする<br>
	 * Michigan.setRule(Rule rule)と，Michigan.rule2gene()を行う．<br>
	 * 結果，Michiganクラスのgene[]とruleはRuleSetクラスからセットされる．<br>
	 */
	public void ruleset2michigan() {
		this.ruleSet.calcRuleLength();
		this.ruleNum = ruleSet.getMicRules().size();
		this.michigan = new SingleMichigan[ruleNum];
		for(int i = 0; i < ruleNum; i++) {
			this.michigan[i] = new SingleMichigan(Ndim, objectiveNum);
			this.michigan[i].setRule(ruleSet.getMicRule(i));
			this.michigan[i].rule2gene();
		}
	}

	public void michigan2ruleset() {
		ruleSet.getMicRules().clear();
		for(int i = 0; i < ruleNum; i++) {
			ruleSet.getMicRules().add((SingleRule)michigan[i].getRule());
		}
		ruleSet.calcRuleLength();
	}

//	/**
//	 * RuleSetクラス内の各Ruleクラスに対して，結論部の学習を行わせるメソッド<br>
//	 * @param train : DataSetInfo : 学習用データセット
//	 * @param forkJoinPool
//	 */
//	@SuppressWarnings("rawtypes")
//	@Override
//	public void learning(DataSetInfo train, ForkJoinPool forkJoinPool) {
//		this.ruleSet.learning(train, forkJoinPool);
//	}

//	/**
//	 * RuleSetクラスとMichiganクラスをRule.fitnessの降順にソートする．<br>
//	 */
//	public void sortMichiganByFitness() {
//		Collections.sort(ruleSet.getMicRules(), new Comparator<SingleRule>() {
//			@Override
//			public int compare(SingleRule o1, SingleRule o2) {
//				double no1 = o1.getFitness();
//				double no2 = o2.getFitness();
//				 //降順でソート
//		        if (no1 < no2) {
//		            return 1;
//
//		        } else if (no1 == no2) {
//		            return 0;
//
//		        } else {
//		            return -1;
//
//		        }
//			}
//
//		});
//	}

	/**
	 * Deep Copy
	 * @param ruleset
	 */
	@Override
	public void setRuleSet(SingleRuleSet ruleSet) {
		this.ruleSet = new SingleRuleSet(ruleSet);
	}

//	public SingleRuleSet getRuleSet() {
//		return this.ruleSet;
//	}

	/**
	 * Deep Copy
	 * @param michigan
	 */
	@Override
	public void setMichigan(Michigan[] michigan) {
		this.michigan = new SingleMichigan[michigan.length];
		for(int i = 0; i < michigan.length; i++) {
			this.michigan[i] = new SingleMichigan((SingleMichigan)michigan[i]);
		}
	}

	/**
	 * Deep Copy
	 * @param index
	 * @param michigan
	 */
	@Override
	public void setMichigan(int index, Michigan michigan) {
		this.michigan[index] = new SingleMichigan((SingleMichigan)michigan);
	}

	public void setEvaluated(boolean evaluated) {
		this.evaluated = evaluated;
	}

	public boolean getEvaluated() {
		return this.evaluated;
	}

//	public SingleMichigan[] getMichigan() {
//		return this.michigan;
//	}

//	public SingleMichigan getMichigan(int index) {
//		return this.michigan[index];
//	}

}
