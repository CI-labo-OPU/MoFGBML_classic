package data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import method.Output;

public class MLRBCformat_birds {
	public static void main(String[] args) {
		/* ********************************************************* */
		String dataName = args[0];
		int cv = Integer.parseInt(args[1]);
		int repeat = Integer.parseInt(args[2]);
		/* ********************************************************* */
		String infoFile = dataName + "_info.txt";

		for(int rr = 0; rr < repeat; rr++) {
			for(int cc = 0; cc < cv; cc++) {
				transformOneTrial(dataName, cc, rr, infoFile);
			}
		}
	}

	public static void transformOneTrial(String dataName, int cc, int rr, String infoFile) {
		/* ********************************************************* */
		String traFile = "a" + rr + "_" + cc + "_" + dataName + "-10tra.dat";
		String tstFile = "a" + rr + "_" + cc + "_" + dataName + "-10tst.dat";
		/* ********************************************************* */
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		MultiDataSetInfo Dtst = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, traFile);
		Input.inputMultiLabel(Dtst, tstFile);
		/* ********************************************************* */
		int Ndim = 260;
		int Cnum = 19;
		String zero = "";
		for(int c = 0; c < Cnum; c++) {
			zero += "0";
		}
		/* ********************************************************* */
		//Info File
		List<String> infos = null;
		try ( Stream<String> info = Files.lines(Paths.get(infoFile)) ) {
			infos = info.collect(Collectors.toList());
		} catch (IOException e) {
		    e.printStackTrace();
		}
		if(infos == null) {
			return;
		}
		double[] max = new double[Ndim];
		double[] min = new double[Ndim];
		boolean[] isCategoric = new boolean[Ndim];
		/* ********************************************************* */
		//Load Info.
		infos.remove(0);	//[Attribute]
		for(int i = 0; i < Ndim; i++) {
			String[] data = infos.get(0).split(",");
			if(data[1].equals("categoric")) {
				isCategoric[i] = true;
			}
			else {
				isCategoric[i] = false;
				min[i] = Double.parseDouble(data[2]);
				max[i] = Double.parseDouble(data[3]);

			}
			infos.remove(0);
		}
		infos.remove(0);
		/* ********************************************************* */

		ArrayList<String> strs = new ArrayList<>();
		String str;
		String tab = "\t";
		String outputFile;
		/* ********************************************************* */
		//Train
		strs = new ArrayList<>();
		str = "";
		//header
		for(int n = 0; n < Ndim; n++) {
			str += "Att" + String.valueOf(n+1) + tab;
		}
		str += "Class";
		strs.add(str);
		//patterns
		for(int p = 0; p < Dtra.getDataSize(); p++) {
			MultiPattern pattern = Dtra.getPattern(p);
			str = "";
			//Input values
			for(int n = 0; n < Ndim; n++) {
				double original = 0;
				if(isCategoric[n]) {
					original = (int)((-1) * pattern.getDimValue(n));
				}
				else {
					double normalized = pattern.getDimValue(n);
					original = (max[n] - min[n]) * normalized + min[n];
					if(max[n] < original) {
						original = max[n];
					}
					if(min[n] > original) {
						original = min[n];
					}
				}
				str += String.valueOf(original) + tab;
			}
			//Output Class-labels
			String label = "";
			for(int c = 0; c < Cnum; c++) {
				label += String.valueOf((int)pattern.getConClass(c));
			}
/* ********************************************************* */
			if(label.equals(zero)) {
				label += "1";
			}
			else {
				label += "0";
			}
/* ********************************************************* */
			str += label;
			strs.add(str);
		}
		outputFile = "b" + rr + "_" + cc + "_" + dataName + "-10tra.dat";
		Output.writeln(outputFile, strs);

		/* ********************************************************* */
		//Test
		strs = new ArrayList<>();
		str = "";
		//header
		for(int n = 0; n < Ndim; n++) {
			str += "Att" + String.valueOf(n+1) + tab;
		}
		str += "Class";
		strs.add(str);
		//patterns
		for(int p = 0; p < Dtst.getDataSize(); p++) {
			MultiPattern pattern = Dtst.getPattern(p);
			str = "";
			//Input values
			for(int n = 0; n < Ndim; n++) {
				double original = 0;
				if(isCategoric[n]) {
					original = (int)((-1) * pattern.getDimValue(n));
				}
				else {
					double normalized = pattern.getDimValue(n);
					original = (max[n] - min[n]) * normalized + min[n];
					if(max[n] < original) {
						original = max[n];
					}
					if(min[n] > original) {
						original = min[n];
					}
				}
				str += String.valueOf(original) + tab;
			}
			//Output Class-labels
			String label = "";
			for(int c = 0; c < Cnum; c++) {
				label += String.valueOf((int)pattern.getConClass(c));
			}
/* ********************************************************* */
			if(label.equals(zero)) {
				label += "1";
			}
			else {
				label += "0";
			}
/* ********************************************************* */
			str += label;
			strs.add(str);
		}
		outputFile = "b" + rr + "_" + cc + "_" + dataName + "-10tst.dat";
		Output.writeln(outputFile, strs);

	}
}











































