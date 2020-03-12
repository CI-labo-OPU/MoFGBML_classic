package fgbml.mofgbml;

import java.util.concurrent.ExecutionException;

import data.SingleDataSetInfo;
import fgbml.SinglePittsburgh;
import fgbml.problem.FGBML;
import ga.Population;
import main.Setting;

public abstract class Problem_MoFGBML extends FGBML<SinglePittsburgh>{
	// ************************************************************
	final int traID = 0;
	final int tstID = 1;

	SingleDataSetInfo Dtra;
	SingleDataSetInfo Dtst;

	/** 0:Dtra, 1:Dtst */
	boolean[] doMemorizeMissPatterns;

	// ************************************************************
	public Problem_MoFGBML(SingleDataSetInfo Dtra, SingleDataSetInfo Dtst) {
		this.Dtra = Dtra;
		this.Dtst = Dtst;

		/** 0:Dtra, 1:Dtst */
		this.appendixNum = 2;
	}

	// ************************************************************

	/**
	 * <h1>dataIDに応じてデータセットを返す</h1>
	 * @param dataID : int : (0:Dtra, 1:Dtst)
	 * @return SingleDataSetInfo
	 */
	public SingleDataSetInfo getDataSet(int dataID) {
		switch(dataID) {
			case traID:
				return Dtra;
			case tstID:
				return Dtst;
			default:
				return Dtra;
		}
	}

	/**
	 * <h1>各個体の詳細情報を確認するための付加情報を獲得するメソッド</h1>
	 * <p>0:Dtra, 1:Dtst</p>
	 * @param population : {@literal Population<Pittsburgh>}
	 */
	public void setAppendix(Population<SinglePittsburgh> population) {
		try {
			Setting.forkJoinPool.submit( () ->
				population.getIndividuals().parallelStream()
				.forEach( individual -> {
					double[] appendix = new double[appendixNum];
					//Dtra
					appendix[0] = getMissRate(0, individual);
					//Dtst
					appendix[1] = getMissRate(1, individual);

					individual.setAppendix(appendix);
					}
				)
			).get();

		}
		catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <h1>dataIDに応じて誤識別率を計算する</h1>
	 * <p>doMemorizeMissPatternsに応じて誤識別パターンの保持を行う</p>
	 * <p>doMemorizeMissPatternsに応じてルールのfitnessが更新されるため，
	 * 勝者とならないルールを削除する</p>
	 * @param dataID : int : (0:Dtra, 1:Dtst)
	 * @param individual : SinglePittsburgh :
	 * @return double : Error Rate [%]
	 */
	public double getMissRate(int dataID, SinglePittsburgh individual) {
		SingleDataSetInfo dataset = getDataSet(dataID);
		double missRate = individual.getRuleSet().calcMissRate(dataset, doMemorizeMissPatterns[dataID]);
		if(doMemorizeMissPatterns[dataID]) {
			individual.getRuleSet().removeRuleByFitness();
			individual.getRuleSet().calcRuleLength();
			individual.ruleset2michigan();
			individual.michigan2pittsburgh();
		}
		return missRate;
	}

	/**
	 * <h1>for MOEA/D</h1>
	 * @param dataID : int : 0:Dtra, 1:Dtst
	 * @param individual : SinglePittsburgh
	 * @return double : Error Rate [%]
	 */
	public double getMissRateParallel(int dataID, SinglePittsburgh individual) {
		SingleDataSetInfo dataset = getDataSet(dataID);
		double missRate = individual.getRuleSet().calcMissRateParallel(dataset, doMemorizeMissPatterns[dataID]);
		if(doMemorizeMissPatterns[dataID]) {
			individual.getRuleSet().removeRuleByFitness();
			individual.getRuleSet().calcRuleLength();
			individual.ruleset2michigan();
			individual.michigan2pittsburgh();
		}
		return missRate;
	}




}
