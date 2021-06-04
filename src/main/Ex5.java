package main;

import java.util.concurrent.ForkJoinPool;

import data.Input;
import data.SingleDataSetInfo;
import data.SinglePattern;
import fuzzy.SingleRule;
import fuzzy.SingleRuleSet;
import fuzzy.StaticFuzzyFunc;

public class Ex5 {
	public static void main(String[] args) {
		String pattern1 = "kadai5_pattern1.txt";
		String pattern2 = "kadai5_pattern2.txt";

		SingleDataSetInfo d1 = new SingleDataSetInfo();
		SingleDataSetInfo d2 = new SingleDataSetInfo();
		Input.inputFile(d1, pattern1);
		Input.inputFile(d2, pattern2);

		// 0:DC, 1:small, 2:medium, 3:large
		StaticFuzzyFunc.threeTriangle(d1.getNdim());

		ForkJoinPool forkJoinPool = new ForkJoinPool(1);

		int DC = 0;
		int small = 1;
		int medium = 2;
		int large = 3;

		String[] fuzzy = {"DC", "small", "medium", "large"};

		int Cnum = d1.getCnum();

//		int[] antecedent = new int[] {DC, large};
//		SingleRule rule = new SingleRule(antecedent);
//		rule.calcRuleConc(d1, forkJoinPool);
//
//		int C = rule.getConc() + 1;
//		double CF = rule.getCf();
//
//		// Output
//		System.out.println("{" + fuzzy[antecedent[0]] + ", " + fuzzy[antecedent[1]] + "}");
//		System.out.println("C: class " + C);
//		System.out.println("CF: " + CF);

//		SingleRuleSet ruleSet = new SingleRuleSet();
//		for(int i = 0; i < 4; i++) {
//			for(int j = 0; j < 4; j++) {
//				int[] antecedent = new int[] {i, j};
//				SingleRule rule = new SingleRule(antecedent);
//				rule.calcRuleConc(d1, forkJoinPool);
//				ruleSet.addRule(rule);
//			}
//		}
		
		int[][] As = new int[][] {
			new int[] {1, 2},
			new int[] {2, 1},
			new int[] {2, 2},
			new int[] {0, 3},
		};
		
		SingleRuleSet ruleSet = new SingleRuleSet();
		for(int i = 0; i < As.length; i++) {
			int[] antecedent = As[i];
			SingleRule rule = new SingleRule(antecedent);
			rule.calcRuleConc(d1, forkJoinPool);
			ruleSet.addRule(rule);
			System.out.println("" + i + ": " + rule.getCf());
		}

//		ruleSet.learning(d1, forkJoinPool);
		ruleSet.calcRuleLength();
		
		for(int i = 0; i < ruleSet.getRuleNum(); i++) {
			SingleRule rule = ruleSet.getMicRule(i);
			double CF = rule.getCf();
			int[] antecedent = rule.getRule();
			if(CF <= 0) {
				System.out.print("A: {" + fuzzy[antecedent[0]] + ", " + fuzzy[antecedent[1]] + "}");
				System.out.println(" " + CF);
			}
		}

		ruleSet.removeRule();
		ruleSet.calcRuleLength();


//		double[] x = new double[] {0.9, 0.98, 1};
//		SinglePattern pattern = new SinglePattern(0, x);
//		int[] C = ruleSet.classify(pattern, false);

		double count = 0;
		for(int p = 0; p < d1.getDataSize(); p++) {
			SinglePattern pattern = d1.getPattern(p);
			int C = ruleSet.classify(pattern, false)[0];
			if(C == pattern.getConClass()) count++;
		}
		
		System.out.println(count/60.0);
		System.out.println(ruleSet.getRuleNum());




	}
}
