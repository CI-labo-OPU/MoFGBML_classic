package data;

import java.util.ArrayList;
import java.util.List;

import method.MersenneTwisterFast;
import method.Output;

public class CrossValidation {

	public static void main(String[] args) {
		int seed = Integer.parseInt(args[0]);
		String fileName = args[1];
		String dataName = args[2];
		int repeat = Integer.parseInt(args[3]);
		int cv = Integer.parseInt(args[4]);

		cilabo_CrossValidation(seed, fileName, dataName, repeat, cv);
	}

	/**
	 * CILABフォーマットのデータセットをcross-validation用に分割する
	 * @param seed
	 * @param fileName
	 * @param dataName
	 * @param repeat
	 * @param cv
	 */
	public static void cilabo_CrossValidation(int seed, String fileName, String dataName, int repeat, int cv) {
		MersenneTwisterFast rnd = new MersenneTwisterFast(seed);

		List<double[]> lines = Input.inputAsList(fileName);

		int dataSize = (int)lines.get(0)[0];
		int Ndim = (int)lines.get(0)[1];
		int Cnum = (int)lines.get(0)[2];
		lines.remove(0);

		for(int rr = 0; rr < repeat; rr++) {
			lines = shuffle(lines, rnd);
			ArrayList<ArrayList<double[]>> divided = divideCV(lines, cv);

			for(int cc = 0; cc < cv; cc++) {
				ArrayList<double[]> train = new ArrayList<>();
				ArrayList<double[]> test = new ArrayList<>();

				test.addAll(divided.get(cc));
				for(int ccc = 0; ccc < cv; ccc++) {
					if(ccc != cc) {
						train.addAll(divided.get(ccc));
					}
				}

				String traFileName = "a" + rr + "_" + cc + "_" + dataName + "-" + cv + "tra.dat";
				String tstFileName = "a" + rr + "_" + cc + "_" + dataName + "-" + cv + "tst.dat";

				ArrayList<String> trainStrings = translateStringList(train, ",");
				ArrayList<String> testStrings = translateStringList(test, ",");
				String header;
				header = trainStrings.size() + "," + Ndim + "," + Cnum;
				trainStrings.add(0, header);
				header = testStrings.size() + "," + Ndim + "," + Cnum;
				testStrings.add(0, header);

				Output.writeln(traFileName, trainStrings);
				Output.writeln(tstFileName, testStrings);
			}
		}

	}


	/**
	 * <h1>Format for Output</h1>
	 * @param lines
	 * @param spliter
	 * @return
	 */
	public static ArrayList<String> translateStringList(List<double[]> lines, String spliter) {
		ArrayList<String> strs = new ArrayList<>();
		for(int i = 0; i < lines.size(); i++) {
			int dim = lines.get(i).length;
			String str = String.valueOf(lines.get(i)[0]);
			for(int j = 1; j < dim; j++) {
				str += spliter + String.valueOf(lines.get(i)[j]);
			}
			strs.add(str);
		}
		return strs;
	}

	/**
	 * <h1>Divide dataset into cv sub-datasets.</h1>
	 * @param lines
	 * @param cv
	 * @return
	 */
	public static ArrayList<ArrayList<double[]>> divideCV(List<double[]> lines, int cv) {
		ArrayList<ArrayList<double[]>> divided = new ArrayList<>();
		for(int h = 0; h < cv; h++) {
			divided.add(new ArrayList<>());
		}
		for(int p = 0; p < lines.size(); p++) {
			double[] line = lines.get(p);
			divided.get(p % cv).add(line);
		}
		return divided;
	}

	/**
	 * <h1>Shuffle the order of original dataset</h1>
	 * @param lines
	 * @param rnd
	 * @return
	 */
	public static ArrayList<double[]> shuffle(List<double[]> lines, MersenneTwisterFast rnd) {
		ArrayList<Integer> box = new ArrayList<>();
		for(int i = 0; i < lines.size(); i++) {
			box.add(i);
		}

		ArrayList<double[]> shuffled = new ArrayList<>();
		while(box.size() > 0) {
			int index = rnd.nextInt(box.size());
			shuffled.add(lines.get(box.get(index)));
			box.remove(index);
		}

		return shuffled;
	}
}
