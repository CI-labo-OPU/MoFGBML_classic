package data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Extends {

	public static void main(String[] args) {
		/* ----------------------------- */
		String fileName = args[0];
		/* ----------------------------- */

		System.out.println("START");
		System.out.println("File: " + fileName);

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

		String[] header = lines.remove(0).split(" ");

		int dataSize = Integer.parseInt(header[0]);
		int dimension = Integer.parseInt(header[1]);
		int Cnum = Integer.parseInt(header[2]);

		ArrayList<String> outputLines = new ArrayList<>();
		String head = String.valueOf(dataSize) + "," + String.valueOf(dimension) + "," + String.valueOf(Cnum);
		outputLines.add(head);

		int count = 0;
		for(int i = 0; i < dataSize; i++) {
			if(count % 100 == 0) {
				System.out.print(".");
			}

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

			count++;
		}
		System.out.println();

		String outputName = args[1];
		writeln(outputName, outputLines);

		System.out.println("END");


	}

	/**
	 * ArrayList用
	 * @param fileName
	 * @param strs : ArrayList{@literal <String>}
	 */
	public static void writeln(String fileName, ArrayList<String> strs) {
		String[] array = (String[]) strs.toArray(new String[0]);
		writeln(fileName, array);
	}

	/**
	 * 配列用
	 * @param fileName
	 * @param array : String[]
	 */
	public static void writeln(String fileName, String[] array){

		try {
//			FileWriter fw = new FileWriter(fileName, true);
			FileWriter fw = new FileWriter(fileName, false);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );
			for(int i=0; i<array.length; i++){
				 pw.println(array[i]);
			}
			pw.close();
	    }
		catch (IOException ex){
			ex.printStackTrace();
	    }
	}

}
