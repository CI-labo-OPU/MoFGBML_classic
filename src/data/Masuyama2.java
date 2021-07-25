package data;

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

public class Masuyama2 {

	public static void test(String dataset) {
		String sep = File.separator;
		String fileName = "dataset" + sep + "arff" + sep + dataset + sep + "validation.txt";

		List<String> lines = null;
		try ( Stream<String> line = Files.lines(Paths.get(fileName)) ) {
			lines = line.collect(Collectors.toList());
		} catch (IOException e) {
		    e.printStackTrace();
		}
		if(lines == null) {
			return;
		}


		System.out.println();
	}


	public static void main(String[] args) {
		System.out.println("START");

		String sep = File.separator;
		String dataset = "EURLex-4.3K";
		System.out.println("Dataset: " + dataset);
		String rawFile = "dataset" + sep + "arff" + sep + dataset + sep + "train.txt";

//		test(dataset);

		//Load
		List<String> lines = null;
		try ( Stream<String> line = Files.lines(Paths.get(rawFile)) ) {
			lines = line.collect(Collectors.toList());
		} catch (IOException e) {
		    e.printStackTrace();
		}
		if(lines == null) {
			return;
		}

		String[] header = lines.remove(0).split(" ");

		int dataSize = Integer.parseInt(header[0]);
		int dimension = Integer.parseInt(header[1]);
		int Cnum = Integer.parseInt(header[2]);

		ArrayList<String> outputLines = new ArrayList<>();
		String head = String.valueOf(dataSize) + "," + String.valueOf(dimension) + "," + String.valueOf(Cnum);
		outputLines.add(head);

		//
		for(int i = 0; i < dataSize; i++) {
			String[] line = lines.get(i).split(" ");

			// Class Label
			String[] classLabel = new String[Cnum];
			Arrays.fill(classLabel, "0");
			String[] cIndex = line[0].split(",");
			if(!cIndex[0].equals("")) {
				for(int j = 0; j < cIndex.length; j++) {
					int index = Integer.parseInt(cIndex[j]);
					classLabel[index] = "1";
				}
			}

			// Attribute
			String[] attribute = new String[dimension];
			Arrays.fill(attribute, "0");
			for(int j = 1; j < line.length; j++) {
				int index = Integer.parseInt(line[j].split(":")[0]);
				attribute[index] = line[j].split(":")[1];
			}

			// to Output Line
			String l = attribute[0];
			for(int j = 1; j < attribute.length; j++) {
				l += "," + attribute[j];
			}
			for(int j = 0; j < classLabel.length; j++) {
				l += "," + classLabel[j];
			}
			outputLines.add(l);

		}

		String outputName = "dataset" + sep + "arff" + sep + "txt" + sep + dataset+".txt";
		Output.writeln(outputName, outputLines);

		System.out.println("END");


	}

}
