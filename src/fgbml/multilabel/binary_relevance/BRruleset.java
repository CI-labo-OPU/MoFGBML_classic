package fgbml.multilabel.binary_relevance;

import java.util.concurrent.ForkJoinPool;

import data.DataSetInfo;
import data.Pattern;
import fuzzy.RuleSet;
import fuzzy.SingleRuleSet;
import fuzzy.multi_label.MultiRule;

public class BRruleset extends RuleSet<MultiRule>{
	// ************************************************************
	int Cnum;
	SingleRuleSet[] BRrulesets;

	// ************************************************************
	public BRruleset() {}

	/** Deep Copy */
	public BRruleset(SingleRuleSet[] BRrulesets) {
		this.setBRrulesets(BRrulesets);
	}

	public BRruleset(BRruleset ruleSet) {
		deepCopy(ruleSet);
	}

	// ************************************************************
	@Override
	public Object newInstance() {
		BRruleset instance = new BRruleset();
		return instance;
	}

	public Object newInstance(BRruleset ruleSet) {
		BRruleset instance = new BRruleset(ruleSet);
		return instance;
	}

	@Override
	public void deepCopySpecific(Object ruleSet) {
		BRruleset cast = (BRruleset)ruleSet;
		this.Cnum = cast.getCnum();
		this.setBRrulesets(cast.getBRrulesets());
	}

	@Override
	public int[] classify(Pattern pattern, boolean doMemorizeMissPatterns) {
		int[] answerClass = new int[Cnum];

		for(int c = 0; c < Cnum; c++) {
			answerClass[c] = BRrulesets[c].classify(pattern, doMemorizeMissPatterns)[0];
		}

		return answerClass;
	}

	@Override
	public int[] classifyParallel(Pattern pattern, boolean doMemorizeMissPatterns) {
		int[] answerClass = new int[Cnum];
		for(int c = 0; c < Cnum; c++) {
			answerClass[c] = BRrulesets[c].classifyParallel(pattern, doMemorizeMissPatterns)[0];
		}
		return answerClass;
	}

	public void aggregationRuleNum() {
		int ruleNum = 0;
		int ruleLength = 0;
		for(int c = 0; c < Cnum; c++) {
			BRrulesets[c].calcRuleLength();
			ruleNum += BRrulesets[c].getRuleNum();
			ruleLength += BRrulesets[c].getRuleLength();
		}

		this.ruleNum = ruleNum;
		this.ruleLength = ruleLength;
	}


	public int getCnum() {
		return this.Cnum;
	}

	public void setCnum(int Cnum) {
		this.Cnum = Cnum;
	}

	/** Deep Copy */
	public void setBRrulesets(SingleRuleSet[] BRrulesets) {
		this.Cnum = BRrulesets.length;
		this.BRrulesets = new SingleRuleSet[Cnum];

		for(int i = 0; i < Cnum; i++) {
			this.BRrulesets[i] = new SingleRuleSet(BRrulesets[i]);
		}
	}

	public SingleRuleSet[] getBRrulesets() {
		return this.BRrulesets;
	}

	public SingleRuleSet getBRrulesets(int index) {
		return this.BRrulesets[index];
	}

	@SuppressWarnings("rawtypes")
	@Override
	public double calcMissRate(DataSetInfo dataSetInfo, boolean doMemorizeMissPatterns) {
		return 0;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public double calcMissRateParallel(DataSetInfo dataSetInfo, boolean doMemorizeMissPatterns) {
		return 0;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void learning(DataSetInfo train, ForkJoinPool forkJoinPool) {

	}

}
