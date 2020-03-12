package fuzzy.multi_label.fuzzieee2020;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import data.Input;
import data.MultiDataSetInfo;
import data.MultiPattern;
import fuzzy.StaticFuzzyFunc;
import fuzzy.multi_label.MultiRule;
import fuzzy.multi_label.MultiRuleSet;
import main.Consts;
import main.Setting;
import method.Output;
import method.StaticFunction;

public class MultiLabel_realExamine {
	public static void setting(String[] args) {
		args = new String[] {"./", "consts", "setting", "5"};
		//設定ファイル読込 - Load .properties
		String currentDir = args[0];
		String constsSource = args[1];
		String settingSource = args[2];
		Consts.setConsts(currentDir, constsSource);
		Setting.setSettings(currentDir, settingSource);
		//コマンドライン引数読込 - Load command line arguments
		Setting.parallelCores = Integer.parseInt(args[3]);
		Setting.forkJoinPool = new ForkJoinPool(Setting.parallelCores);
	}

	public static void main(String[] args) {
		setting(args);
		String sep = File.separator;
		/* ********************************************************* */

		String dataName = "CAL500";
//		String dataName = "emotions";
//		String dataName = "scene";
//		String dataName = "yeast_multi";

		String fileName = "multi-label_real" + sep + dataName + ".dat";
		MultiDataSetInfo dataset = new MultiDataSetInfo();
		Input.inputMultiLabel(dataset, fileName);

		int dataSize = dataset.getDataSize();
		int Ndim = dataset.getNdim();
		int Cnum = dataset.getCnum();

		StaticFuzzyFunc.homogeneousInit(Ndim);

		MultiRuleSet classifier = makeClassifier(dataset);

//		int[] missPatterns = new int[] {458, 485};	//CAL500
//		int[] missPatterns = new int[] {713, 1857, 1937, 1949, 2083};	//scene
//		int head = 0;

		double miss = 0;
		for(int p = 0; p < dataSize; p++) {
			MultiPattern pattern = dataset.getPattern(p);
//			int[] answerClass = classifier.cfMeanClassify(pattern, true);
			int[] answerClass = classifier.cfVectorClassify(pattern, true);
//			int[] answerClass = classifier.getMicRule(p).getConc();

			if(!Arrays.equals(answerClass, pattern.getConClass())) {
				miss++;
				System.out.println(p);
			}
		}

		System.out.println();
		System.out.println(miss);
		System.out.println();

		double fitZero = 0;
		for(int p = 0; p < dataSize; p++) {
			MultiRule rule = classifier.getMicRule(p);
			if(rule.getFitness() > 1) {
				fitZero++;
				System.out.println(p);
			}
		}

		System.out.println();
		System.out.println(fitZero);
		System.out.println();



		String originalClassifier = outputRuleSetCSV(classifier);
		fileName = dataName + "_originalRuleSet.csv";
		Output.writeln(fileName, originalClassifier);

	}

	public static MultiRuleSet makeClassifier(MultiDataSetInfo Dtra) {
		int dataSize = Dtra.getDataSize();
		int Ndim = Dtra.getNdim();
		int Cnum = Dtra.getCnum();

		MultiRuleSet ruleSet = new MultiRuleSet();

		for(int p = 0; p < dataSize; p++) {
			int[] maxLengthRule = heuristicGeneration(Dtra.getPattern(p));

			MultiRule rule = new MultiRule(maxLengthRule, Cnum);
			ruleSet.addRule(rule);
		}

		ruleSet.learning(Dtra, Setting.forkJoinPool);
		ruleSet.calcRuleLength();

		return ruleSet;
	}


	public static String outputRuleSetCSV(MultiRuleSet ruleSet) {
		String ln = System.lineSeparator();
		String strs = "";
		String str = "";

		int ruleNum = ruleSet.getRuleNum();
		int Ndim = ruleSet.getMicRule(0).getRule().length;
		int Cnum = ruleSet.getMicRule(0).getConc().length;

		//Header
		str = "id";
		for(int n = 0; n < Ndim; n++) {
			str += "," + "a" + n;
		}
		for(int c = 0; c < Cnum; c++) {
			str += "," + "c" + c;
		}
		str += "," + "CFmean";
		for(int c = 0; c < Cnum; c++) {
			str += "," + "CFvec_" + c;
		}
		str += "," + "fitness";
		strs += str + ln;

		//Contents
		for(int r = 0; r < ruleNum; r++) {
			MultiRule rule = ruleSet.getMicRule(r);

			//ID
			str = String.valueOf(r);
			//Antecedent
			for(int n = 0; n < Ndim; n++) {
				str += "," + rule.getRule(n);
			}
			//Conclusion Class
			for(int c = 0; c < Cnum; c++) {
				str += "," + rule.getConc(c);
			}
			//CFmean
			str += "," + rule.getCf();
			//CFvector
			for(int c = 0; c < Cnum; c++) {
				str += "," + rule.getCFVector(c);
			}
			//Fitness
			str += "," + rule.getFitness();

			strs += str + ln;
		}

		return strs;
	}


	public static int[] heuristicGeneration(MultiPattern line) {
		int[] rule = new int[line.getNdim()];

		for(int n = 0; n < line.getNdim(); n++) {
			int Fdiv = StaticFuzzyFunc.kb.getFSs(n).length - 1;
			double[] membershipValues = new double[Fdiv];

			for(int f = 0; f < Fdiv; f++) {
				membershipValues[f] = StaticFuzzyFunc.calcMembership(n, f+1, line.getDimValue(n));
			}

			//Max
			double max = -0.1;
			int maxIndex = 0;
			for(int f = 0; f < Fdiv; f++) {
				if(max <= membershipValues[f]) {
					max = membershipValues[f];
					maxIndex = f;
				}
			}

			rule[n] = maxIndex + 1;
		}

		return rule;
	}

	public static int[][] assignDontCare(int[] originalRule, int length) {
		int Ndim = originalRule.length;
		int newNum = StaticFunction.combination(Ndim, length);

		int[][] newRules = new int[newNum][Ndim];

		boolean[][] dcIndex = makeCombination(Ndim, length);

		for(int r = 0; r < newNum; r++) {
			for(int n = 0; n < Ndim; n++) {
				if(dcIndex[r][n]) {
					newRules[r][n] = originalRule[n];
				}
				else {
					newRules[r][n] = 0;
				}
			}
		}

		return newRules;
	}


	public static boolean[][] makeCombination(int n, int r) {
		int num = StaticFunction.combination(n, r);
		boolean[][] combination = new boolean[num][n];

		List<Integer> candidate = new ArrayList<>();
		for(int i = 0; i < n; i++) {
			candidate.add(i);
		}

		List<List<Integer>> result = make(candidate, r);

		for(int i = 0; i < num; i++) {
			Arrays.fill(combination[i], false);

			List<Integer> index = result.get(i);

			for(int j = 0; j < index.size(); j++) {
				int dim = index.get(j);
				combination[i][dim] = true;
			}
		}

		return combination;
	}

    // 候補となるリストと、何個ピックアップするかを渡す
    public static List<List<Integer>> make (List<Integer> candidate, int r) {
        // 5C6みたいなのは空
        // 0C5も空
        // 5C0も空
        if (candidate.size() < r || candidate.size() <= 0 || r <= 0) {
            List<List<Integer>> empty = new ArrayList<>();
            empty.add(new ArrayList<>());
            return empty;
        }

        List<List<Integer>> combination = new ArrayList<>();
        // 5C3だったら、添字0, 1, 2だけ考えたらいい
        for (int i = 0; i <= candidate.size() - r; i++) {
            // 一つ取り出して
            Integer picked = candidate.get(i);
            List<Integer> rest = new ArrayList<>(candidate);
            // 以降の文字を削って
            rest.subList(0, i + 1).clear();
            // 再帰呼び出しし、得られたリストの全ての先頭に取り出したものを結合する
            combination.addAll(make(rest, r - 1).stream().map(list -> {
                list.add(0, picked);
                return list;
            }).collect(Collectors.toList()));
        }
        return combination;
    }
}

























