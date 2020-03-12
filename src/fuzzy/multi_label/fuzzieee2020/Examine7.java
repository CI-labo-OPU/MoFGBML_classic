package fuzzy.multi_label.fuzzieee2020;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import data.Input;
import main.Consts;
import main.Setting;
import method.Output;
import method.StaticFunction;

/**
 * MLRBCの結果をとる
 */

public class Examine7 {
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
		int Ndim = 68;
		int Cnum = 174;
		/* ********************************************************* */
		String sep = File.separator;
		String dataName = "CAL500";
		String dirName = "workspace" + sep + "MLRBC_last" + sep +dataName;

		int cv = 10;
		int repeat = 3;
		/* ********************************************************* */

		ArrayList<String> strs = new ArrayList<>();
		String str = "CV,ExactMatchError_Dtst,Fmeasure_Dtst,HammingLoss_Dtst,Precision_Dtst,Recall_Dtst";
		strs.add(str);
		for(int rr = 0; rr < repeat; rr++) {
			for(int cc = 0; cc < cv; cc++) {
				String now = String.format("%02d", rr*cv + cc);
				String fileName = dirName + sep + dataName + "_" + now + "_MLRBC_Prediction_Compare_0.txt";
				double[] results = oneTrial(fileName);

				str = String.valueOf(now);
				str += "," + results[0];
				str += "," + results[1];
				str += "," + results[2];
				str += "," + results[3];
				str += "," + results[4];
				strs.add(str);
			}
		}
		String outputFile = dataName + "_gene1000.csv";
		Output.writeln(outputFile, strs);
	}

	/**
	 *
	 * @param fileName
	 * @return double[] : {errorRate, Fmeasure, loss, precision, recall}
	 */
	public static double[] oneTrial(String fileName) {
		List<String> lines = Input.inputAsListString(fileName);
		lines.remove(0);	//header

		int dataSize = lines.size();
		int[][] classifieds = new int[dataSize][];
		int[][] answers = new int[dataSize][];

		for(int i = 0; i < dataSize; i++) {
			String[] data = lines.get(i).split("\t");
			int Cnum = data[0].length();
//			Cnum = Cnum-1;	//birdsのみ
			int[] classified = new int[Cnum];
			int[] answer = new int[Cnum];
			for(int c = 0; c < Cnum; c++) {
				classified[c] = Integer.parseInt(String.valueOf(data[0].charAt(c)));
				answer[c] = Integer.parseInt(String.valueOf(data[1].charAt(c)));
			}
			classifieds[i] = classified;
			answers[i] = answer;
		}

		/* ********************************************************* */
		//ExactMatchError
		double exactMatchNum = 0;
		double errorRate = 0.0;
		for(int p = 0; p < dataSize; p++) {
			if(Arrays.equals(classifieds[p], answers[p])) {
				exactMatchNum++;
			}
		}
		errorRate = 100 * (((double)dataSize - exactMatchNum) / (double)dataSize);
		/* ********************************************************* */
		//F-measure
		double recall = 0.0;
		double precision = 0.0;
		double Fmeasure = 0.0;
		for(int p = 0; p < dataSize; p++) {
			precision += StaticFunction.PrecisionMetric(classifieds[p], answers[p]);
			recall += StaticFunction.RecallMetric(classifieds[p], answers[p]);
		}
		recall = 100.0 * (recall / (double)dataSize);
		precision = 100.0 * (precision / (double)dataSize);
		if((precision + recall) == 0) {
			Fmeasure = 0;
		}
		else {
			Fmeasure = (2.0 * recall * precision) / (recall + precision);
		}
		/* ********************************************************* */
		//Hamming Loss
		double loss = 0.0;
		int Cnum = classifieds[0].length;
		for(int p = 0; p < dataSize; p++) {
			double distance = StaticFunction.HammingDistance(classifieds[p], answers[p]);
			loss += distance / (double)Cnum;
		}
		loss = 100.0 * (loss/(double)dataSize);
		/* ********************************************************* */

		double[] results = {errorRate, Fmeasure, loss, precision, recall};
		return results;
	}
}































