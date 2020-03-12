package fuzzy.multi_label.fuzzieee2020;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import data.Input;
import data.MultiDataSetInfo;
import fgbml.multilabel.MOP_ExactMatchError;
import fgbml.multilabel.MultiPittsburgh;
import fgbml.multilabel.Output_MultiLabel;
import fgbml.multilabel.Problem_MultiLabel;
import fgbml.problem.OutputClass;
import fuzzy.StaticFuzzyFunc;
import fuzzy.multi_label.MultiRule;
import fuzzy.multi_label.MultiRuleSet;
import ga.Population;
import main.Consts;
import main.Setting;
import method.Output;

/**
 * ruleSet.txtを読み込んでもう一度個体群を生成し，appendixを出力するプログラム
 *
 */
public class Examine8_readRuleTxt {
	public static void setting() {
		String[] args = new String[] {"./", "consts", "setting", "5"};
		//設定ファイル読込 - Load .properties
		String currentDir = args[0];
		String constsSource = args[1];
		String settingSource = args[2];
		Consts.setConsts(currentDir, constsSource);
		Setting.setSettings(currentDir, settingSource);
		//コマンドライン引数読込 - Load command line arguments
		Setting.parallelCores = Integer.parseInt(args[3]);
		Setting.forkJoinPool = new ForkJoinPool(Setting.parallelCores);
	}

	public static void main(String[] args) {
		/* ********************************************************* */
		setting();
		/* ********************************************************* */
		String sep = File.separator;
		String dataName = args[0];
		String objective = args[1];
//		String dataName = "flags";
//		String[] objectives = {"SubAcc", "HammingLoss", "Fmeasure"};
		/* ********************************************************* */
		int cv = 10;
		int repeat = 3;

		String[][] traFiles = new String[repeat][cv];
		String[][] tstFiles = new String[repeat][cv];
		for(int rep_i = 0; rep_i < repeat; rep_i++) {
			for(int cv_i = 0; cv_i < cv; cv_i++) {
				traFiles[rep_i][cv_i] = "dataset" + sep + dataName + sep + "a" + rep_i + "_" + cv_i + "_" + dataName + "-10tra.dat";
				tstFiles[rep_i][cv_i] = "dataset" + sep + dataName + sep + "a" + rep_i + "_" + cv_i + "_" + dataName + "-10tst.dat";
			}
		}

		String dirName = "result" + sep
				+ "MOP_" + objective + sep;
		try(DirectoryStream<Path> entries = Files.newDirectoryStream(Paths.get(dirName), dataName + "*")){
			dirName = entries.iterator().next().toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for(int rep_i = 0; rep_i < repeat; rep_i++) {
			for(int cv_i = 0; cv_i < cv; cv_i++) {
				String outputDir = dirName + sep + "new";
				Output.mkdirs(outputDir);
				outputDir += sep + "trial"+rep_i+cv_i + sep + "population" + sep + "individual";
				Output.mkdirs(outputDir);

				String workDir = dirName + sep + "trial"+rep_i+cv_i + sep + "population" + sep;
				String result = oneTrial(workDir, traFiles[rep_i][cv_i], tstFiles[rep_i][cv_i]);

				String outputFile = outputDir + sep + "gen1000.csv";
				Output.writeln(outputFile, result);
			}
		}

	}

	public static String oneTrial(String dirName, String traFile, String tstFile) {
		/* ********************************************************* */
		String sep = File.separator;
		/* ********************************************************* */
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		MultiDataSetInfo Dtst = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, traFile);
		Input.inputMultiLabel(Dtst, tstFile);
		/* ********************************************************* */
		int Ndim = Dtra.getNdim();
		int Cnum = Dtst.getCnum();
		StaticFuzzyFunc.homogeneousInit(Ndim);
		Problem_MultiLabel mop = new MOP_ExactMatchError(Dtra, Dtst);
		/* ********************************************************* */

		String fileName = dirName + sep + "ruleset" + sep + "gen1000.txt";
		Population<MultiPittsburgh> population = loadRuleSet(fileName, mop);
		mop.setAppendix(population);

		OutputClass<MultiPittsburgh> output = new Output_MultiLabel();
		String str = output.outputPittsburgh(population);

		return str;
	}

	public static Population<MultiPittsburgh> loadRuleSet(String fileName, Problem_MultiLabel mop) {
		/* ********************************************************* */
		MultiDataSetInfo Dtra = (MultiDataSetInfo)mop.getTrain();
		/* ********************************************************* */
		List<String> lines = Input.inputAsListString(fileName);

		int Ndim = Dtra.getNdim();
		int Cnum = Dtra.getCnum();

		//Start
		int popSize = 60;
		Population<MultiPittsburgh> population = new Population<>();
		for(int p = 0; p < popSize; p++) {
			MultiPittsburgh individual = new MultiPittsburgh();
			MultiRuleSet ruleSet = new MultiRuleSet();
			/* ********************************************************* */
			lines.remove(0);	//******
			lines.remove(0);	//pop_
			/* ********************************************************* */
			String[] line = lines.get(0).split(" ");
			int ruleNum = Integer.parseInt(line[1]);
			lines.remove(0);	//ruleNum:
			lines.remove(0);	//rank
			lines.remove(0);	//crowding
			lines.remove(0);	//---Dtra---
			lines.remove(0);	//ExactMatch
			lines.remove(0);	//Fmeasure
			lines.remove(0);	//Recall
			lines.remove(0);	//Precision
			lines.remove(0);	//HammingLoss
			lines.remove(0);	//---Dtst---
			lines.remove(0);	//ExactMatch
			lines.remove(0);	//Fmeasure
			lines.remove(0);	//Recall
			lines.remove(0);	//Precision
			lines.remove(0);	//HammingLoss
			lines.remove(0);	//f0
			lines.remove(0);	//f1
			lines.remove(0);	//---
			/* ********************************************************* */

			for(int r = 0; r < ruleNum; r++) {
				line = lines.get(0).split(",");
				String[] separate = line[0].split(" +");

				int[] antecedent = new int[Ndim];
				for(int n = 0; n < Ndim; n++) {
					antecedent[n] = Integer.parseInt(separate[1+n]);
				}

				MultiRule rule = new MultiRule(antecedent, Cnum);
				ruleSet.addRule(rule);

				lines.remove(0);
			}
			ruleSet.learning(Dtra, Setting.forkJoinPool);
			ruleSet.radixSort();
			ruleSet.calcRuleLength();
			individual.setRuleSet(ruleSet);
			individual.ruleset2michigan();
			individual.michigan2pittsburgh();
			individual.setObjectiveNum(2);
			mop.evaluate(individual);
			population.addIndividual(individual);

			/* ********************************************************* */
			lines.remove(0);	//*****
			lines.remove(0);	//\n
			/* ********************************************************* */
		}

		return population;
	}

}

































