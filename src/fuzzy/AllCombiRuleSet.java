package fuzzy;

import java.util.concurrent.ForkJoinPool;

import data.SingleDataSetInfo;

/**
 * 条件部ファジィ集合の全組み合わせで構成されるファジィ識別器
 *
 */
public class AllCombiRuleSet extends SingleRuleSet{

	// ************************************************************

	// ************************************************************
	public AllCombiRuleSet() {
		super();
	}

	public AllCombiRuleSet(AllCombiRuleSet ruleSet) {
		super(ruleSet);
	}

	// ************************************************************
	public void init(SingleDataSetInfo Dtra, ForkJoinPool forkJoinPool) {
		int Ndim = Dtra.getNdim();
		int[] Fdiv = new int[Ndim];
		int ruleNum = 1;

		for(int i = 0; i < Ndim; i++) {
			Fdiv[i] = StaticFuzzyFunc.kb.getFSs(i).length;
			ruleNum *= Fdiv[i];
		}

		//Antecedent Part
		int[][] rule = new int[ruleNum][Ndim];
		for(int i = 0; i < Ndim; i++) {
			int rule_i = 0;
			int repeatNum = 1;
			int interval = 1;
			int count = 0;
			for(int j = 0; j < i; j++) {
				repeatNum *= Fdiv[j];
			}
			for(int j = i+1; j < Ndim; j++) {
				interval *= Fdiv[j];
			}
			for(int j = 0; j < repeatNum; j++) {
				count = 0;
				for(int k = 0; k < Fdiv[i]; k++) {
					for(int l = 0; l < interval; l++) {
						rule[rule_i][i] = count;
						rule_i++;
					}
					count++;
				}
			}
		}

		for(int i = 0; i < ruleNum; i++) {
			addRule(new SingleRule(rule[i]));
		}
		calcRuleLength();

		//Consequent Part
		learning(Dtra, forkJoinPool);
	}
}
