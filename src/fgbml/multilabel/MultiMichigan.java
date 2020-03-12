package fgbml.multilabel;

import fgbml.Michigan;
import fuzzy.multi_label.MultiRule;

public class MultiMichigan extends Michigan<MultiRule> {
	// ************************************************************
	int Cnum;

	// ************************************************************
	public MultiMichigan() {
		super();
	}

	public MultiMichigan(int geneNum, int objectiveNum) {
		super(geneNum, objectiveNum);
	}

	public MultiMichigan(MultiMichigan individual) {
		deepCopy(individual);
	}

	// ************************************************************

	@Override
	public Michigan<MultiRule> newInstance() {
		MultiMichigan instance = new MultiMichigan();
		return instance;
	}

	@Override
	public Michigan<MultiRule> newInstance(int geneNum, int objectiveNum) {
		MultiMichigan instance = new MultiMichigan(geneNum, objectiveNum);
		return instance;
	}

	@Override
	public Michigan<MultiRule> newInstance(Object individual) {
		MultiMichigan instance = new MultiMichigan((MultiMichigan)individual);
		return instance;
	}

	@Override
	public void deepCopySpecific(Object individual) {
		if( ((MultiMichigan)individual).getRule() != null ) {
			setRule( ((MultiMichigan)individual).getRule() );
		}

		this.Cnum = ((MultiMichigan)individual).getCnum();
	}

	@Override
	public void gene2rule() {
		//Antecedent Part
		int[] rule = new int[geneNum];
		for(int i = 0; i < geneNum; i++) {
			rule[i] = (int)getGene(i);
		}
		this.rule = new MultiRule(rule, Cnum);
	}

	public void setCnum(int Cnum) {
		this.Cnum = Cnum;
	}

	public int getCnum() {
		return this.Cnum;
	}

}
