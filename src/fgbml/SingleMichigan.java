package fgbml;

import fuzzy.SingleRule;

public class SingleMichigan extends Michigan<SingleRule> {
	// ************************************************************

	// ************************************************************
	public SingleMichigan() {
		super();
	}

	public SingleMichigan(int geneNum, int objectiveNum) {
		super(geneNum, objectiveNum);
	}

	public SingleMichigan(SingleMichigan individual) {
		deepCopy(individual);
	}

	// ************************************************************

	@Override
	public Michigan<SingleRule> newInstance() {
		SingleMichigan instance = new SingleMichigan();
		return instance;
	}

	@Override
	public Michigan<SingleRule> newInstance(int geneNum, int objectiveNum) {
		SingleMichigan instance = new SingleMichigan(geneNum, objectiveNum);
		return instance;
	}

	@Override
	public Michigan<SingleRule> newInstance(Object individual) {
		SingleMichigan instance = new SingleMichigan((SingleMichigan)individual);
		return instance;
	}

	/**
	 * GAの遺伝子表現からMichiganファジィルール(Ruleクラス)を生成<br>
	 * ただし，このとき結論部の計算は行わない．<br>
	 *
	 */
	public void gene2rule() {
		//Antecedent Part
		int[] rule = new int[geneNum];
		for(int i = 0; i < geneNum; i++) {
			rule[i] = (int)getGene(i);
		}
		this.rule = new SingleRule(rule);
	}

}
