package gui;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;

import data.Input;
import data.SingleDataSetInfo;
import fuzzy.AllCombiRuleSet;
import fuzzy.StaticFuzzyFunc;
import main.Setting;
import method.MersenneTwisterFast;
import method.Output;
import method.ResultMaster;

public class ISIS2019_Main {

	public static void startExperiment( String[] args, String traFile, String tstFile,
										MersenneTwisterFast rnd, ResultMaster resultMaster) {

		/* ********************************************************* */
		//START:

		Setting.forkJoinPool = new ForkJoinPool(3);

		/* ********************************************************* */
		//Load Dataset
		SingleDataSetInfo Dtra = new SingleDataSetInfo();
		SingleDataSetInfo Dtst = new SingleDataSetInfo();
		Input.inputFile(Dtra, traFile);
		Input.inputFile(Dtst, tstFile);

		/* ********************************************************* */
		//Make result directry
		String sep = File.separator;
		String resultRoot = resultMaster.getRootDir();
		String xmlDir = resultRoot + sep + "xml";
		Output.mkdirs(xmlDir);
		String errorDir = resultRoot + sep + "errors";
		Output.mkdirs(errorDir);

		int nowCV = resultMaster.getNowCV();
		int nowRep = resultMaster.getNowRep();

		/* ********************************************************* */
		//Start experiment
		double F = 1.0;
		int[] K = new int[] {2, 3, 4, 5};

		ArrayList<String> strs = new ArrayList<>();
		String str;
		//Step 0. Result Header
		str = "K,Dtra,Dtst,ruleNum,ruleLength,Dtra2,Dtst2,ruleNum2,ruleLength2";
		strs.add(str);

		for(int k = 0; k < K.length; k++) {

			//Step 1. Initialize Fuzzy Sets
			StaticFuzzyFunc.classEntropyInit(Dtra, K[k], F);

			//Step 2. Output Knowledge Base as XML.
			String fileName = xmlDir + sep + Setting.dataName + "-" + nowRep + nowCV + "_K" + K[k] + ".xml";
			StaticFuzzyFunc.outputFML(fileName);

			//Step 3. Make Rule set composed of all combinations of antecedent fuzzy sets.
			AllCombiRuleSet ruleSet = new AllCombiRuleSet();
			ruleSet.init(Dtra, Setting.forkJoinPool);

			//Step 4. Classification
			double missTra = ruleSet.calcMissRateParallel(Dtra, false);
			double missTst = ruleSet.calcMissRateParallel(Dtst, false);
			int ruleNum = ruleSet.getRuleNum();
			int ruleLength = ruleSet.getRuleLength();

			//Step 5. Remove rules
			ruleSet.removeRule();
			ruleSet.calcRuleLength();

			//Step 6. Re-classification
			double missTra2 = ruleSet.calcMissRateParallel(Dtra, false);
			double missTst2 = ruleSet.calcMissRateParallel(Dtst, false);
			int ruleNum2 = ruleSet.getRuleNum();
			int ruleLength2 = ruleSet.getRuleLength();

			//Step 7. Save result on k.
			str = String.valueOf(K[k]);
			str += "," + missTra;
			str += "," + missTst;
			str += "," + ruleNum;
			str += "," + ruleLength;
			str += "," + missTra2;
			str += "," + missTst2;
			str += "," + ruleNum2;
			str += "," + ruleLength2;

			strs.add(str);
		}

		String fileName = errorDir + sep + Setting.dataName + "-" + nowRep + nowCV + ".csv";
		Output.writeln(fileName, strs);




	}


}
