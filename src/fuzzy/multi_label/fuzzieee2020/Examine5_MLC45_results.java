package fuzzy.multi_label.fuzzieee2020;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import method.Output;
import method.StaticFunction;

/**
 * MLC4.5の予測出力ラベルから
 * Exact-match error
 * F-measure
 * Hamming Lossを計算する
 *
 * predicted_results/内のデータを扱う
 */

public class Examine5_MLC45_results {
	public static void main(String[] args) {
		String[][] problems = new String[][] {
			new String[] {"CAL500", "174"},
			new String[] {"emotions", "6"},
			new String[] {"scene", "6"},
			new String[] {"yeast_multi", "14"},
			new String[] {"flags", "7"},
			new String[] {"birds", "19"}
		};

		ArrayList<String> strs = new ArrayList<>();
		String str = "dataset,ExactMatchError_Dtst,Fmeasure_Dtst,HammingLoss_Dtst";
		strs.add(str);
		for(int data = 0; data < problems.length; data++) {
			ArrayList<String> strs_data = new ArrayList<>();
			String str_data = "CV,ExactMatchError_Dtst,Fmeasure_Dtst,HammingLoss_Dtst";
			strs_data.add(str_data);
			/* ********************************************************* */
			String dataName = problems[data][0];
			int Cnum = Integer.parseInt(problems[data][1]);
			/* ********************************************************* */
			ArrayList<double[]> all = new ArrayList<>();
			int cv = 10;
			int repeat = 3;
			for(int rr = 0; rr < repeat; rr++) {
				for(int cc = 0; cc < cv; cc++) {
					String now = String.valueOf(rr) + String.valueOf(cc);
					str_data = now;

					double[] oneTrial = oneTrial(dataName, Cnum, rr, cc);
					all.add(oneTrial);

					str_data += "," + oneTrial[0];
					str_data += "," + oneTrial[1];
					str_data += "," + oneTrial[2];
					strs_data.add(str_data);
				}
			}
			String summaryFile = dataName + "_gene1000.csv";
			Output.writeln(summaryFile, strs_data);

			double SubAcc_mean = 0.0;
			double FM_mean = 0.0;
			double HL_mean = 0.0;

			for(int i = 0; i < all.size(); i++) {
				SubAcc_mean += all.get(i)[0];
				FM_mean += all.get(i)[1];
				HL_mean += all.get(i)[2];
			}

			SubAcc_mean = SubAcc_mean / (double)all.size();
			FM_mean = FM_mean / (double)all.size();
			HL_mean = HL_mean / (double)all.size();

			str = "";
			str += dataName;
			str += "," + String.valueOf(SubAcc_mean);
			str += "," + String.valueOf(FM_mean);
			str += "," + String.valueOf(HL_mean);
			strs.add(str);
		}

		String fileName = "summary_MLC45.csv";
		Output.writeln(fileName, strs);



	}

	/**
	 * double[3]
	 * 0:Exact-Match Error, 1:F-measure, 2:HammingLoss
	 */
	public static double[] oneTrial(String dataName, int Cnum, int rr, int cc) {
		/**
		 * double[3]
		 * 0:Exact-Match Error, 1:F-measure, 2:HammingLoss
		 */
		double[] results = new double[3];

		/* ********************************************************* */
		String sep = File.separator;
		String fileName = "workspace" + sep
						+ "predicted_results" + sep
						+ dataName + sep
						+ dataName + "_" + rr + "_" + cc + ".test.pred.arff";
		/* ********************************************************* */
		List<String> lines = null;
		try ( Stream<String> line = Files.lines(Paths.get(fileName)) ) {
			lines = line.collect(Collectors.toList());
		} catch (IOException e) {
		    e.printStackTrace();
		}
		if(lines == null) {
			return null;
		}
		/* ********************************************************* */

		int dataSize = 0;
		double missPatterns = 0.0;
		double recall = 0.0;
		double precision = 0.0;
		double Fmeasure = 0.0;
		double hammingLoss = 0.0;

		for(String line : lines) {
			if( line.matches("^@.*") ||
				line.matches("^$")) {
				continue;
			}
			dataSize++;
			String[] array = line.split(",");

			int[] answer = new int[Cnum];
			int[] predicted = new int[Cnum];

			for(int c = 0; c < Cnum; c++) {
				answer[c] = Integer.parseInt(array[c]);
				predicted[c] = Integer.parseInt(array[c + Cnum]);
			}

			if(!Arrays.equals(predicted, answer)) {
				missPatterns++;
			}
			recall += StaticFunction.RecallMetric(predicted, answer);
			precision += StaticFunction.PrecisionMetric(predicted, answer);

			double distance = StaticFunction.HammingDistance(predicted, answer);
			hammingLoss += distance / (double)Cnum;

		}

		recall = recall / (double)dataSize;
		precision = precision / (double)dataSize;
		if((precision + recall) == 0) {
			Fmeasure = 0;
		}
		else {
			Fmeasure = (2.0 * recall * precision) / (recall + precision);
		}

		results[0] = missPatterns / (double)dataSize * 100.0;
		results[1] = Fmeasure * 100.0;
		results[2] = hammingLoss / (double)dataSize * 100.0;

		return results;
	}
}




































