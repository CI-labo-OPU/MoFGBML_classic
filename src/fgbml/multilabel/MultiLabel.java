package fgbml.multilabel;

import java.io.File;

import data.Input;
import data.MultiDataSetInfo;
import emo.algorithms.Algorithm;
import emo.algorithms.moead.MOEA_D;
import emo.algorithms.nsga2.NSGA2;
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
 * @version 1.0
 *
 * Multi-Label用のMainメソッド
 *
 * 引数として，MOP番号を受け取る
 *
 * <p>*****************************</p>
 * <h1>ver1.0 - ver2.0</h1>
 * <p>MultiLabelProblemクラスのgetClassified()内で各ルールのfitnessをclear()するように変更</p>
 * <p>各ルールのfitnessをclearする条件として，doMemorizeMissPatternsを追加</p>
 * <p>StaticFuzzyFuncクラスのcalcTrustMulti()メソッド内の分母sumAllが0かどうかを判定するように変更</p>
 * <p></p>
 *
 * <h1>ver2.0 - ver3.0</h1>
 * <p>目的: MOEA/D-AOFの実装</p>>
 * <p>consts.propertiesファイルの変数[VECTOR_DIVIDE_NUM]をカンマ区切りのリストに変更</p>
 * <p>AOFのベクトルの初期化を(maxRule/popSize)刻みのweightに変更</p>
 * <p></p>
 *
 */
public class MultiLabel implements Experiment {

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
		String populationDir = trialRoot + sep + Consts.POPULATION;
		Output.mkdirs(populationDir);
		String offspringDir = trialRoot + sep + Consts.OFFSPRING;
		Output.mkdirs(offspringDir);

		Output.makeDir(populationDir, Consts.INDIVIDUAL);
		Output.makeDir(populationDir, Consts.RULESET);
		Output.makeDir(offspringDir, Consts.INDIVIDUAL);
		Output.makeDir(offspringDir, Consts.RULESET);

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
		//Generate Problem
		Problem_MultiLabel mop = getMOP(mopNo, Dtra, Dtst);

		/* ********************************************************* */
		//Generate Algorithm
		Algorithm<MultiPittsburgh> algorithm;

		/* ********************************************************* */
		//Generate OutputClass
		OutputClass<MultiPittsburgh> output = new Output_MultiLabel();

		/* ********************************************************* */
		//Generate Individual Instance
		MultiPittsburgh instance = new MultiPittsburgh();
		instance.setCnum(((MultiDataSetInfo)mop.getTrain()).getCnum());

		/* ********************************************************* */
		//Timer start
		TimeWatcher timeWatcher = new TimeWatcher();	//All Exprimeint executing time
		TimeWatcher evaWatcher = new TimeWatcher();		//Evaluating time
		timeWatcher.start();

		/* ********************************************************* */
		/* ********************************************************* */
		//GA Start
		if(Setting.emoType == Consts.NSGA2) {
			algorithm = new NSGA2<MultiPittsburgh>();
			algorithm.main( mop, output, instance,
							resultMaster, rnd,
							timeWatcher, evaWatcher);
		}
		else if(Setting.emoType == Consts.WS ||
				Setting.emoType == Consts.TCHEBY ||
				Setting.emoType == Consts.PBI ||
				Setting.emoType == Consts.AOF ||
				Setting.emoType == Consts.AOF2) {
			algorithm = new MOEA_D<MultiPittsburgh>();
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
	 * 1: MOP_ExactMatchError<br>
	 * 2: MOP_Fmeasure<br>
	 * 3: MOP_HammingLoss<br>
	 * 4: ExactMatch, Fmeasure, HammingLoss<br>
	 * 5: ExactMatch, Fmeasure, HammingLoss, ruleNum<br>
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
			mop =  new MOP_ExactMatchError(Dtra, Dtst);
			break;
		case 2:
			mop = new MOP_Fmeasure(Dtra, Dtst);
			break;
		case 3:
			mop = new MOP_HammingLoss(Dtra, Dtst);
			break;
		case 4:
			mop = new MOP_Multi3obj(Dtra, Dtst);
			break;
		case 5:
			mop = new MOP_Multi4obj(Dtra, Dtst);
			break;
		}
		return mop;
	}
}










































