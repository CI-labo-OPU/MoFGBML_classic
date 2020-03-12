package fuzzy.multi_label.fuzzieee2020;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import data.Input;
import data.MultiDataSetInfo;
import data.SingleDataSetInfo;
import fgbml.multilabel.binary_relevance.BRruleset;
import fgbml.multilabel.binary_relevance.MOP_multi;
import fgbml.multilabel.binary_relevance.Problem_BR_Multi;
import fuzzy.SingleRule;
import fuzzy.SingleRuleSet;
import fuzzy.StaticFuzzyFunc;
import main.Consts;
import main.Setting;
import method.Output;


/**
 * BinaryRelevanceのruleSet.txtを再読込後，richromaticの識別境界を出力
 *
 */

public class Examine3 {
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
		/* ********************************************************* */
		String trainName = "a2_0_richromatic-wide-10tra.dat";
		String testName = "a2_0_richromatic-wide-10tst.dat";
		String fileName = "ruleSet_BR_richromatic-wide_trial20.txt";
		/* ********************************************************* */
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		MultiDataSetInfo Dtst = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, trainName);
		Input.inputMultiLabel(Dtst, testName);
		Problem_BR_Multi mop = new MOP_multi(Dtra, Dtst);
		/* ********************************************************* */

		BRruleset ruleSet = loadRuleset(fileName, mop);

		/* ********************************************************* */
		String verticalFile = "richromatic_verticalGrid.csv";
		String horizontalFile = "richromatic_horizontalGrid.csv";
		/* ********************************************************* */
		MultiDataSetInfo vertical = new MultiDataSetInfo();
		MultiDataSetInfo horizontal = new MultiDataSetInfo();
		Input.inputMultiLabel(vertical, verticalFile);
		Input.inputMultiLabel(horizontal, horizontalFile);
		/* ********************************************************* */

		int Cnum = vertical.getCnum();
		int[][] classified_vertical = new int[vertical.getDataSize()][Cnum];
		int[][] classified_horizontal = new int[horizontal.getDataSize()][Cnum];

		Problem_BR_Multi mopGrid = new MOP_multi(vertical, horizontal);

		for(int p = 0; p < vertical.getDataSize(); p++) {
			for(int c = 0; c < Cnum; c++) {
				SingleDataSetInfo dataset = mopGrid.getSingleDataSet(0, c);
				classified_vertical[p][c] = ruleSet.classify(dataset.getPattern(p), false)[c];
			}
		}
		for(int p = 0; p < horizontal.getDataSize(); p++) {
			for(int c = 0; c < Cnum; c++) {
				SingleDataSetInfo dataset = mopGrid.getSingleDataSet(1, c);
				classified_horizontal[p][c] = ruleSet.classify(dataset.getPattern(p), false)[c];
			}
		}

		/* ********************************************************* */
		//Output
		ArrayList<String> strs = new ArrayList<>();
		String str = "";

		//vertical
		str = "";
		str += vertical.getDataSize() + "," + vertical.getNdim() + "," + vertical.getCnum();
		strs.add(str);
		for(int p = 0; p < vertical.getDataSize(); p++) {
			str = "";
			str += vertical.getPattern(p).getDimValue(0);
			str += "," + vertical.getPattern(p).getDimValue(1);
			for(int c = 0; c < Cnum; c++) {
				str += "," + classified_vertical[p][c];
			}
			str += ",";
			strs.add(str);
		}
		String output = "vertical_" + fileName.split("\\.")[0] + ".csv";
		Output.writeln(output, strs);

		//horizontal
		strs.clear();
		str = "";
		str += horizontal.getDataSize() + "," + horizontal.getNdim() + "," + horizontal.getCnum();
		strs.add(str);
		for(int p = 0; p < horizontal.getDataSize(); p++) {
			str = "";
			str += horizontal.getPattern(p).getDimValue(0);
			str += "," + horizontal.getPattern(p).getDimValue(1);
			for(int c = 0; c < Cnum; c++) {
				str += "," + classified_horizontal[p][c];
			}
			str += ",";
			strs.add(str);
		}
		output = "horizontal_" + fileName.split("\\.")[0] + ".csv";
		Output.writeln(output, strs);

	}


	public static BRruleset loadRuleset(String fileName, Problem_BR_Multi mop) {
		/* ********************************************************* */
		MultiDataSetInfo Dtra_multi = mop.getMultiDataSet(0);
		/* ********************************************************* */
		int Ndim = Dtra_multi.getNdim();
		int Cnum = Dtra_multi.getCnum();
		StaticFuzzyFunc.homogeneousInit(Ndim);
		/* ********************************************************* */
		List<String> lines = Input.inputAsListString(fileName);
		/* ********************************************************* */

		SingleRuleSet[] ruleSets = new SingleRuleSet[Cnum];
		for(int c = 0; c < Cnum; c++) {
			lines.remove(0);	// *****
			lines.remove(0);	//label_c

			String[] line = lines.get(0).split(" ");
			int ruleNum = Integer.parseInt(line[1]);
			lines.remove(0);	//ruleNum:

			lines.remove(0);	//---

			//SingleDataSetInfo
			SingleDataSetInfo Dtra = mop.getSingleDataSet(0, c);

			SingleRuleSet ruleSet = new SingleRuleSet();
			SingleRule[] rules = new SingleRule[ruleNum];
			for(int r = 0; r < ruleNum; r++) {
				line = lines.get(0).split(",");
				String[] separate = line[0].split(" +");

				int[] antecedent = new int[Ndim];
				for(int n = 0; n < Ndim; n++) {
					antecedent[n] = Integer.parseInt(separate[1+n]);
				}
				rules[r] = new SingleRule(antecedent);
				ruleSet.addRule(rules[r]);

				lines.remove(0);
			}

			ruleSet.learning(Dtra, Setting.forkJoinPool);
			ruleSet.radixSort();
			ruleSet.calcRuleLength();

			ruleSets[c] = ruleSet;

			lines.remove(0);	//*****
			if(lines.size() > 0) {
				lines.remove(0);	// \n
			}
		}

		BRruleset BRruleset = new BRruleset(ruleSets);

		return BRruleset;
	}
}






























































