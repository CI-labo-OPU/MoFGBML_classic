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

public class Masuyama {

	public static void main(String[] args) {
		System.out.println("Start");

		String sep = File.separator;
		String dataset = "Wiki10-31K";
		String fileName = "dataset" + sep + "arff" + sep + dataset + ".arff";

		//Load
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
		//Load data
		ArrayList<String> data = new ArrayList<>();
		int dimension = 0;
		for(String line : lines) {

			if(	line.matches("^$") ||
				line.matches("^@relation.*") ||
				line.matches("^@inputs.*") ||
				line.matches("^@outputs.*") ||
				line.matches("^@data.*") )
			{
			}
			else if(line.matches("^@attribute.*"))
			{
				dimension++;
			}
			else
			{
				line = line.replace("{", "");
				line = line.replace("}", "");
				data.add(line);
			}
		}
		/* ********************************************************* */
		/* ********************************************************* */
		// Sparse
		ArrayList<String> newData = new ArrayList<>();

		for(int i = 0; i < data.size(); i++) {
			String line = data.get(i);
			String[] array = new String[dimension];
			Arrays.fill(array, "0");

			String[] packet = line.split(",");
			for(int j = 0; j < packet.length; j++) {
				String[] map = packet[j].split(" ");
				int key = (int)Double.parseDouble(map[0]);
				array[key] = map[1];
			}

			String l = array[0];
			for(int j = 1; j < array.length; j++) {
				l += "," + array[j];
			}
			newData.add(l);
		}
		/* ********************************************************* */

		String outputName = "dataset" + sep + "arff" + sep + dataset + ".txt";
		Output.writeln(outputName, newData);
		System.out.println("End");

	}


}
