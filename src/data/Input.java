package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.Setting;

/**
 *
 *
 */
public class Input {
	// ************************************************************


	// ************************************************************


	// ************************************************************

	public static List<String> inputAsListString(String fileName) {
		List<String> lines = new ArrayList<>();
		try ( Stream<String> line = Files.lines(Paths.get(fileName)) ) {
		    lines = line.collect( Collectors.toList() );

		} catch (IOException e) {
		    e.printStackTrace();
		}

		return lines;
	}

	/**
	 *
	 * @param fileName
	 * @return : List{@literal <double[]>}
	 */
	public static List<double[]> inputAsList(String fileName) {
		List<double[]> lines = new ArrayList<double[]>();
		try ( Stream<String> line = Files.lines(Paths.get(fileName)) ) {
		    lines =
		    	line.map(s ->{
		    	String[] numbers = s.split(",");
		    	double[] nums = new double[numbers.length];

		    	//値が無い場合の例外処理
		    	for (int i = 0; i < nums.length; i++) {
//		    		if (numbers[i].matches("^([1-9][0-9]*|0|/-)(.[0-9]+)?$") ){
		    			nums[i] = Double.parseDouble(numbers[i]);
//		    		}else{
//		    			nums[i] = 0.0;
//		    		}
				}
		    	return nums;
		    })
		    .collect( Collectors.toList() );

		} catch (IOException e) {
		    e.printStackTrace();
		}

		return lines;
	}

	public static void inputSubdata(SingleDataSetInfo origin, SingleDataSetInfo divided, String fileName) {
		List<double[]> lines = inputAsList(fileName);

		//1行目はデータのパラメータ
		divided.setDataSize( (int)lines.get(0)[0] );
		divided.setNdim( (int)lines.get(0)[1] );
		divided.setCnum( (int)lines.get(0)[2] );
		lines.remove(0);

		//2行目以降は属性値（最終列はクラス）
		for(int p = 0; p < lines.size(); p++) {
			SinglePattern pattern = origin.getPatternWithID((int)lines.get(p)[0]);
			divided.addPattern(pattern);
		}
	}

	/**
	 * <h1>Input File for Single-Label Classification Dataset</h1>
	 * @param data : DataSetInfo
	 * @param fileName : String
	 */
	public static void inputFile(SingleDataSetInfo data, String fileName) {
		List<double[]> lines = inputAsList(fileName);

		//1行目はデータのパラメータ
		data.setDataSize( (int)lines.get(0)[0] );
		data.setNdim( (int)lines.get(0)[1] );
		data.setCnum( (int)lines.get(0)[2] );
		lines.remove(0);

		//2行目以降は属性値（最終列はクラス）
		for(int p = 0; p < lines.size(); p++) {
			SinglePattern pattern = new SinglePattern(p, lines.get(p));
			data.addPattern(pattern);
		}
	}

	/**
	 * <h1>Input File for Multi-Label Classification Dataset</h1>
	 * @param data : MultiDataSetInfo
	 * @param fileName : String
	 */
	public static void inputMultiLabel(MultiDataSetInfo data, String fileName) {
		List<double[]> lines = inputAsList(fileName);

		//1行目はデータの詳細
		data.setDataSize( (int)lines.get(0)[0] );
		data.setNdim( (int)lines.get(0)[1] );
		data.setCnum( (int)lines.get(0)[2] );
		lines.remove(0);

		//2行目以降は属性値
		for(int p = 0; p < lines.size(); p++) {
			MultiPattern pattern = new MultiPattern(p, lines.get(p), data.getNdim(), data.getCnum());
			data.addPattern(pattern);
		}

	}

	/**
	 * 引数に与えられた試行回数に応じたファイル名を作成するメソッド
	 * @param cv_i
	 * @param rep_i
	 * @param isTra
	 * @return String : "rep_i"_"cv_i"-10"isTra"に応じたファイル名
	 */
	public static String makeFileNameOne(int cv_i, int rep_i, boolean isTra) {
		String sep = File.separator;
		String fileName = "";
		if(isTra) {
			fileName = System.getProperty("user.dir") + sep + "dataset" + sep + Setting.dataName + sep + "a" + rep_i + "_" + cv_i + "_" + Setting.dataName + "-10tra.dat";
		} else {
			fileName = System.getProperty("user.dir") + sep + "dataset" + sep + Setting.dataName + sep + "a" + rep_i + "_" + cv_i + "_" + Setting.dataName + "-10tst.dat";
		}
		return fileName;
	}


	@Deprecated
	//使用されていない？
	public static void inputFileOneLine(SingleDataSetInfo data, String fileName, String dirLocation){

		String line = "";
		try{
			File file = new File(fileName);
			BufferedReader br = new BufferedReader( new FileReader(file) );
			line = br.readLine();
			br.close();
		}
		catch(FileNotFoundException e){
		  System.out.println(e);
		}catch(IOException e){
		  System.out.println(e);
		}
		//1行目はデータのパラメータ
		String[] splitLine = line.split(",");
		data.setDataSize( Integer.parseInt(splitLine[0]) );
		data.setNdim( Integer.parseInt(splitLine[1]) );
		data.setCnum( Integer.parseInt(splitLine[2]) );

	}


}
