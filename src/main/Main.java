package main;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ForkJoinPool;

import data.Input;
import fgbml.mofgbml.MoFGBML;
import fgbml.multilabel.MultiLabel;
import fgbml.multilabel.binary_relevance.BinaryRelevance_ver1;
import fgbml.multilabel.label_power_set.LabelPowerSet_ver1;
import fgbml.multitask.MultiTask;
import fgbml.subdivision.Subdivision;
import method.MersenneTwisterFast;
import method.Output;
import method.ResultMaster;


/**
 * .jarファイルの書き出しの際は，使用したstartExperiment()を持つクラス名で書き出すこと．
 * また，エクスポート時は
 *   .classpath
 *   src/setting.properties
 *   src/consts.properties
 * のチェックを外してエクスポートすること．<br>
 * <br>
 * <h1>Command Line Arguments</h1>
 * <p>
 * args[0] : String : ./<br>
 * args[1] : String : consts (.propertiesファイル名)<br>
 * args[2] : String : setting (.propertiesファイル名)<br>
 * args[3] : String : dataName<br>
 * args[4] : int : parallelCores<br>
 * args[5] : String : saveDir<br>
 * </p>
 *
 * <p>
 * Setting.isOneExe = true の場合<br>
 * args[6] : int : rep_i<br>
 * args[7] : int : cv_i<br>
 * args[8] : String : id<br>
 * これらはstartExperiment()に渡される前に配列argsから削除されnewArgsを作る
 * </p>
 *
 */
public class Main {

	public static void main(String[] args) {
		/* ********************************************************* */
		String version = "22.0";
		System.out.println("ver.: " + version);

		/* ********************************************************* */
		//設定ファイル読込 - Load .properties
		String currentDir = args[0];
 		String constsSource = args[1];
		String settingSource = args[2];
		Consts.setConsts(currentDir, constsSource);
		Setting.setSettings(currentDir, settingSource);
		//コマンドライン引数読込 - Load command line arguments
		Setting.dataName = args[3];
		Setting.parallelCores = Integer.parseInt(args[4]);
		Setting.saveDir = args[5];
		//並列用fork join pool 生成
		if(Setting.calclationType == 0) {
			Setting.forkJoinPool = new ForkJoinPool(Setting.parallelCores);
		}


		/* ********************************************************* */
		//基本データ出力
		System.out.println("Processors: " + Runtime.getRuntime().availableProcessors() + " ");
		System.out.println();
		System.out.print("args: ");
		for(int i = 0; i < args.length; i++) {
			System.out.print(args[i] + " ");
		}
		System.out.println();
		System.out.println();


		/* ********************************************************* */
		Date start = new Date();
		System.out.println("START: ");
		System.out.println(start);

		/* ********************************************************* */
		if(Setting.isOnceExe) {
			//Execute one trial
			oneExection(args);
		}
		else {
			//Repeat x-fold cross-validation
			repeatExection(args);
		}
		/* ********************************************************* */

		Date end = new Date();
		System.out.println("END: ");
		System.out.println(end);
		/* ********************************************************* */
	}

	public static void oneExection(String[] args) {
		/* ********************************************************* */
		int rep_i = Integer.parseInt(args[6]);
		int cv_i = Integer.parseInt(args[7]);
		String id = args[8];
		ArrayList<String> tmp = new ArrayList<>(Arrays.asList(args));
		tmp.remove(6);	//rep_i
		tmp.remove(6);	//cv_i
		tmp.remove(6);	//id
		String[] newArgs = tmp.toArray(new String[0]);

		/* ********************************************************* */
		//The names of files
		String sep = File.separator;
		String traFile = System.getProperty("user.dir") + sep + "dataset" + sep + Setting.dataName + sep + "a" + rep_i + "_" + cv_i + "_" + Setting.dataName + "-10tra.dat";
		String tstFile = System.getProperty("user.dir") + sep + "dataset" + sep + Setting.dataName + sep + "a" + rep_i + "_" + cv_i + "_" + Setting.dataName + "-10tst.dat";

		/* ********************************************************* */
		//Make result directries
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmm");
		//format: ".\result\iris_20191021-1255"
		String resultRoot = System.getProperty("user.dir") + sep + "result" + sep
							+ Setting.saveDir + sep + Setting.dataName + "_" + id;
		Output.mkdirs(resultRoot);

		/* ********************************************************* */
		//Result Master
		ResultMaster resultMaster = new ResultMaster(resultRoot, id);
		//make now trial Directory
		resultMaster.setNowRep(rep_i);
		resultMaster.setNowCV(cv_i);
		resultMaster.setTrialRoot(resultRoot + sep + "trial" + rep_i+cv_i);
		Output.mkdirs(resultMaster.getTrialRoot());

		/* ********************************************************* */
		//Output "Experimental Settings"
		String consts = (new Consts()).getStaticValues();
		String settings = (new Setting()).getStaticValues();
		String fileName = resultMaster.getTrialRoot() + sep + "Consts_" + rep_i+cv_i + ".txt";
		Output.writeln(fileName, consts);
		fileName = resultMaster.getTrialRoot() + sep + "Setting_" + rep_i+cv_i + ".txt";
		Output.writeln(fileName, settings);

		/* ********************************************************* */
		//Experiment
		Experiment main = setExperiment();

		MersenneTwisterFast rnd = new MersenneTwisterFast(Setting.seed);

		System.out.println(Setting.dataName + " : TRIAL: " + rep_i + cv_i);
		main.startExperiment(newArgs,
							 traFile, tstFile,
							 rnd, resultMaster);
		System.out.println();

		/* ********************************************************* */
		//Output Times
		fileName = resultMaster.getTrialRoot() + sep + "Times_" + rep_i+cv_i + ".csv";
		resultMaster.outputTimes(fileName);

	}

	public static void repeatExection(String[] args) {
		/* ********************************************************* */
		//The names of files
		String[][] traFiles = new String[Setting.repeatTimes][Setting.crossValidationNum];
		String[][] tstFiles = new String[Setting.repeatTimes][Setting.crossValidationNum];
		for(int rep_i = 0; rep_i < Setting.repeatTimes; rep_i++) {
			for(int cv_i = 0; cv_i < Setting.crossValidationNum; cv_i++) {
				traFiles[rep_i][cv_i] = Input.makeFileNameOne(cv_i, rep_i, true);
				tstFiles[rep_i][cv_i] = Input.makeFileNameOne(cv_i, rep_i, false);
			}
		}

		/* ********************************************************* */
		//Make result directries
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmm");
		String sep = File.separator;
		String id = format.format(calendar.getTime());
		//format: ".\result\iris_20191021-1255"
		String resultRoot = System.getProperty("user.dir") + sep + "result" + sep
							+ Setting.saveDir + sep + Setting.dataName + "_" + id;
		Output.mkdirs(resultRoot);

		/* ********************************************************* */
		//Output "Experimental Settings"
		String consts = (new Consts()).getStaticValues();
		String settings = (new Setting()).getStaticValues();
		String fileName = resultRoot + sep + "Consts_" + id + ".txt";
		Output.writeln(fileName, consts);
		fileName = resultRoot + sep + "Setting_" + id + ".txt";
		Output.writeln(fileName, settings);

		/* ********************************************************* */
		//Result Master
		ResultMaster resultMaster = new ResultMaster(resultRoot, id);

		/* ********************************************************* */
		//Experiment
		Experiment main = setExperiment();

		MersenneTwisterFast rnd = new MersenneTwisterFast(Setting.seed);
		for(int rep_i = 0; rep_i < Setting.repeatTimes; rep_i++) {
			for(int cv_i = 0; cv_i < Setting.crossValidationNum; cv_i++) {
				//make now trial Directory
				resultMaster.setNowRep(rep_i);
				resultMaster.setNowCV(cv_i);
				resultMaster.setTrialRoot(resultRoot + sep + "trial" + rep_i+cv_i);
				Output.mkdirs(resultMaster.getTrialRoot());

				System.out.println(Setting.dataName + " : TRIAL: " + rep_i + cv_i);

				main.startExperiment(args,
									 traFiles[rep_i][cv_i], tstFiles[rep_i][cv_i],
									 rnd, resultMaster);

				System.out.println();
			}
		}
		/* ********************************************************* */
		//Output Times
		fileName = resultRoot + sep + "Times_" + id + ".csv";
		resultMaster.outputTimes(fileName);

	}

	public static Experiment setExperiment() {
		Experiment main = null;

		switch(Setting.experimentName) {
		case "MoFGBML":
			main = new MoFGBML();
			break;
		case "Subdivision":
			main = new Subdivision();
			break;
		case "MoFGBMLML":
			main = new MultiLabel();
			break;
		case "BinaryRelevance":
			main = new BinaryRelevance_ver1();
			break;
		case "LabelPowerSet":
			main = new LabelPowerSet_ver1();
			break;
		case "MultiTask":
			main = new MultiTask();
			break;
		}

		return main;
	}

}
















