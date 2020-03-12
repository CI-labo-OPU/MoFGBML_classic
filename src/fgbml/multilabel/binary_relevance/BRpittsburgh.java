package fgbml.multilabel.binary_relevance;

import data.DataSetInfo;
import fgbml.Michigan;
import fgbml.Pittsburgh;
import fuzzy.multi_label.MultiRuleSet;
import method.MersenneTwisterFast;

public class BRpittsburgh extends Pittsburgh<MultiRuleSet> {
	// ************************************************************

	BRruleset BRruleSet;
	boolean evaluated = false;

	// ************************************************************
	public BRpittsburgh(BRruleset ruleSet) {
		this.BRruleSet = ruleSet;
	}

	// ************************************************************

	public BRruleset getBRruleset() {
		return this.BRruleSet;
	}

	@Override
	public Pittsburgh<MultiRuleSet> newInstance() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Pittsburgh<MultiRuleSet> newInstance(int Ndim, int ruleNum, int objectiveNum) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Pittsburgh<MultiRuleSet> newInstance(Object individual) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setMichigan(Michigan[] michigan) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setMichigan(int index, Michigan michigan) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void setRuleSet(MultiRuleSet ruleSet) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void michigan2ruleset() {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void ruleset2michigan() {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void gene2ruleset() {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void initRand(MersenneTwisterFast rnd) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void initHeuristic(DataSetInfo train, MersenneTwisterFast rnd) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public boolean getEvaluated() {
		return this.evaluated;
	}

	public void setEvaluated(boolean evaluated) {
		this.evaluated = evaluated;
	}


}
