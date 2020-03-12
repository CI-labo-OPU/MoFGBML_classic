package main;

import java.io.File;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ForkJoinPool;

import method.OsSpecified;

/**
 * コマンドライン引数として，
 * カレントディレクトリ
 * setting.propertiesのファイル名
 * を与える．<br>
 * <br>
 * 例: > Java -jar xx.jar ./ setting<br>
 * <br>
 * setSettings()メソッドに，以上の引数を与えてプロパティファイルを読み込む．
 *
 *
 */
public class Setting {
	// ************************************************************
	public static int osType;
	public static ForkJoinPool forkJoinPool = null;

	/** Using Main Experiment */
	public static String experimentName = "Subdivision";

	public static int calclationType = 0;	//0:Single node, 1:Apache Spark, 2:Simple Socket

	/* ********************************************************* */
	/** Do use command line argument for Below parameters<br>
	 *  true:use, false:properties file */
	public static boolean useArgs = true;
	/** Dataset Name */
	public static String dataName = "iris";
	/** Place of stored results */
	public static String saveDir = "results";
	/** #of CPU cores */
	public static int parallelCores = 1;
	/* ********************************************************* */

	/** Termination Criteria (true: generation, false: evaluation) */
	public static boolean terminationCriteria = true;
	/** #of Generation */
	public static int generationNum = 1000;
	/** #of Evaluation */
	public static int evaluationNum = 50000;
	/** Timing of output Population (generation based) */
	public static int timingOutput = 50;
	/** #of Population */
	public static int populationSize = 50;
	/** #of Offspring */
	public static int offspringSize = 50;
	@Deprecated
	//TODO MOPクラスで指定
	public static int objectiveNum = 2;	// #of Objective
	/** EMOA (0:NSGA-II, 1:WS, 2:TCH, 3:PBI, 4:IPBI, 5:AOF) */
	public static int emoType = 0;
	/** x-CrossValidation */
	public static int crossValidationNum = 10;
	/** Time of repeat CV */
	public static int repeatTimes = 3;
	/** Random seed */
	public static int seed = 2019;
	/** Dataset preDivide Number */
	public static int preDivNum = 1;
	/** (true:Stop by each trial) */
	public static boolean isOnceExe = true;
	/** #of island */
	public static int islandNum = 1;
	/** Interval of migration */
	public static int migrationItv = 100;
	/** Interval of rotation Dataset */
	public static int rotationItv = 100;

	//Parallel Environment
	public static boolean isDistributed = true;
	public static String dirLocation = "";
	public static int serverNum = 4;	// #of servers
	public static int portNum = 50000;	//Port Number
	public static int threadNum = 18;
	public static ArrayList<String> nodeNames;
	public static InetSocketAddress[] serverList;

	// ************************************************************
	public static void setSettings(String dir, String source) {

		URLClassLoader urlLoader = null;
		ResourceBundle bundle = null;
		try {
			urlLoader = new URLClassLoader(new URL[] {new File(dir).toURI().toURL()});
			bundle = ResourceBundle.getBundle(source, Locale.getDefault(), urlLoader);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		//The type of OS
		if(OsSpecified.isLinux() == true || OsSpecified.isMac() == true) {
			osType = Consts.UNIX;	//Linux or Mac
			System.out.println("OS: Linux or Mac");
		} else {
			osType = Consts.WINDOWS;	//windows
			System.out.println("OS: Windows");
		}

		if(bundle.containsKey("experimentName")) { experimentName = bundle.getString("experimentName"); }

		if(bundle.containsKey("calclationType")) { calclationType = Integer.parseInt(bundle.getString("calclationType")); }

		if(bundle.containsKey("useArgs")) { useArgs = Boolean.parseBoolean(bundle.getString("useArgs")); }

		if(bundle.containsKey("dataName")) { dataName = bundle.getString("dataName"); }

		if(bundle.containsKey("saveDir")) { saveDir = bundle.getString("saveDir"); }

		if(bundle.containsKey("parallelCores")) { parallelCores = Integer.parseInt(bundle.getString("parallelCores")); }

		if(bundle.containsKey("terminationCriteria")) { terminationCriteria = Boolean.parseBoolean(bundle.getString("terminationCriteria")); }

		if(bundle.containsKey("generationNum")) { generationNum = Integer.parseInt(bundle.getString("generationNum")); }

		if(bundle.containsKey("evaluationNum")) { evaluationNum = Integer.parseInt(bundle.getString("evaluationNum")); }

		if(bundle.containsKey("timingOutput")) { timingOutput = Integer.parseInt(bundle.getString("timingOutput")); }

		if(bundle.containsKey("populationSize")) { populationSize = Integer.parseInt(bundle.getString("populationSize")); }

		if(bundle.containsKey("offspringSize")) { offspringSize = Integer.parseInt(bundle.getString("offspringSize")); }

		if(bundle.containsKey("objectiveNum")) { objectiveNum = Integer.parseInt(bundle.getString("objectiveNum")); }

		if(bundle.containsKey("emoType")) { emoType = Integer.parseInt(bundle.getString("emoType")); }

		if(bundle.containsKey("crossValidationNum")) { crossValidationNum = Integer.parseInt(bundle.getString("crossValidationNum")); }

		if(bundle.containsKey("repeatTimes")) { repeatTimes = Integer.parseInt(bundle.getString("repeatTimes")); }

		if(bundle.containsKey("seed")) { seed = Integer.parseInt(bundle.getString("seed")); }

		if(bundle.containsKey("preDivNum")) { preDivNum = Integer.parseInt(bundle.getString("preDivNum")); }

		if(bundle.containsKey("isOnceExe")) { isOnceExe = Boolean.parseBoolean(bundle.getString("isOnceExe")); }

		if(bundle.containsKey("islandNum")) { islandNum = Integer.parseInt(bundle.getString("islandNum")); }

		if(bundle.containsKey("migrationItv")) { migrationItv = Integer.parseInt(bundle.getString("migrationItv")); }

		if(bundle.containsKey("rotationItv")) { rotationItv = Integer.parseInt(bundle.getString("rotationItv")); }

		if(bundle.containsKey("isDistributed")) { isDistributed = Boolean.parseBoolean(bundle.getString("isDistributed")); }

		if(bundle.containsKey("dirLocation")) { dirLocation = bundle.getString("dirLocation"); }

		if(bundle.containsKey("serverNum")) { serverNum = Integer.parseInt(bundle.getString("serverNum")); }

		if(bundle.containsKey("portNum")) { portNum = Integer.parseInt(bundle.getString("portNum")); }

		if(bundle.containsKey("threadNum")) { threadNum = Integer.parseInt(bundle.getString("threadNum")); }

		if(bundle.containsKey("nodeNames")) {
			int nodeNum = bundle.getString("nodeNames").split(",").length;
			nodeNames = new ArrayList<String>();
			for(int i = 0; i < nodeNum; i++) {
				nodeNames.add( bundle.getString("nodeNames").split(",")[i] );
			}
			serverList = new InetSocketAddress[nodeNum];
			for(int i = 0; i < nodeNum; i++) {
				serverList[i] = new InetSocketAddress(nodeNames.get(i), portNum);
			}
		}

		//設定値を追加したい時は，以下のvariableとkeyを追加すれば良い
		//if(bundle.containsKey("key")){
		//	variable = Integer.parseInt(bundle.getStrint("key"));
		//}

		bundle = null;

	}

	public String getStaticValues() {
		StringBuilder sb = new StringBuilder();
		String sep = System.lineSeparator();
		sb.append("Class: " + this.getClass().getCanonicalName() + sep);
		sb.append("Settings: " + sep);
		for(Field field : this.getClass().getDeclaredFields()) {
			try {
				field.setAccessible(true);
				sb.append(field.getName() + " = " + field.get(this) + sep);
			} catch(IllegalAccessException e) {
				sb.append(field.getName() + " = " + "access denied" + sep);
			}
		}
		return sb.toString();
	}






}
