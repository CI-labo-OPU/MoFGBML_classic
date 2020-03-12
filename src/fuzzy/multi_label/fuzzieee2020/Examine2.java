package fuzzy.multi_label.fuzzieee2020;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

import data.Input;
import data.MultiDataSetInfo;
import data.MultiPattern;
import data.artificial.Richromatic;
import fuzzy.StaticFuzzyFunc;
import fuzzy.fml.params.HomoTriangle_2_3_4_5;
import fuzzy.multi_label.MultiRule;
import fuzzy.multi_label.MultiRuleSet;
import main.Consts;
import main.Setting;
import method.Output;
import method.StaticFunction;

/**
 * 人工データセットに対して，7種類の結論部があるから，
 * 1種類に着目してその結論部を持つルールを削除しても
 * 正しく識別が可能かどうか調べる．
 *
 */
public class Examine2 {
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
		int h = 500;
		int dataSize = 3000;

		double a = 0.2;
		double b = 0.4;
		double r = 0.4;

		String name = "richromatic_a-" + a + "_b-" + b + "_r-" + r + "_dataSize-" + dataSize;
		/* ********************************************************* */

		MultiDataSetInfo[] datasets = makeDataset(name, h, dataSize, a, b, r);
		MultiDataSetInfo Dtra = datasets[0];
		MultiDataSetInfo Dgrid_horizontal = datasets[1];
		MultiDataSetInfo Dgrid_vertical = datasets[2];

		float[][] params = HomoTriangle_2_3_4_5.getParams();
		MultiRuleSet classifier = makeClassifier(Dtra, params);

		int[][] existLabels
		= new int[][] { new int[] {0, 0, 1},
						new int[] {0, 1, 0},
						new int[] {0, 1, 1},
						new int[] {1, 0, 0},
						new int[] {1, 0, 1},
						new int[] {1, 1, 0},
						new int[] {1, 1, 1}};

		MultiDataSetInfo[] removedDatasets = new MultiDataSetInfo[existLabels.length];
		for(int i = 0; i < existLabels.length; i++) {
			removedDatasets[i] = removeDtraByLabel(Dtra, existLabels[i]);
		}

		//存在しない結論部を正しく識別できるか？
		//(= Dtstのみに存在するようなパワーセットのパターンを正しく識別することが可能か？)
		for(int i = 0; i < existLabels.length; i++) {
			int[] label = existLabels[i];
			String labelStr = "";
			System.out.println("---");
			System.out.print("except label: ");
			for(int l = 0; l < label.length; l++) {
				System.out.print(label[l] + ", ");
				labelStr += String.valueOf(label[l]);
			}
			System.out.println();

			classifier.learning(removedDatasets[i], Setting.forkJoinPool);

			//Dtraに存在しないパワーセットを結論部に持つルールが存在するか？
			int count = 0;
			for(int rule_i = 0; rule_i < classifier.getRuleNum(); rule_i++) {
				MultiRule rule = classifier.getMicRule(rule_i);	//Shallow Copy
				if(Arrays.equals(rule.getConc(), label)) {
					count++;
				}
			}
			System.out.println("Exist Consequent: " + count);

			//Appendix
			double[] appendixDtra_CFmean = calcApendix(Dtra, classifier, true);
			double[] appendixDtra_CFvector = calcApendix(Dtra, classifier, false);

			//Get Boundary
			int[][] CFmean_horizontal = new int[Dgrid_horizontal.getDataSize()][];
			int[][] CFmean_vertical = new int[Dgrid_vertical.getDataSize()][];
			int[][] CFvector_horizontal = new int[Dgrid_horizontal.getDataSize()][];
			int[][] CFvector_vertical = new int[Dgrid_vertical.getDataSize()][];
			//horizontal
			for(int p = 0; p < Dgrid_horizontal.getDataSize(); p++) {
				MultiPattern pattern = Dgrid_horizontal.getPattern(p);
				CFmean_horizontal[p] = classifier.cfMeanClassify(pattern, false);
				CFvector_horizontal[p] = classifier.cfVectorClassify(pattern, false);
			}
			//vertical
			for(int p = 0; p < Dgrid_vertical.getDataSize(); p++) {
				MultiPattern pattern = Dgrid_vertical.getPattern(p);
				CFmean_vertical[p] = classifier.cfMeanClassify(pattern, false);
				CFvector_vertical[p] = classifier.cfVectorClassify(pattern, false);
			}

			//Dtraに存在しないパワーセットを出力可能か?
			count = 0;
			for(int p = 0; p < Dgrid_horizontal.getDataSize(); p++) {
				MultiPattern pattern = Dgrid_horizontal.getPattern(p);
				int[] answer = classifier.cfVectorClassify(pattern, false);

				if(Arrays.equals(answer, label)) {
					count++;
				}
			}
			System.out.println("Classified Boundary: " + count);
			System.out.println();


			//Output Classifier
			String outputClassifier = outputRuleSet(classifier, appendixDtra_CFmean, appendixDtra_CFvector);
			String fileName = name + "_classifier.txt";
			Output.writeln(fileName, outputClassifier);

			//Output Boundary
			String sep = File.separator;
			String outDir = "except_" + labelStr;
			Output.mkdirs(outDir);
			ArrayList<String> strs = new ArrayList<>();
			String str = "";
			//CFmean_horizontal
			int patternSize = Dgrid_horizontal.getDataSize();
			//Header
			str = patternSize + "," + Dgrid_horizontal.getNdim() + "," + Dgrid_horizontal.getCnum();
			strs.add(str);
			//data
			for(int p = 0; p < patternSize; p++) {
				str = "";
				//input
				for(int n = 0; n < Dgrid_horizontal.getNdim(); n++) {
					str += Dgrid_horizontal.getPattern(p).getDimValue(n) + ",";
				}
				//classified label
				for(int c = 0; c < Dgrid_horizontal.getCnum(); c++) {
					str += CFmean_horizontal[p][c] + ",";
				}
				strs.add(str);
			}
			fileName = outDir + sep + name + "_boundary_CFmean_horizontal.csv";
			Output.writeln(fileName, strs);

			//CFmean_vertical
			strs = new ArrayList<>();
			str = "";
			patternSize = Dgrid_vertical.getDataSize();
			//Header
			str = patternSize + "," + Dgrid_vertical.getNdim() + "," + Dgrid_vertical.getCnum();
			strs.add(str);
			//data
			for(int p = 0; p < patternSize; p++) {
				str = "";
				//input
				for(int n = 0; n < Dgrid_vertical.getNdim(); n++) {
					str += Dgrid_vertical.getPattern(p).getDimValue(n) + ",";
				}
				//classified label
				for(int c = 0; c < Dgrid_vertical.getCnum(); c++) {
					str += CFmean_vertical[p][c] + ",";
				}
				strs.add(str);
			}
			fileName = outDir + sep + name + "_boundary_CFmean_vertical.csv";
			Output.writeln(fileName, strs);

			//CFvector_horizontal
			strs = new ArrayList<>();
			str = "";
			patternSize = Dgrid_horizontal.getDataSize();
			//Header
			str = patternSize + "," + Dgrid_horizontal.getNdim() + "," + Dgrid_horizontal.getCnum();
			strs.add(str);
			//data
			for(int p = 0; p < patternSize; p++) {
				str = "";
				//input
				for(int n = 0; n < Dgrid_horizontal.getNdim(); n++) {
					str += Dgrid_horizontal.getPattern(p).getDimValue(n) + ",";
				}
				//classified label
				for(int c = 0; c < Dgrid_horizontal.getCnum(); c++) {
					str += CFvector_horizontal[p][c] + ",";
				}
				strs.add(str);
			}
			fileName = outDir + sep + name + "_boundary_CFvector_horizontal.csv";
			Output.writeln(fileName, strs);

			//CFvector_vertical
			strs = new ArrayList<>();
			str = "";
			patternSize = Dgrid_vertical.getDataSize();
			//Header
			str = patternSize + "," + Dgrid_vertical.getNdim() + "," + Dgrid_vertical.getCnum();
			strs.add(str);
			//data
			for(int p = 0; p < patternSize; p++) {
				str = "";
				//input
				for(int n = 0; n < Dgrid_vertical.getNdim(); n++) {
					str += Dgrid_vertical.getPattern(p).getDimValue(n) + ",";
				}
				//classified label
				for(int c = 0; c < Dgrid_vertical.getCnum(); c++) {
					str += CFvector_vertical[p][c] + ",";
				}
				strs.add(str);
			}
			fileName = outDir + sep + name + "_boundary_CFvector_vertical.csv";
			Output.writeln(fileName, strs);



		}

		System.out.println();

	}

	public static MultiDataSetInfo removeDtraByLabel(MultiDataSetInfo Dtra, int[] label) {
		MultiDataSetInfo newD = new MultiDataSetInfo();
		newD.setNdim(Dtra.getNdim());
		newD.setCnum(Dtra.getCnum());
		int dataSize = 0;

		for(int p = 0; p < Dtra.getDataSize(); p++) {
			if(!Arrays.equals(Dtra.getPattern(p).getConClass(), label)) {
				newD.addPattern(Dtra.getPattern(p));
				dataSize++;
			}
		}

		newD.setDataSize(dataSize);
		return newD;
	}

	/**
	 *
	 * @param Dtra
	 * @param cfMeanORcfVector : boolean : true:CFmean, false:CFvector
	 * @return
	 */
	public static MultiRuleSet makeClassifier(MultiDataSetInfo Dtra, float[][] params) {
		int Ndim = Dtra.getNdim();
		int Cnum = Dtra.getCnum();

		MultiRuleSet ruleSet = new MultiRuleSet();
		int[][] antecedent = initAllCombiAntecedent(Ndim, params);
		int ruleNum = antecedent.length;
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			MultiRule rule = new MultiRule(antecedent[rule_i], Cnum);
			ruleSet.addRule(rule);
		}

		ruleSet.learning(Dtra, Setting.forkJoinPool);
		ruleSet.calcRuleLength();

		return ruleSet;
	}

	/**
	 *
	 * @param dataset
	 * @param ruleSet
	 * @param cfMeanORcfVector : boolean : true:CFmean, false:CFvector
	 * @return double[] : 0:Exact-Match-Error, 1:F-measure, 2:Hamming Loss
	 */
	public static double[] calcApendix(MultiDataSetInfo dataset, MultiRuleSet ruleSet, boolean cfMeanORcfVector) {
		double errorRate = 0;
		double exactMatchNum = 0;
		double HammingLoss = 0.0;
		double Fmeasure = 0.0;
		int ruleNum = 0;
		int ruleLength = 0;

		for(int p = 0; p < dataset.getDataSize(); p++) {
			int[] answerClass = null;
			if(cfMeanORcfVector) {
				answerClass = ruleSet.cfMeanClassify(dataset.getPattern(p), true);
			}
			else {
				answerClass = ruleSet.cfVectorClassify(dataset.getPattern(p), true);
			}

			//Exact-Match
			if(Arrays.equals(answerClass, dataset.getPattern(p).getConClass())) {
				exactMatchNum++;
			}

			//F-measure
			double precision = StaticFunction.PrecisionMetric(answerClass, dataset.getPattern(p).getConClasses());
			double recall = StaticFunction.RecallMetric(answerClass, dataset.getPattern(p).getConClasses());
			double f;
			if((precision + recall) == 0) {
				f = 0;
			}
			else {
				f = (2.0 * recall * precision) / (recall + precision);
			}
			Fmeasure += f;

			//Hamming Loss
			double distance = StaticFunction.HammingDistance(answerClass, dataset.getPattern(p).getConClass());
			HammingLoss += distance / (double)dataset.getCnum();
		}

		//range [0, 1]
		errorRate = ((double)dataset.getDataSize() - exactMatchNum) / (double)dataset.getDataSize();
		Fmeasure = Fmeasure / (double)dataset.getDataSize();
		HammingLoss = HammingLoss / (double)dataset.getDataSize();

		//ruleNum
		for(int r = 0; r < ruleSet.getRuleNum(); r++) {
			if(ruleSet.getMicRule(r).getFitness() > 0) {
				ruleNum++;
				ruleLength += ruleSet.getMicRule(r).getRuleLength();
			}
		}

		double[] appendix = new double[] {errorRate * 100.0, Fmeasure * 100.0, HammingLoss * 100.0, ruleNum, ruleLength};

		return appendix;
	}

	/**
	 *
	 * @param args
	 * @return : 0:Dtra, 1:grid_horizontal, 2:grid_vertical
	 */
	public static MultiDataSetInfo[] makeDataset(String name, int h, int dataSize, double a, double b, double r) {
		Richromatic dataset = new Richromatic(name, a, b, r, h, dataSize);

		dataset.makeRichromatic(false);
		dataset.makeGrid();

		String DtraFile = name + ".dat";
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, DtraFile);

		//Except Zeros Label
		String gridHorizontalFile = name + "_horizontalGrid.csv";
		MultiDataSetInfo Dgrid_horizontal = new MultiDataSetInfo();
		Input.inputMultiLabel(Dgrid_horizontal, gridHorizontalFile);

		String gridVerticalFile = name + "_verticalGrid.csv";
		MultiDataSetInfo Dgrid_vertical = new MultiDataSetInfo();
		Input.inputMultiLabel(Dgrid_vertical, gridVerticalFile);

		MultiDataSetInfo[] datasets = new MultiDataSetInfo[] {Dtra, Dgrid_horizontal, Dgrid_vertical};

		return datasets;
	}

	//Don't care なし
	public static int[][] initAllCombiAntecedent(int Ndim, float[][] params) {

//		StaticFuzzyFunc.paramInit(Ndim, params);	//Don't Care なし
		StaticFuzzyFunc.homogeneousInit(Ndim);		//Don't Care あり

//		int Fdiv = params.length;	//Don't careなし
		int Fdiv = params.length + 1;	//Don't careあり
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
//						antecedent[rule_i][i] = count+1;	//Don't Care なし
						antecedent[rule_i][i] = count;	//Don't Care あり
						rule_i++;
					}
					count++;
				}
			}
		}

		return antecedent;
	}

	public static String outputRuleSet(MultiRuleSet ruleSet, double[] appendix_CFmean, double[] appendix_CFvector) {
		String ln = System.lineSeparator();
		String row = "***************************************";
		String hyphen = "---";

		String strs = "";
		String str = "";

		int Ndim = ruleSet.getMicRule(0).getRule().length;
		int Cnum = ruleSet.getMicRule(0).getConc().length;
		int ruleNum = ruleSet.getRuleNum();

		strs += row + ln;
		strs += "[CFmean]" + ln;
		strs += "Error Rate for Dtra[%]: " + appendix_CFmean[0] + ln;
		strs += "F-measure for Dtra[%]: " + appendix_CFmean[1] + ln;
		strs += "Hamming Loss for Dtra[%]: " + appendix_CFmean[2] + ln;
		strs += "ruleNum: " + appendix_CFmean[3] + ln;
		strs += "ruleLength: " + appendix_CFmean[4] + ln;
		strs += hyphen + ln;

		strs += row + ln;
		strs += "[CFvector]" + ln;
		strs += "Error Rate for Dtra[%]: " + appendix_CFvector[0] + ln;
		strs += "F-measure for Dtra[%]: " + appendix_CFvector[1] + ln;
		strs += "Hamming Loss for Dtra[%]: " + appendix_CFvector[2] + ln;
		strs += "ruleNum: " + appendix_CFvector[3] + ln;
		strs += "ruleLength: " + appendix_CFvector[4] + ln;
		strs += hyphen + ln;

		//Rules
		for(int rule = 0; rule < ruleNum; rule++) {
			//id
			str = "Rule_" + String.format("%02d", rule) + ":";
			//rule
			for(int n = 0; n < Ndim; n++) {
				str += " " + String.format("%2d", ruleSet.getMicRule(rule).getRule(n));
			}
			//class
			str += ", " + "Class:";
			for(int l = 0; l < Cnum; l++) {
				str += " " + ruleSet.getMicRule(rule).getConc(l);
			}
			//cf Mean
			str += ", " + "CF_mean: " + ruleSet.getMicRule(rule).getCf();
			//CF Vector
			str += ", " + "CF_vector:";
			for(int l = 0; l < Cnum; l++) {
				str += " " + ruleSet.getMicRule(rule).getCFVector(l);
			}
			//fitness
			str += ", " + "Fitness: " + ruleSet.getMicRule(rule).getFitness();

			strs += str + ln;
		}
		strs += row + ln;
		strs += "" + ln;
		return strs;
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
