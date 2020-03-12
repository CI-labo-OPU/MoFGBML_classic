package fgbml.multilabel;

import java.util.Arrays;

import data.DataSetInfo;
import data.Pattern;
import fgbml.Michigan;
import fgbml.Pittsburgh;
import fuzzy.multi_label.MultiRule;
import fuzzy.multi_label.MultiRuleSet;
import method.MersenneTwisterFast;
import method.StaticFunction;

public class MultiPittsburgh extends Pittsburgh<MultiRuleSet>{

	// ************************************************************
	protected int Cnum;

	// ************************************************************
	public MultiPittsburgh() {
		super();
	}

	/**
	 * 個体によってルール数が変わる = 遺伝子座の長さが変わる<br>
	 * つまり，各個体ごとに遺伝子座配列の初期化長が違うことのためのコンストラクタ．<br>
	 *
	 * @param Ndim : int : 次元数
	 * @param ruleNum : int : ルール数
	 * @param objectiveNum : int : 目的数
	 * @param Cnum : int : ラベル数
	 */
	public MultiPittsburgh(int Ndim, int ruleNum, int objectiveNum) {
		super(Ndim*ruleNum, objectiveNum);	//geneNum = Ndim*ruleNum
		setNdim(Ndim);
		setRuleNum(ruleNum);
		this.michigan = new MultiMichigan[ruleNum];
	}
//	/**
//	 * 個体によってルール数が変わる = 遺伝子座の長さが変わる<br>
//	 * つまり，各個体ごとに遺伝子座配列の初期化長が違うことのためのコンストラクタ．<br>
//	 *
//	 * @param Ndim : int : 次元数
//	 * @param ruleNum : int : ルール数
//	 * @param objectiveNum : int : 目的数
//	 * @param Lnum : int : ラベル数
//	 */
//	public MultiPittsburgh(int Ndim, int ruleNum, int objectiveNum, int Lnum) {
//		super(Ndim*ruleNum, objectiveNum);	//geneNum = Ndim*ruleNum
//		setNdim(Ndim);
//		setRuleNum(ruleNum);
//		setLnum(Lnum);
//		this.michigan = new MultiMichigan[ruleNum];
//	}

	public MultiPittsburgh(MultiPittsburgh individual) {
		deepCopy(individual);
	}

	// ************************************************************

	@Override
	public Pittsburgh<MultiRuleSet> newInstance() {
		MultiPittsburgh instance = new MultiPittsburgh();
		instance.setCnum(this.Cnum);
		return instance;
	}

	@Override
	public Pittsburgh<MultiRuleSet> newInstance(int Ndim, int ruleNum, int objectiveNum) {
		MultiPittsburgh instance = new MultiPittsburgh(Ndim, ruleNum, objectiveNum);
		instance.setCnum(this.Cnum);
		return instance;
	}

	@Override
	public Pittsburgh<MultiRuleSet> newInstance(Object individual) {
		MultiPittsburgh instance = new MultiPittsburgh((MultiPittsburgh)individual);
		instance.setCnum(this.Cnum);
		return instance;
	}


	@Override
	public void deepCopySpecific(Object individual) {
		MultiPittsburgh cast = (MultiPittsburgh)individual;
		this.ruleNum = cast.getRuleNum();
		this.Ndim = cast.getNdim();
		this.Cnum = cast.getCnum();

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
			michigan[i] = new MultiMichigan(Ndim, objectiveNum);
			((MultiMichigan)michigan[i]).setCnum(Cnum);
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
	@Override
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
			michigan[i] = new MultiMichigan(Ndim, objectiveNum);
			((MultiMichigan)michigan[i]).setCnum(Cnum);
			michigan[i].initHeuristic((Pattern)train.getPattern(index[i]), uniqueRnd);;
			michigan[i].gene2rule();
		}

		//Get Pittsburgh type Gene from Michigan Individual
		michigan2pittsburgh();
		gene2ruleset();
	}

	/**
	 * GAの遺伝子表現からPittsburghファジィルール集合(RuleSetクラス)を生成<br>
	 * ただし，このとき，各ルールの結論部は学習されない．<br>
	 * learningメソッドなどで学習させる必要がある．<br>
	 *
	 */
	public void gene2ruleset() {
		this.ruleSet = new MultiRuleSet();
		for(int i = 0; i < ruleNum; i++) {
			michigan[i].gene2rule();
			this.ruleSet.addRule((MultiRule)michigan[i].getRule());
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
		this.michigan = new MultiMichigan[ruleNum];
		for(int i = 0; i < ruleNum; i++) {
			this.michigan[i] = new MultiMichigan(Ndim, objectiveNum);
			((MultiMichigan)this.michigan[i]).setCnum(Cnum);
			this.michigan[i].setRule(ruleSet.getMicRule(i));
			this.michigan[i].rule2gene();
		}
	}

	public void michigan2ruleset() {
		ruleSet.getMicRules().clear();
		for(int i = 0; i < ruleNum; i++) {
			ruleSet.getMicRules().add((MultiRule)michigan[i].getRule());
		}
		ruleSet.calcRuleLength();
	}

	/**
	 * Deep Copy
	 * @param ruleset
	 */
	@Override
	public void setRuleSet(MultiRuleSet ruleSet) {
		this.ruleSet = new MultiRuleSet(ruleSet);
	}

	public MultiRuleSet getRuleSet() {
		return this.ruleSet;
	}

	/**
	 * Deep Copy
	 * @param michigan
	 */
	@Override
	public void setMichigan(Michigan[] michigan) {
		this.michigan = new MultiMichigan[michigan.length];
		for(int i = 0; i < michigan.length; i++) {
			this.michigan[i] = new MultiMichigan((MultiMichigan)michigan[i]);
		}
	}

	/**
	 * Deep Copy
	 * @param index
	 * @param michigan
	 */
	@Override
	public void setMichigan(int index, Michigan michigan) {
		this.michigan[index] = new MultiMichigan((MultiMichigan)michigan);
	}

	public void setCnum(int Cnum) {
		this.Cnum = Cnum;
	}

	public int getCnum() {
		return this.Cnum;
	}

}
