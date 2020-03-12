package fuzzy.multi_label.fuzzieee2020;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import data.Input;
import data.MultiDataSetInfo;
import fgbml.multilabel.MOP_ExactMatchError;
import fgbml.multilabel.Problem_MultiLabel;
import fuzzy.StaticFuzzyFunc;
import fuzzy.multi_label.MultiRule;
import fuzzy.multi_label.MultiRuleSet;
import main.Consts;
import main.Setting;
import method.Output;

/**
 * CFmean, CFvectorのruleSet.txtを再読込後，richromaticの識別境界を出力
 *
 */

public class Examine4 {
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
		String trainName = "a1_2_richromatic-wide-10tra.dat";
		String testName = "a1_2_richromatic-wide-10tst.dat";
		String meanName = "ruleSet_CFmean_richromatic-wide_trial13.txt";
		String vectorName = "ruleSet_CFvector_richromatic-wide_trial12.txt";
		/* ********************************************************* */
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		MultiDataSetInfo Dtst = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, trainName);
		Input.inputMultiLabel(Dtst, testName);
		Problem_MultiLabel mop = new MOP_ExactMatchError(Dtra, Dtst);
		/* ********************************************************* */

		MultiRuleSet ruleSetMean = loadRuleSet(meanName, Dtra);
		MultiRuleSet ruleSetVector = loadRuleSet(vectorName, Dtra);

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
		int[][] mean_vertical = new int[vertical.getDataSize()][Cnum];
		int[][] mean_horizontal = new int[horizontal.getDataSize()][Cnum];
		int[][] vector_vertical = new int[vertical.getDataSize()][Cnum];
		int[][] vector_horizontal = new int[horizontal.getDataSize()][Cnum];

		for(int p = 0; p < vertical.getDataSize(); p++) {
			mean_vertical[p] = ruleSetMean.cfMeanClassify(vertical.getPattern(p), false);
			vector_vertical[p] = ruleSetVector.cfVectorClassify(vertical.getPattern(p), false);
		}
		for(int p = 0; p < horizontal.getDataSize(); p++) {
			mean_horizontal[p] = ruleSetMean.cfMeanClassify(horizontal.getPattern(p), false);
			vector_horizontal[p] = ruleSetVector.cfVectorClassify(horizontal.getPattern(p), false);
		}

		/* ********************************************************* */
		//Output
		ArrayList<String> strs = new ArrayList<>();
		String str = "";

		//CFmean
		//vertical
		strs.clear();
		str = "";
		str += vertical.getDataSize() + "," + vertical.getNdim() + "," + vertical.getCnum();
		strs.add(str);
		for(int p = 0; p < vertical.getDataSize(); p++) {
			str = "";
			str += vertical.getPattern(p).getDimValue(0);
			str += "," + vertical.getPattern(p).getDimValue(1);
			for(int c = 0; c < Cnum; c++) {
				str += "," + mean_vertical[p][c];
			}
			str += ",";
			strs.add(str);
		}
		String output = "richromatic-wide_boundary_CFmean_vertical.csv";
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
				str += "," + mean_horizontal[p][c];
			}
			str += ",";
			strs.add(str);
		}
		output = "richromatic-wide_boundary_CFmean_horizontal.csv";
		Output.writeln(output, strs);

		//CFvector
		//vertical
		strs.clear();
		str = "";
		str += vertical.getDataSize() + "," + vertical.getNdim() + "," + vertical.getCnum();
		strs.add(str);
		for(int p = 0; p < vertical.getDataSize(); p++) {
			str = "";
			str += vertical.getPattern(p).getDimValue(0);
			str += "," + vertical.getPattern(p).getDimValue(1);
			for(int c = 0; c < Cnum; c++) {
				str += "," + vector_vertical[p][c];
			}
			str += ",";
			strs.add(str);
		}
		output = "richromatic-wide_boundary_CFvector_vertical.csv";
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
				str += "," + vector_horizontal[p][c];
			}
			str += ",";
			strs.add(str);
		}
		output = "richromatic-wide_boundary_CFvector_horizontal.csv";
		Output.writeln(output, strs);


		System.out.println();
	}

	public static MultiRuleSet loadRuleSet(String fileName, MultiDataSetInfo Dtra) {
		/* ********************************************************* */
		int Ndim = Dtra.getNdim();
		int Cnum = Dtra.getCnum();
		StaticFuzzyFunc.homogeneousInit(Ndim);
		/* ********************************************************* */
		List<String> lines = Input.inputAsListString(fileName);
		lines.remove(0);	//******
		lines.remove(0);	//pop_

		String[] line = lines.get(0).split(" ");
		int ruleNum = Integer.parseInt(line[1]);
		lines.remove(0);	//ruleNum:
		lines.remove(0);	//---
		/* ********************************************************* */

		MultiRuleSet ruleSet = new MultiRuleSet();
		for(int r = 0; r < ruleNum; r++) {
			line = lines.get(0).split(",");
			String[] separate = line[0].split(" +");

			int[] antecedent = new int[Ndim];
			for(int n = 0; n < Ndim; n++) {
				antecedent[n] = Integer.parseInt(separate[1+n]);
			}

			MultiRule rule = new MultiRule(antecedent, Cnum);
			ruleSet.addRule(rule);

			lines.remove(0);
		}
		ruleSet.learning(Dtra, Setting.forkJoinPool);
		ruleSet.radixSort();
		ruleSet.calcRuleLength();

		return ruleSet;
	}
}




























