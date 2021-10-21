package fgbml.multitask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import data.Input;
import data.MultiDataSetInfo;
import fgbml.problem.OutputClass;
import fuzzy.StaticFuzzyFunc;
import ga.Population;
import main.Consts;
import main.Experiment;
import main.Setting;
import method.MersenneTwisterFast;
import method.Output;
import method.ResultMaster;
import time.TimeWatcher;

/**
 * @version 1.0
 *
 * Multi-Tasking用のMainメソッド
 *
 * 引数として，MOP番号を受け取る
 *
 * <p>*****************************</p>

 */
public class MultiTask implements Experiment {

	@SuppressWarnings("unchecked")
	public void startExperiment( String[] args, String traFile, String tstFile,
										MersenneTwisterFast rnd, ResultMaster resultMaster) {
		/* ********************************************************* */
		//START:

		/* ********************************************************* */
		//Load Dataset
		MultiDataSetInfo Dtra = new MultiDataSetInfo();
		MultiDataSetInfo Dtst = new MultiDataSetInfo();
		Input.inputMultiLabel(Dtra, traFile);
		Input.inputMultiLabel(Dtst, tstFile);

		/* ********************************************************* */
		//Make result directry
		String sep = File.separator;
		String resultRoot = resultMaster.getRootDir();

		String trialRoot = resultMaster.getTrialRoot();

		/* ********************************************************* */
		//Initialize Fuzzy Sets
		StaticFuzzyFunc.initFuzzy(Dtra);

		/* ********************************************************* */
		//MOP No.
		int mopNo;
		if(args.length < 7) {
			mopNo = 1;
		}
		else {
			mopNo = Integer.parseInt(args[6]);
		}

		/* ********************************************************* */
		//command line argument

		//移住間隔
		int intervalMigration = 0;
		//移住個体数
		int numMigration = 0;

		if(args.length < 8) {
			intervalMigration = 10;
			numMigration = 10;
		}
		else {
			intervalMigration = Integer.parseInt(args[7]);
			numMigration = Integer.parseInt(args[8]);
		}

		/* ********************************************************* */
		//各タスクの各世代における結果保持用
//		ArrayList<ArrayList<String>> individualPopulation = new ArrayList<>();
//		ArrayList<ArrayList<String>> ruleSetsPopulation = new ArrayList<>();
//		ArrayList<ArrayList<String>> individualOffspring = new ArrayList<>();
//		ArrayList<ArrayList<String>> ruleSetsOffspring = new ArrayList<>();
//		ArrayList<String> genCounts = new ArrayList<>();

		/* ********************************************************* */
		//Generate OutputClass
		OutputClass<MultiPittsburgh> output = new Output_MultiLabel();

		/* ********************************************************* */
		//Timer start
		TimeWatcher timeWatcher = new TimeWatcher();	//All Exprimeint executing time
		TimeWatcher evaWatcher = new TimeWatcher();		//Evaluating time
		timeWatcher.start();

		/* ********************************************************* */
		/* ********************************************************* */
		//MultiTask Start
		int genCount = 0;

		//島作成＝タスク作成
		TaskManager world = new TaskManager();
		world.setNumMigration(numMigration);

		MOP1 mop1 = new MOP1(Dtra, Dtst);
		MOP2 mop2 = new MOP2(Dtra, Dtst);
		MOP3 mop3 = new MOP3(Dtra, Dtst);
//		MOP4 mop4 = new MOP4(Dtra, Dtst);
//		MaOP maop = new MaOP(Dtra, Dtst);
		Task task1 = new Task(0, mop1);
		Task task2 = new Task(1, mop2);
		Task task3 = new Task(2, mop3);
//		Task task4 = new Task(3, mop4);
//		Task task5 = new Task(4, maop);
		world.addTask(task1);
		world.addTask(task2);
		world.addTask(task3);
//		world.addTask(task4);
//		world.addTask(task5);

		//0. 結果用ディレクトリ作成（マルチタスキング用）
		this.makeResultDir(trialRoot, world.getTaskNum());
		String[] populationDir = new String[world.getTaskNum()];
		String[] offspringDir = new String[world.getTaskNum()];
		String[] ruleSetDir = new String[world.getTaskNum()];
		for(int i = 0; i < world.getTaskNum(); i++) {
			String taskDirName = trialRoot + sep + "task"+(i+1);
			populationDir[i] = taskDirName + sep + Consts.POPULATION;
			offspringDir[i] = taskDirName + sep + Consts.OFFSPRING;
			ruleSetDir[i] = taskDirName + sep + Consts.RULESET;
			Output.mkdirs(ruleSetDir[i]);

			String fileName;
			String header;

			// Objective values
			header = ((Output_MultiLabel)output).pittsburghHeader(world.getTask(i).getMOP().getObjectiveNum());
			fileName = populationDir[i] + sep + "individual.csv";
			Output.write(fileName, header);
//			fileName = offspringDir[i] + sep + "individual.csv";
//			Output.write(fileName, header);

//			individualPopulation.add(new ArrayList<>());
//			ruleSetsPopulation.add(new ArrayList<>());
//			individualOffspring.add(new ArrayList<>());
//			ruleSetsOffspring.add(new ArrayList<>());
		}

		//1. 初期個体群生成＆評価
		for(int i = 0; i < world.getTaskNum(); i++) {
			Task task = world.getTask(i);
			task.initialize(rnd);
			task.populationEvaluation();
			task.nonDominatedSort(task.getPopulation());
		}
//		genCount++;
		//初期個体群結果保持
//		genCounts.add(String.valueOf(genCount));
		for(int i = 0; i < world.getTaskNum(); i++) {
			Task task = world.getTask(i);
			String fileName;
			String individual;
			String ruleSetTxt;

			//Population
			task.calcAppendix(task.getPopulation());
			setGeneration(genCount, task.getPopulation());
			individual = output.outputPittsburgh(task.getPopulation());

			fileName = populationDir[i] + sep + "individual.csv";
			Output.write(fileName, individual);
//			fileName = offspringDir[i] + sep + "individual.csv";
//			Output.write(fileName, individual);

//			// Decision values
//			fileName = ruleSetDir[i] + sep + "gen" + genCount + ".txt";
//			ruleSetTxt = task.getPopulation().toString();
//			Output.write(fileName, ruleSetTxt);


//			ruleSets = output.outputRuleSet(task.getPopulation());
//			individualPopulation.get(i).add(individual);
//			ruleSetsPopulation.get(i).add(ruleSets);
		}
//		System.out.print("0");

		//2. GA Searching Frame
		int detailCount = 0;
		while(true) {
			/* ********************************************************* */
			//Output "Period" per const interval.
			if(genCount % Consts.PER_SHOW_GENERATION_NUM == 0) {
				if(detailCount % Consts.PER_SHOW_GENERATION_DETAIL == 0) {
					System.out.print(genCount);
				} else {
					System.out.print(".");
				}
				detailCount++;
			}

			if(genCount >= Setting.generationNum) {
				break;
			}
			/* ********************************************************* */
			if(genCount % intervalMigration == (intervalMigration-1)) {
				/* 情報交換する */

				//1. 各個体群を複雑順にソートしたリストを生成
				ArrayList<List<MultiPittsburgh>> immigrant_list = new ArrayList<>();
				for(int i = 0; i < world.getTaskNum(); i++) {
					//個体群を複雑順にソート
					immigrant_list.add(world.getTask(i).getSortedListByComplex());
				}

				//2. 子個体生成
				for(int i = 0; i < world.getTaskNum(); i++) {
					Task task = world.getTask(i);
					task.immigrantMakeOffspring(genCount, immigrant_list, world.getTaskNum(), world.getNumMigration(), rnd);
//TODO					task.nonDominatedSort(task.getOffspring());
				}

				//3. 子個体群評価
				for(int i = 0; i < world.getTaskNum(); i++) {
					Task task = world.getTask(i);
					task.offspringEvaluation();
					task.nonDominatedSort(task.getOffspring());
				}

				//4. 環境選択
				for(int i = 0; i < world.getTaskNum(); i++) {
					Task task = world.getTask(i);
					task.environmentalSelection();
				}
			}
			else {
				/* 情報交換しない */
				//2. 子個体生成
				for(int i = 0; i < world.getTaskNum(); i++) {
					Task task = world.getTask(i);
					task.normalMakeOffspring(genCount, Setting.offspringSize, rnd);
				}

				//3. 子個体群評価
				for(int i = 0; i < world.getTaskNum(); i++) {
					Task task = world.getTask(i);
					task.offspringEvaluation();
					task.nonDominatedSort(task.getOffspring());
				}

				//4. 環境選択
				for(int i = 0; i < world.getTaskNum(); i++) {
					Task task = world.getTask(i);
					task.environmentalSelection();
				}
			}
			genCount++;

			/* ********************************************************* */
			//Output current Population & new Offspring
			timeWatcher.stop();
			if(genCount % Setting.timingOutput == 0) {
//				genCounts.add(String.valueOf(genCount));
				//Appendix Information
				for(int i = 0; i < world.getTaskNum(); i++) {
					Task task = world.getTask(i);
					String fileName;
					String individual;
					String ruleSetTxt;

					//Population
					task.nonDominatedSort(task.getPopulation());
					task.calcAppendix(task.getPopulation());
					setGeneration(genCount, task.getPopulation());
					individual = output.outputPittsburgh(task.getPopulation());
					fileName = populationDir[i] + sep + "individual.csv";
					Output.write(fileName, individual);
//					individualPopulation.get(i).add(individual);
//					ruleSets = output.outputRuleSet(task.getPopulation());
//					ruleSetsPopulation.get(i).add(ruleSets);

//					// Decision values
//					fileName = ruleSetDir[i] + sep + "gen" + genCount + ".txt";
//					ruleSetTxt = task.getPopulation().toString();
//					Output.write(fileName, ruleSetTxt);

					//Offspring
//					task.nonDominatedSort(task.getOffspring());
//					task.calcAppendix(task.getOffspring());
//					setGeneration(genCount, task.getOffspring());
//					individual = output.outputPittsburgh(task.getOffspring());
//					fileName = offspringDir[i] + sep + "individual.csv";
//					Output.write(fileName, individual);
//					individualOffspring.get(i).add(individual);
//					ruleSets = output.outputRuleSet(task.getOffspring());
//					ruleSetsOffspring.get(i).add(ruleSets);
				}
			}

			timeWatcher.start();
		}

		timeWatcher.stop();
		for(int i = 0; i < world.getTaskNum(); i++) {
			Task task = world.getTask(i);
			// Decision values
			String fileName = ruleSetDir[i] + sep + "gen" + genCount + ".txt";
			String ruleSetTxt = task.getPopulation().toString();
			Output.write(fileName, ruleSetTxt);
		}
		timeWatcher.start();

		/* ********************************************************* */
		/* ********************************************************* */
		// ここまで

		//GA End
		timeWatcher.stop();
		resultMaster.addTimes( timeWatcher.getSec() );
		resultMaster.addEvaTimes( evaWatcher.getSec() );

		// Output
//		this.output2files(trialRoot, genCounts, individualPopulation, ruleSetsPopulation, individualOffspring, ruleSetsOffspring);
	}

	public void setGeneration(int genCount, Population<MultiPittsburgh> individuals) {
		for(int p = 0; p < individuals.getIndividuals().size(); p++) {
			individuals.getIndividual(p).setGeneration(genCount);
		}
	}

	public void makeResultDir(String trialRoot, int taskNum) {
		String sep = File.separator;

		for(int i = 0; i < taskNum; i++) {
			// make Directroy
			String taskDirName = trialRoot + sep + "task"+(i+1);
			String populationDir = taskDirName + sep + Consts.POPULATION;
			Output.mkdirs(populationDir);
//			String offspringDir = taskDirName + sep + Consts.OFFSPRING;
//			Output.mkdirs(offspringDir);
//			Output.makeDir(populationDir, Consts.INDIVIDUAL);
//			Output.makeDir(populationDir, Consts.RULESET);
//			Output.makeDir(offspringDir, Consts.INDIVIDUAL);
//			Output.makeDir(offspringDir, Consts.RULESET);
		}
	}

	public void output2files(String trialRoot, ArrayList<String> genCounts,
			ArrayList<ArrayList<String>> individualPopulation, ArrayList<ArrayList<String>> ruleSetsPopulation,
			ArrayList<ArrayList<String>> individualOffspring, ArrayList<ArrayList<String>> ruleSetsOffspring) {
		String sep = File.separator;
		int taskNum = individualPopulation.size();

		for(int i = 0; i < taskNum; i++) {
			String taskDirName = trialRoot + sep + "task"+(i+1);
			String populationDir = taskDirName + sep + Consts.POPULATION;
			String offspringDir = taskDirName + sep + Consts.OFFSPRING;

			ArrayList<String> individual;
			ArrayList<String> ruleSets;

			String fileName;
			//Population
			individual = individualPopulation.get(i);
			ruleSets = ruleSetsPopulation.get(i);
			for(int j = 0; j < individual.size(); j++) {
//				int genCount = j * Setting.timingOutput;
				String genCount = genCounts.get(j);
				fileName = populationDir + sep + Consts.INDIVIDUAL + sep + "gen" + genCount + ".csv";
				Output.writeln(fileName, individual.get(j));
//				fileName = populationDir + sep + Consts.RULESET + sep + "gen" + genCount + ".csv";
//				Output.writeln(fileName, ruleSets.get(j));
			}

			//Offspring
			individual = individualOffspring.get(i);
			ruleSets = ruleSetsOffspring.get(i);
			for(int j = 0; j < individual.size(); j++) {
//				int genCount = (j+1) * Setting.timingOutput;
				String genCount = genCounts.get(j+1);
				fileName = offspringDir + sep + Consts.INDIVIDUAL + sep + "gen" + genCount + ".csv";
				Output.writeln(fileName, individual.get(j));
//				fileName = offspringDir + sep + Consts.RULESET + sep + "gen" + genCount + ".csv";
//				Output.writeln(fileName, ruleSets.get(j));
			}
		}

	}


	/**
	 * 1: MOP1<br>
	 * 2: MOP2<br>
	 * 3: MOP3<br>
	 *
	 * @param mopNo
	 * @param Dtra
	 * @param Dtst
	 * @return MultiLabelProblem
	 */
	public Problem_MultiLabel getMOP(int mopNo, MultiDataSetInfo Dtra, MultiDataSetInfo Dtst) {
		Problem_MultiLabel mop = null;
		switch(mopNo) {
		case 1:
			// Subset Accuracy
			mop =  new MOP1(Dtra, Dtst);
			break;
		case 2:
			// Hamming Loss
			mop = new MOP2(Dtra, Dtst);
			break;
		case 3:
			// F-Measure
			mop = new MOP3(Dtra, Dtst);
			break;
		}
		return mop;
	}
}









































