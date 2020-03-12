package fuzzy.multi_label.fuzzieee2020;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;

import data.Input;
import data.MultiDataSetInfo;
import data.artificial.Richromatic;
import fuzzy.StaticFuzzyFunc;
import fuzzy.fml.params.HomoTriangle_2_3_4_5;
import fuzzy.multi_label.MultiRule;
import fuzzy.multi_label.MultiRuleSet;
import main.Consts;
import main.Setting;
import method.Output;

/**
 * 人工データセットについて，全組合せルール集合による識別境界を出力
 *
 */
public class Examine6 {
	public static void setting() {
		String[] args = new String[] {"./", "consts", "setting", "5"};
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
		/* ********************************************************* */
		setting();
		int Ndim = 2;
		int Cnum = 3;
		StaticFuzzyFunc.homogeneousInit(Ndim);
		/* ********************************************************* */
		//Artificial Dataset Settings
		double a = 0.2;
		double b = 0.28;
		double r = 0.4;
		int dataSize = 30000;
		int h = 500;

		String sep = File.separator;
		String dirName = "workspace" + sep + "richromatic_a-" + a + "_b-" + b + "_r-" + r + "_dataSize-" + dataSize;
		Output.mkdirs(dirName);

		String name = dirName + sep + "richromatic_a-" + a + "_b-" + b + "_r-" + r + "_dataSize-" + dataSize;
		Richromatic richromatic = new Richromatic(name, a, b, r, h, dataSize);
		richromatic.makeRichromatic(false);
		String DtraFile = name + ".dat";
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, DtraFile);
		/* ********************************************************* */
		//Make Classifier
		MultiRuleSet ruleSet_CFmean = new MultiRuleSet();
		MultiRuleSet ruleSet_CFvector = new MultiRuleSet();
		int[][] antecedent = initAllCombiAntecedent(Ndim);
		int ruleNum = antecedent.length;
		for(int i = 0; i < ruleNum; i++) {
			MultiRule rule = new MultiRule(antecedent[i], Cnum);
			ruleSet_CFmean.addRule(rule);
			ruleSet_CFvector.addRule(rule);
		}
		ruleSet_CFmean.learning(Dtra, Setting.forkJoinPool);
		ruleSet_CFmean.calcRuleLength();
		ruleSet_CFvector.learning(Dtra, Setting.forkJoinPool);
		ruleSet_CFvector.calcRuleLength();
		/* ********************************************************* */
		//Calculate Rule Fitness
		for(int p = 0; p < Dtra.getDataSize(); p++) {
			ruleSet_CFmean.cfMeanClassify(Dtra.getPattern(p), true);
			ruleSet_CFvector.cfVectorClassify(Dtra.getPattern(p), true);
		}
		//Output All Combination of Rules
		String cfMeanFile = name + "_allRules_CFmean.csv";
		String cfVectorFile = name + "_allRules_CFvector.csv";
		String output_CFmean = outputRuleSetCSV(ruleSet_CFmean);
		String output_CFvector = outputRuleSetCSV(ruleSet_CFvector);
		Output.writeln(cfMeanFile, output_CFmean);
		Output.writeln(cfVectorFile, output_CFvector);
		//Delition Rule
		//CFmean
		int head = 0;
		while(ruleSet_CFmean.getMicRules().size() > head) {
			// (CF <= 0) or (Fitness == 0)
			if( ruleSet_CFmean.getMicRules().get(head).getCf() <= 0 ||
				ruleSet_CFmean.getMicRules().get(head).getFitness() <= 0) {

				ruleSet_CFmean.getMicRules().remove(head);
			}
			else {
				head++;
			}
		}
		ruleSet_CFmean.calcRuleLength();
		//CFvector
		head = 0;
		while(ruleSet_CFvector.getMicRules().size() > head) {
			// (CF <= 0) or (Fitness == 0)
			if( ruleSet_CFvector.getMicRules().get(head).getCf() <= 0 ||
				ruleSet_CFvector.getMicRules().get(head).getFitness() <= 0) {

				ruleSet_CFvector.getMicRules().remove(head);
			}
			else {
				head++;
			}
		}
		ruleSet_CFvector.calcRuleLength();
		/* ********************************************************* */
		//Load Grid File
		String verticalFile = "richromatic_verticalGrid.csv";
		String horizontalFile = "richromatic_horizontalGrid.csv";
		MultiDataSetInfo vertical = new MultiDataSetInfo();
		MultiDataSetInfo horizontal = new MultiDataSetInfo();
		Input.inputMultiLabel(vertical, verticalFile);
		Input.inputMultiLabel(horizontal, horizontalFile);
		/* ********************************************************* */
		//Make Boundaries
		int[][] mean_vertical = new int[vertical.getDataSize()][Cnum];
		int[][] mean_horizontal = new int[horizontal.getDataSize()][Cnum];
		int[][] vector_vertical = new int[vertical.getDataSize()][Cnum];
		int[][] vector_horizontal = new int[horizontal.getDataSize()][Cnum];
		for(int p = 0; p < vertical.getDataSize(); p++) {
			mean_vertical[p] = ruleSet_CFmean.cfMeanClassify(vertical.getPattern(p), false);
			vector_vertical[p] = ruleSet_CFvector.cfVectorClassify(vertical.getPattern(p), false);
		}
		for(int p = 0; p < horizontal.getDataSize(); p++) {
			mean_horizontal[p] = ruleSet_CFmean.cfMeanClassify(horizontal.getPattern(p), false);
			vector_horizontal[p] = ruleSet_CFvector.cfVectorClassify(horizontal.getPattern(p), false);
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
		String output = name + "_CFmean_vertical.csv";
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
		output = name + "_CFmean_horizontal.csv";
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
		output = name + "_CFvector_vertical.csv";
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
		output = name + "_CFvector_horizontal.csv";
		Output.writeln(output, strs);

	}

	//Don't care あり
	public static int[][] initAllCombiAntecedent(int Ndim) {
		float[][] params = HomoTriangle_2_3_4_5.getParams();

		StaticFuzzyFunc.paramInit(Ndim, params);	//Don't Care なし
//		StaticFuzzyFunc.homogeneousInit(Ndim);		//Don't Care あり

		int Fdiv = params.length;	//Don't careなし
//		int Fdiv = StaticFuzzyFunc.kb.getFSs()[0].length;	//Don't careあり
		int ruleNum = (int)Math.pow(Fdiv, Ndim);

		//Antecedent Part
		int[][] antecedent = new int[ruleNum][Ndim];
		for(int i = 0; i < Ndim; i++) {
			int rule_i = 0;
			int repeatNum = 1;
			int interval = 1;
			int count = 0;
			for(int j = 0; j < i; j++) {
				repeatNum *= Fdiv;
			}
			for(int j = i+1; j < Ndim; j++) {
				interval *= Fdiv;
			}
			for(int j = 0; j < repeatNum; j++) {
				count = 0;
				for(int k = 0; k < Fdiv; k++) {
					for(int l = 0; l < interval; l++) {
						antecedent[rule_i][i] = count+1;	//Don't Care なし
//						antecedent[rule_i][i] = count;	//Don't Care あり
						rule_i++;
					}
					count++;
				}
			}
		}

		return antecedent;
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

}




















