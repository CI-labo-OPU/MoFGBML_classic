package fgbml.subdivision;

import java.io.File;

import data.Divider;
import data.Input;
import data.SingleDataSetInfo;
import emo.algorithms.Algorithm;
import emo.algorithms.moead.MOEA_D;
import emo.algorithms.nsga2.NSGA2;
import fgbml.SinglePittsburgh;
import fgbml.problem.OutputClass;
import fuzzy.StaticFuzzyFunc;
import main.Consts;
import main.Experiment;
import main.Setting;
import method.MersenneTwisterFast;
import method.Output;
import method.ResultMaster;
import time.TimeWatcher;

/**
 * @version 2.0
 *
 * Subdivision用のMainメソッド
 *
 * 引数として，mopNoを受け取る．
 * Mainクラスの引数の後ろに
 * int mopNo
 * を受け取り，使用するMOPを選択できる．
 * <p>*****************************</p>
 *
 * <h1>ver1.0 - ver2.0</h1>
 * <p>appendixを毎世代計算していたのを，outputのタイミングのみ計算するように変更．</p>
 * <p>個体評価のタイミングを個体群単位に変更．また，並列実装による個体群評価に変更．</p>
 * <p>各結果ディレクトリの生成タイミングの変更．</p>
 * <p>MOP, Algorithmのnewタイミング変更．</p>
 * <p>個体群出力のタイミングを各試行の終わりに変更．
 * 試行の終わりまで，ResultMasterクラスで情報を保持する．</p>
 * <p>個体群情報(Individual出力)として，ruleNum, ruleLengthを出力するように変更．</p>
 * <p>RuleSet出力として，各ルールのFitnessを出力するように変更．</p>
 * <p>初期個体群の評価を1世代とカウントするように変更．</p>
 * <p>分割データ(subtra, valid)の出力先ディレクトリの配置変更．</p>
 * <p>実行中のドット表示の調整．</p>
 * <p>Dsubtra, Dvalidの出力内容を，IDのみに変更</p>
 * <p>目的関数の最大化/最小化を決定する定数を各MOPではなく，SubdivisionMOP.javaで定義済に変更</p>
 * <p>Appendixを計算する際の並列化を，「パターンごと」→「個体ごと」に変更．</p>
 * <p>NSGA2クラスがAlgorithmを継承するように変更</p>
 * <p>IndividualとRuleSetの出力メソッドをResultMasterクラスに移行</p>
 * <p>gaFrame()の引数にデータセットを渡さないように変更</p>
 * <p>EvaluationメソッドをPopulationManagerクラスへ移行</p>
 * <p>terminationJudgeメソッドをStaticFunctionクラスへ移行</p>
 * <p>Michiganルールのfitnessのaddの方法を，識別結果が正しい場合のみに変更</p>
 * <p>SingleRuleSetクラスのcalcMissRate()内で各ルールのfitnessをclear()するように変更</p>
 * <p>evaluate()後に，fitnessが0になるルールを削除するメソッドremoveRuleByFitness()を追加</p>
 * <p></p>
 *
 */
public class Subdivision implements Experiment {

	public void startExperiment( String[] args, String traFile, String tstFile,
										MersenneTwisterFast rnd, ResultMaster resultMaster) {
		/* ********************************************************* */
		//START:

		/* ********************************************************* */
		//MOP No.
		int mopNo;
		if(args.length < 7) {
			mopNo = 1;	//MOP1
		} else {
			mopNo = Integer.parseInt(args[6]);
		}

		/* ********************************************************* */
		//Sub rate
		if(args.length < 8) {}
		else {
			Consts.SUBRATE = Double.parseDouble(args[7]);
		}

		/* ********************************************************* */
		//Load Dataset
		SingleDataSetInfo Dtra = new SingleDataSetInfo();
		SingleDataSetInfo Dtst = new SingleDataSetInfo();
		Input.inputFile(Dtra, traFile);
		Input.inputFile(Dtst, tstFile);

		//Dividing Dataset
		SingleDataSetInfo[] trainDataInfos = null;
		String now = String.valueOf(resultMaster.getNowRep()) + String.valueOf(resultMaster.getNowCV());
		//Load divided sub-datasets.
		if(Consts.LOAD_SUBDATASET) {
			String sep = File.separator;
			String subtraFile = "dataset" + sep + Setting.dataName + sep + "subdata" + sep + "a" + now + "_subtra.csv";
			String validFile = "dataset" + sep + Setting.dataName + sep + "subdata" + sep + "a" + now + "_valid.csv";
			trainDataInfos = new SingleDataSetInfo[2];

			//Dsubtra
			trainDataInfos[0] = new SingleDataSetInfo();
			Input.inputSubdata(Dtra, trainDataInfos[0], subtraFile);

			//Dvalid
			trainDataInfos[1] = new SingleDataSetInfo();
			Input.inputSubdata(Dtra, trainDataInfos[1], validFile);
		}
		//Devide dataset now.
		else {
			trainDataInfos = Divider.divideSubAndValid(Dtra, Setting.calclationType, Setting.serverList);
		}
		SingleDataSetInfo Dsubtra = trainDataInfos[0];
		SingleDataSetInfo Dvalid = trainDataInfos[1];

		/* ********************************************************* */
		//Make result directry
		String sep = File.separator;
		String resultRoot = resultMaster.getRootDir();
		Output.makeDir(resultRoot, Consts.SUBDATA);
		String subdataDir = resultMaster.getRootDir() + sep + Consts.SUBDATA;

		String trialRoot = resultMaster.getTrialRoot();
		Output.makeDir(trialRoot, Consts.POPULATION);
		Output.makeDir(trialRoot, Consts.OFFSPRING);

		String populationDir = resultMaster.getTrialRoot() + sep + Consts.POPULATION;
		String offspringDir = resultMaster.getTrialRoot() + sep + Consts.OFFSPRING;
		Output.makeDir(populationDir, Consts.INDIVIDUAL);
		Output.makeDir(populationDir, Consts.RULESET);
		Output.makeDir(offspringDir, Consts.INDIVIDUAL);
		Output.makeDir(offspringDir, Consts.RULESET);

		/* ********************************************************* */
		//Initialize Fuzzy Sets
		StaticFuzzyFunc.initFuzzy(Dtra);

		/* ********************************************************* */
		//Generate Problem
		Problem_Subdivision mop = getMOP(mopNo, Dtra, Dtst, Dsubtra, Dvalid);
		mop.outputDividedData(subdataDir, resultMaster.getNowCV(), resultMaster.getNowRep());

		/* ********************************************************* */
		//Generate Algorithm
		Algorithm<SinglePittsburgh> algorithm;

		/* ********************************************************* */
		//Generate OutputClass
		OutputClass<SinglePittsburgh> output = new Output_subdivision();

		/* ********************************************************* */
		//Generate Individual Instance
		SinglePittsburgh instance = new SinglePittsburgh();

		/* ********************************************************* */
		//Timer start
		TimeWatcher timeWatcher = new TimeWatcher();	//All Exprimeint executing time
		TimeWatcher evaWatcher = new TimeWatcher();		//Evaluating time
		timeWatcher.start();

		/* ********************************************************* */
		/* ********************************************************* */
		//GA Start
		if(Setting.emoType == Consts.NSGA2) {
			algorithm = new NSGA2<SinglePittsburgh>();
			algorithm.main( mop, output, instance,
							resultMaster, rnd,
							timeWatcher, evaWatcher);
		}
		else if(Setting.emoType == Consts.WS ||
				Setting.emoType == Consts.TCHEBY ||
				Setting.emoType == Consts.PBI ||
				Setting.emoType == Consts.AOF) {
			algorithm = new MOEA_D<SinglePittsburgh>();
			algorithm.main( mop, output, instance,
							resultMaster, rnd,
							timeWatcher, evaWatcher);
		}
		/* ********************************************************* */
		/* ********************************************************* */

		//GA End
		timeWatcher.stop();
		resultMaster.addTimes( timeWatcher.getSec() );
		resultMaster.addEvaTimes( evaWatcher.getSec() );

		//Output One Trial Information
		resultMaster.outputIndividual(populationDir, offspringDir);
		resultMaster.population.clear();
		resultMaster.ruleSetPopulation.clear();
		resultMaster.offspring.clear();
		resultMaster.ruleSetOffspring.clear();

		System.out.println();
	}

	/**
	 * Get MOPx Class<br>
	 * @param mopNo : int : MOPx
	 * @param Dtra
	 * @param Dtst
	 * @param Dsubtra
	 * @param Dvalid
	 * @return
	 */
	public Problem_Subdivision getMOP(int mopNo, SingleDataSetInfo Dtra, SingleDataSetInfo Dtst, SingleDataSetInfo Dsubtra, SingleDataSetInfo Dvalid) {
		Problem_Subdivision mop = null;
		switch(mopNo) {
		case 1:
			mop = new SubMOP1(Dtra, Dtst, Dsubtra, Dvalid);
			break;
		case 2:
			mop = new SubMOP2(Dtra, Dtst, Dsubtra, Dvalid);
			break;
		case 3:
			mop = new SubMOP3(Dtra, Dtst, Dsubtra, Dvalid);
			break;
		case 4:
			mop = new SubMOP4(Dtra, Dtst, Dsubtra, Dvalid);
			break;
		case 5:
			mop = new SubMOP5(Dtra, Dtst, Dsubtra, Dvalid);
			break;
		case 6:
			mop = new SubMOP6(Dtra, Dtst, Dsubtra, Dvalid);
			break;
		case 7:
			mop = new SubMOP7(Dtra, Dtst, Dsubtra, Dvalid);
			break;
		case 8:
			mop = new SubMOP8(Dtra, Dtst, Dsubtra, Dvalid);
			break;
		case 9:
			mop = new SubMOP9(Dtra, Dtst, Dsubtra, Dvalid);
			break;
		case 10:
			mop = new SubMOP10(Dtra, Dtst, Dsubtra, Dvalid);
			break;
		case 11:
			mop = new SubMOP11(Dtra, Dtst, Dsubtra, Dvalid);
			break;
		case 12:
			mop = new SubMOP12(Dtra, Dtst, Dsubtra, Dvalid);
			break;
		case 13:
			mop = new SubMOP13(Dtra, Dtst, Dsubtra, Dvalid);
			break;
		}

		return mop;
	}





}


























