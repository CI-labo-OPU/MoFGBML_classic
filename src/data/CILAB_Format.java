package data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import method.Output;

public class CILAB_Format {
	// ************************************************************


	// ************************************************************


	// ************************************************************

	//cilabo to arff (Multi-Label)
	public static void main(String[] args) {
		String dataName = args[0];
		int cv = Integer.parseInt(args[1]);
		int repeat = Integer.parseInt(args[2]);

		String infoFile = dataName + "_info.txt";
		String headerFile = dataName + "_header.txt";

		for(int rr = 0; rr < repeat; rr++) {
			for(int cc = 0; cc < cv; cc++) {
				//tra
				String outputName = "a" + rr + "_" + cc + "_" + dataName + "-10tra";
				String fileName = outputName + ".dat";
				MultiDataSetInfo dataset = new MultiDataSetInfo();
				Input.inputMultiLabel(dataset, fileName);
				cilabo2arff(dataset, infoFile, headerFile, outputName, true);

				//tst
				outputName = "a" + rr + "_" + cc + "_" + dataName + "-10tst";
				fileName = outputName + ".dat";
				dataset = new MultiDataSetInfo();
				Input.inputMultiLabel(dataset, fileName);
				cilabo2arff(dataset, infoFile, headerFile, outputName, true);
			}
		}



	}


	@SuppressWarnings("rawtypes")
	public static void cilabo2arff(DataSetInfo dataset, String infoFile, String headerFile, String dataName, boolean isMulti) {
		//CILABO format
		ArrayList<String> lines = new ArrayList<>();
		int dataSize = dataset.getDataSize();
		int Ndim = dataset.getNdim();
		int Cnum = dataset.getCnum();
		lines.add(dataSize + "," + Ndim + "," + Cnum);
		for(int p = 0; p < dataSize; p++) {
			Pattern pattern = (Pattern) dataset.getPattern(p);
			String line = String.valueOf(pattern.getX()[0]);
			for(int i = 1; i < pattern.getX().length; i++) {
				line += "," + pattern.getX()[i];
			}
			lines.add(line);
		}

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

		//Header File
		List<String> header = null;
		try ( Stream<String> line = Files.lines(Paths.get(headerFile)) ) {
			header = line.collect(Collectors.toList());
		} catch (IOException e) {
		    e.printStackTrace();
		}
		if(header == null) {
			return;
		}

		/* ********************************************************* */
		String[] data = lines.get(0).split(",");
		dataSize = Integer.parseInt(data[0]);
		Ndim = Integer.parseInt(data[1]);
		Cnum = Integer.parseInt(data[2]);
		lines.remove(0);
		/* ********************************************************* */

		boolean[] isCategoric = new boolean[Ndim];
		String[] attributeName = new String[Ndim];
		String[][] categoricCILAB = new String[Ndim][];
		String[][] categoricARFF = new String[Ndim][];
		String[] classesCILAB = new String[Cnum];
		String[] classesARFF = new String[Cnum];

		double[] max = new double[Ndim];
		double[] min = new double[Ndim];

		/* ********************************************************* */
		//Load Info.

		//Attribute
		infos.remove(0);	//[Attribute]
		for(int i = 0; i < Ndim; i++) {
			data = infos.get(0).split(",");
			attributeName[i] = data[0];

			if(data[1].equals("categoric")) {
				isCategoric[i] = true;
				String[] inputsCILAB = new String[data.length - 2];
				String[] inputsARFF = new String[data.length - 2];

				for(int j = 0; j < inputsCILAB.length; j++) {
					inputsARFF[j] = data[2+j].split("->")[0];
					inputsCILAB[j] = data[2+j].split("->")[1];
				}

				categoricARFF[i] = inputsARFF;
				categoricCILAB[i] = inputsCILAB;
			}
			else {
				isCategoric[i] = false;
				min[i] = Double.parseDouble(data[2]);
				max[i] = Double.parseDouble(data[3]);

			}
			infos.remove(0);
		}
		infos.remove(0);

		//Classes
		infos.remove(0);	//[Classes]
		for(int c = 0; c < Cnum; c++) {
			data = infos.get(0).split("->");
			classesARFF[c] = data[0];
			classesCILAB[c] = data[1];
			infos.remove(0);
		}

		/* ********************************************************* */
		/* ********************************************************* */
		//Output Start
		ArrayList<String> strs = new ArrayList<>();
		String str = "";

		//Add Header
		for(int i = 0; i < header.size(); i++) {
			strs.add(header.get(i));
		}

		//DataSet
		for(int p = 0; p < dataSize; p++) {
			str = "";
			data = lines.get(p).split(",");

			//Attribute
			for(int n = 0; n < Ndim; n++) {
				if(isCategoric[n]) {
					int cast = (int)Double.parseDouble(data[n]);
					data[n] = String.valueOf(cast);
					for(int i = 0; i < categoricCILAB[n].length; i++) {
						if(data[n].equals(categoricCILAB[n][i])) {
							str += categoricARFF[n][i] + ",";
							break;
						}
					}
				}
				else {
					double normalized = Double.parseDouble(data[n]);
					double origin = (max[n] - min[n]) * normalized + min[n];
					if(max[n] < origin) {
						origin = max[n];
					}
					if(min[n] > origin) {
						origin = min[n];
					}
					str += String.valueOf(origin) +",";
				}
			}

			//Classes
			if(isMulti) {
				for(int c = 0; c < Cnum; c++) {
					int cast = (int)Double.parseDouble(data[Ndim+c]);
					data[Ndim+c] = String.valueOf(cast);
				}

				str += data[Ndim + 0];
				for(int c = 1; c < Cnum; c++) {
					str += "," + data[Ndim + c];
				}
			}
			else {
				int cast = (int)Double.parseDouble(data[Ndim]);
				data[Ndim] = String.valueOf(cast);
				for(int c = 0; c < Cnum; c++) {
					if( data[Ndim].equals(classesCILAB[c]) ){
						str += classesARFF[c];
						break;
					}
				}
			}

			strs.add(str);
		}

		/* ********************************************************* */
		//Output
		String outputFile = dataName + ".arff";
		Output.writeln(outputFile, strs);
	}

	public static void cilabo2arff(String csvFile, String infoFile, String headerFile, String dataName, boolean isMulti) {
		//CSV File
		List<String> lines = null;
		try ( Stream<String> line = Files.lines(Paths.get(csvFile)) ) {
			lines = line.collect(Collectors.toList());
		} catch (IOException e) {
		    e.printStackTrace();
		}
		if(lines == null) {
			return;
		}

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

		//Header File
		List<String> header = null;
		try ( Stream<String> line = Files.lines(Paths.get(headerFile)) ) {
			header = line.collect(Collectors.toList());
		} catch (IOException e) {
		    e.printStackTrace();
		}
		if(header == null) {
			return;
		}

		/* ********************************************************* */
		String[] data = lines.get(0).split(",");
		int dataSize = Integer.parseInt(data[0]);
		int Ndim = Integer.parseInt(data[1]);
		int Cnum = Integer.parseInt(data[2]);
		lines.remove(0);
		/* ********************************************************* */

		boolean[] isCategoric = new boolean[Ndim];
		String[] attributeName = new String[Ndim];
		String[][] categoricCILAB = new String[Ndim][];
		String[][] categoricARFF = new String[Ndim][];
		String[] classesCILAB = new String[Cnum];
		String[] classesARFF = new String[Cnum];

		double[] max = new double[Ndim];
		double[] min = new double[Ndim];

		/* ********************************************************* */
		//Load Info.

		//Attribute
		infos.remove(0);	//[Attribute]
		for(int i = 0; i < Ndim; i++) {
			data = infos.get(0).split(",");
			attributeName[i] = data[0];

			if(data[1].equals("categoric")) {
				isCategoric[i] = true;
				String[] inputsCILAB = new String[data.length - 2];
				String[] inputsARFF = new String[data.length - 2];

				for(int j = 0; j < inputsCILAB.length; j++) {
					inputsARFF[j] = data[2+j].split("->")[0];
					inputsCILAB[j] = data[2+j].split("->")[1];
				}

				categoricARFF[i] = inputsARFF;
				categoricCILAB[i] = inputsCILAB;
			}
			else {
				isCategoric[i] = false;
				min[i] = Double.parseDouble(data[2]);
				max[i] = Double.parseDouble(data[3]);

			}
			infos.remove(0);
		}
		infos.remove(0);

		//Classes
		infos.remove(0);	//[Classes]
		for(int c = 0; c < Cnum; c++) {
			data = infos.get(0).split("->");
			classesARFF[c] = data[0];
			classesCILAB[c] = data[1];
			infos.remove(0);
		}

		/* ********************************************************* */
		/* ********************************************************* */
		//Output Start
		ArrayList<String> strs = new ArrayList<>();
		String str = "";

		//Add Header
		for(int i = 0; i < header.size(); i++) {
			strs.add(header.get(i));
		}

		//DataSet
		for(int p = 0; p < dataSize; p++) {
			str = "";
			data = lines.get(p).split(",");

			//Attribute
			for(int n = 0; n < Ndim; n++) {
				if(isCategoric[n]) {
					for(int i = 0; i < categoricCILAB[n].length; i++) {
						if(data[n].equals(categoricCILAB[n][i])) {
							str += categoricARFF[n][i] + ",";
							break;
						}
					}
				}
				else {
					double normalized = Double.parseDouble(data[n]);
					double origin = (max[n] - min[n]) * normalized + min[n];
					if(max[n] < origin) {
						origin = max[n];
					}
					if(min[n] > origin) {
						origin = min[n];
					}
					str += String.valueOf(origin) +",";
				}
			}

			//Classes
			if(isMulti) {
				str += data[Ndim + 0];
				for(int c = 1; c < Cnum; c++) {
					str += "," + data[Ndim + c];
				}
			}
			else {
				for(int c = 0; c < Cnum; c++) {
					if( data[Ndim].equals(classesCILAB[c]) ){
						str += classesARFF[c];
						break;
					}
				}
			}

			strs.add(str);
		}

		/* ********************************************************* */
		//Output
		String outputFile = dataName + ".arff";
		Output.writeln(outputFile, strs);
	}


	/**
	 * <p>2019/12/13現在，研究室仕様では，欠損値ありパターンは，ないものとして扱う．</p>
	 * <p>2019/12/13現在，カテゴリカルな属性は，元ファイルの値から絶対値を+1してからマイナスを付加している．</p>
	 * @param fileName
	 * @param dataName
	 * @param Ndim
	 * @param Cnum
	 * @param multiLabel
	 */
	public static void arff2cilabo(String fileName, String dataName, int Ndim, int Cnum, boolean isMulti) {
		List<String> lines = null;
		try ( Stream<String> line = Files.lines(Paths.get(fileName)) ) {
			lines = line.collect(Collectors.toList());
		} catch (IOException e) {
		    e.printStackTrace();
		}
		if(lines == null) {
			return;
		}

		/* ********************************************************* */
		ArrayList<String> header = new ArrayList<>();
		ArrayList<String> infos = new ArrayList<>();
		ArrayList<String> patterns = new ArrayList<>();

		/* ********************************************************* */
		//Dataset Details
		boolean[] isCategoric = new boolean[Ndim];
		String[] attributeName = new String[Ndim];
		String[][] categoricInputs = new String[Ndim][];
		String[][] classes = new String[Cnum][];
		String[] labelNames = new String[Cnum];

		int noLackSize = 0;
		double[] max = new double[Ndim];
		Arrays.fill(max, -Double.MAX_VALUE);
		double[] min = new double[Ndim];
		Arrays.fill(min, Double.MAX_VALUE);

		/* ********************************************************* */
		//Load Header
		int headerCount = 0;	//後でlinesからheaderを削除する用
		int nowAttribute = 0;
		int labelCount = 0;

		for(String line : lines) {
			if(line.matches("^@data.*")) {
				header.add(line);
				headerCount++;
				break;	//Finish Header
			}
			else if(line.matches("^$") ||
					line.matches("^@relation.*") ||
					line.matches("^@inputs.*") ||
					line.matches("^@outputs.*"))
			{
				header.add(line);
				headerCount++;
			}
			else if(line.matches("^@attribute.*")) {
				header.add(line);
				headerCount++;

				line = line.replace(", ", ",");
				line = line.replaceAll(" +", " ");
				String[] array = line.split("[ *\t*]");

				//Attribute
				if(nowAttribute < Ndim) {
					attributeName[nowAttribute] = array[1];

					//Check Categoric Attribute
					if(array[2].contains("{")) {
						isCategoric[nowAttribute] = true;
						array[2] = array[2].replace("{", "");
						array[2] = array[2].replace("}", "");
						categoricInputs[nowAttribute] = array[2].split(",");
					}
					else {
						isCategoric[nowAttribute] = false;
					}
					nowAttribute++;
					continue;
				}

				//Classes
				if(isMulti) {
					line = line.replace("@attribute ", "");
					labelNames[labelCount] = line.split(" \\{")[0];
					line = line.replace(labelNames[labelCount] + " ", "");
					line = line.replace("{", "");
					line = line.replace("}", "");
					classes[labelCount] = line.split(",");
				}
				else {
					array = line.split("\\{");
					String[] classNames = array[1].replace("}", "").split(", *");
					classes[labelCount] = classNames;
				}
				labelCount++;
			}
		}

		//Remove Headers
		for(int i = 0; i < headerCount; i++) {
			lines.remove(0);
		}
		/* ********************************************************* */

		/* ********************************************************* */
		//Data start

		//Normalize

		//Get Max/Min Values
		int dataSize = lines.size();
		for(int i = 0; i < dataSize; i++) {
			String[] data = lines.get(i).split(", *");

			//Check which the pattern has lack attribute.
			boolean isLack = false;
			for(int n = 0; n < Ndim; n++) {
				if(data[n].matches("\\?")) {
					isLack = true;
				}
			}
			if(isLack) {
				//Ignore the pattern which has lack attribute(s).
				continue;
			}
			noLackSize++;

			//Replace Categoric Attribute value into negative integer.
			for(int n = 0; n < Ndim; n++) {
				if(isCategoric[n]) {
					for(int j = 0; j < categoricInputs[n].length; j++) {
						if(data[n].equals(categoricInputs[n][j])) {
							data[n] = String.valueOf( -(j+1) );
							break;
						}
					}
				}
			}
			//Replace Class Name into Index for Single-Label
			if(!isMulti) {
				for(int c = 0; c < Cnum; c++) {
					if( data[data.length-1].equals(classes[0][c]) ) {
						data[data.length-1] = String.valueOf(c);
						break;
					}
				}
			}

			//Update lines
			String line = data[0];
			for(int n = 1; n < data.length; n++) {
				line += "," + data[n];
			}
			lines.set(i, line);

			//Judge the max/min values
			for(int n = 0; n < Ndim; n++) {
				double value = Double.parseDouble(data[n]);

				if(value > max[n]) {
					max[n] = value;
				}
				if(value < min[n]) {
					min[n] = value;
				}
			}
		}

		//Do Normalize
		for(int i = 0; i < dataSize; i++) {
			String[] data = lines.get(i).split(", *");

			boolean isLack = false;
			for(int n = 0; n < Ndim; n++) {
				if(!data[n].matches("^[-[0-9]].*")) {
					isLack = true;
				}
			}
			if(isLack) {
				//Ignore the pattern which has lack attribute.
				continue;
			}

			//Calculate Normalized Value.
			String line = "";
			for(int n = 0; n < Ndim; n++) {
				if(isCategoric[n]) {
					line += data[n] + ",";
				}
				else {
					double value = Double.parseDouble(data[n]);
					value = (value - min[n]) / (max[n] - min[n]);
					line += String.valueOf(value) + ",";
				}
			}

			//Classes
			if(isMulti) {
				line += data[Ndim + 0];
				for(int l = 1; l < Cnum; l++) {
					line += "," + data[Ndim + l];
				}
			}
			else {
				line += data[Ndim];
			}
			patterns.add(line);
		}
		//Add headers (dataSize, Ndim, Cnum)
		String detail = "";
		detail += String.valueOf(noLackSize);
		detail += "," + String.valueOf(Ndim);
		detail += "," + String.valueOf(Cnum);
		patterns.add(0, detail);
		/* ********************************************************* */

		/* ********************************************************* */
		//data_info.txt
		String line = null;

		//Attributes
		infos.add("[Attribute]");
		for(int n = 0; n < Ndim; n++) {
			if(isCategoric[n]) {
				line = attributeName[n];
				line += "," + "categoric";
				for(int i = 0; i < categoricInputs[n].length; i++) {
					line += "," + categoricInputs[n][i] + "->" + String.valueOf( -(i+1) );
				}
			}
			else {
				line = attributeName[n];
				line += "," + "numeric";
				line += "," + min[n];
				line += "," + max[n];
			}
			infos.add(line);
		}
		infos.add("");

		//Classes
		infos.add("[Classes]");
		if(isMulti) {
			for(int l = 0; l < labelNames.length; l++) {
				line = labelNames[l];
				line += "->" + String.valueOf(l);
				infos.add(line);
			}
		}
		else {
			for(int c = 0; c < classes.length; c++) {
				line = classes[0][c];
				line += "->" + String.valueOf(c);
				infos.add(line);
			}
		}
		infos.add("");

		//Details
		infos.add("[Details]");
		line = "# of patterns: " + String.valueOf(dataSize);
		infos.add(line);
		line = "# of attributes: " + String.valueOf(Ndim);
		infos.add(line);
		line = "# of classes: " + String.valueOf(Cnum);
		infos.add(line);
		line = "# of patterns with missing attribute(s): " + String.valueOf(dataSize - noLackSize);
		infos.add(line);
		/* ********************************************************* */

		/* ********************************************************* */
		//Output
		String outputName;

		outputName = dataName + "_header.txt";
		Output.writeln(outputName, header);

		outputName = dataName + "_info.txt";
		Output.writeln(outputName, infos);

		outputName = dataName + ".dat";
		Output.writeln(outputName, patterns);

	}

}
