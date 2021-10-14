package fgbml.subdivision;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import data.SingleDataSetInfo;
import fgbml.SinglePittsburgh;
import fgbml.problem.FGBML;
import ga.Population;
import main.Setting;
import method.Output;

public abstract class Problem_Subdivision extends FGBML<SinglePittsburgh>{
	// ************************************************************

	int traID = 0;
	int tstID = 1;
	int subtraID = 2;
	int validID = 3;

	SingleDataSetInfo Dtra;
	SingleDataSetInfo Dtst;

	SingleDataSetInfo Dsubtra;
	SingleDataSetInfo Dvalid;

	/** 0:Dtra, 1:Dtst, 2:Dsubtra, 3:Dvalid */
	boolean[] doMemorizeMissPatterns;

	// ************************************************************
	public Problem_Subdivision(SingleDataSetInfo Dtra, SingleDataSetInfo Dtst, SingleDataSetInfo Dsubtra, SingleDataSetInfo Dvalid) {
		this.Dtra = Dtra;
		this.Dtst = Dtst;

		this.Dsubtra = Dsubtra;
		this.Dvalid = Dvalid;

		this.appendixNum = 4;
	}

	// ************************************************************

	/**
	 * Dtraに対する誤識別率を計算<br>
	 * @param individual
	 * @return double : 誤識別率[%]
	 */
	public double traMissRate(SinglePittsburgh individual) {
		double missRate = individual.getRuleSet().calcMissRate(Dtra, doMemorizeMissPatterns[0]);
		if(doMemorizeMissPatterns[0]) {
			individual.getRuleSet().removeRuleByFitness();
			individual.getRuleSet().calcRuleLength();
			individual.ruleset2michigan();
			individual.michigan2pittsburgh();
		}
		return missRate;
	}

	/**
	 * Dtraに対する誤識別率を計算<br>
	 * @param individual
	 * @return double : 誤識別率[%]
	 */
	public double traMissRateParallel(SinglePittsburgh individual) {
		double missRate = individual.getRuleSet().calcMissRateParallel(Dtra, doMemorizeMissPatterns[0]);
		if(doMemorizeMissPatterns[0]) {
			individual.getRuleSet().removeRuleByNwin();
//			individual.getRuleSet().removeRuleByFitness();
			individual.getRuleSet().calcRuleLength();
			individual.ruleset2michigan();
			individual.michigan2pittsburgh();
		}
		return missRate;
	}

	/**
	 * Dtstに対する誤識別率を計算<br>
	 * @param individual
	 * @return double : 誤識別率[%]
	 */
	public double tstMissRate(SinglePittsburgh individual) {
		double missRate = individual.getRuleSet().calcMissRate(Dtst, doMemorizeMissPatterns[1]);
		if(doMemorizeMissPatterns[1]) {
			individual.getRuleSet().removeRuleByNwin();
//			individual.getRuleSet().removeRuleByFitness();
			individual.getRuleSet().calcRuleLength();
			individual.ruleset2michigan();
			individual.michigan2pittsburgh();
		}
		return missRate;
	}

	/**
	 * Dtstに対する誤識別率を計算<br>
	 * @param individual
	 * @return double : 誤識別率[%]
	 */
	public double tstMissRateParallel(SinglePittsburgh individual) {
		double missRate = individual.getRuleSet().calcMissRateParallel(Dtst, doMemorizeMissPatterns[1]);
		if(doMemorizeMissPatterns[1]) {
			individual.getRuleSet().removeRuleByNwin();
//			individual.getRuleSet().removeRuleByFitness();
			individual.getRuleSet().calcRuleLength();
			individual.ruleset2michigan();
			individual.michigan2pittsburgh();
		}
		return missRate;
	}


	/**
	 * Dsubtraに対する誤識別率を計算<br>
	 * @param individual
	 * @return double : 誤識別率[%]
	 */
	public double subtraMissRate(SinglePittsburgh individual) {
		double missRate = individual.getRuleSet().calcMissRate(Dsubtra, doMemorizeMissPatterns[2]);
		if(doMemorizeMissPatterns[2]) {
			individual.getRuleSet().removeRuleByNwin();
//			individual.getRuleSet().removeRuleByFitness();
			individual.getRuleSet().calcRuleLength();
			individual.ruleset2michigan();
			individual.michigan2pittsburgh();
		}
		return missRate;
	}

	/**
	 * Dsubtraに対する誤識別率を計算<br>
	 * @param individual
	 * @return double : 誤識別率[%]
	 */
	public double subtraMissRateParallel(SinglePittsburgh individual) {
		double missRate = individual.getRuleSet().calcMissRateParallel(Dsubtra, doMemorizeMissPatterns[2]);
		if(doMemorizeMissPatterns[2]) {
			individual.getRuleSet().removeRuleByNwin();
//			individual.getRuleSet().removeRuleByFitness();
			individual.getRuleSet().calcRuleLength();
			individual.ruleset2michigan();
			individual.michigan2pittsburgh();
		}
		return missRate;
	}

	/**
	 * Dvalidに対する誤識別率を計算<br>
	 * @param individual
	 * @return double : 誤識別率[%]
	 */
	public double validMissRate(SinglePittsburgh individual) {
		double missRate = individual.getRuleSet().calcMissRate(Dvalid, doMemorizeMissPatterns[3]);
		if(doMemorizeMissPatterns[3]) {
			individual.getRuleSet().removeRuleByNwin();
//			individual.getRuleSet().removeRuleByFitness();
			individual.getRuleSet().calcRuleLength();
			individual.ruleset2michigan();
			individual.michigan2pittsburgh();
		}
		return missRate;
	}

	/**
	 * Dvalidに対する誤識別率を計算<br>
	 * @param individual
	 * @return double : 誤識別率[%]
	 */
	public double validMissRateParallel(SinglePittsburgh individual) {
		double missRate = individual.getRuleSet().calcMissRateParallel(Dvalid, doMemorizeMissPatterns[3]);
		if(doMemorizeMissPatterns[3]) {
			individual.getRuleSet().removeRuleByNwin();
//			individual.getRuleSet().removeRuleByFitness();
			individual.getRuleSet().calcRuleLength();
			individual.ruleset2michigan();
			individual.michigan2pittsburgh();
		}
		return missRate;
	}

	/**
	 * Subdivisionデータを使用する際には，このメソッドを呼び出すこと．
	 * @param individual
	 */
	public void subValidEvaluate(SinglePittsburgh individual) {
		double subtraMissRate = subtraMissRate(individual);
		double validMissRate = validMissRate(individual);
		individual.setAppendix(subtraID, subtraMissRate);
		individual.setAppendix(validID, validMissRate);
	}

	/**
	 * Subdivisionデータを使用する際には，このメソッドを呼び出すこと．
	 * @param individual
	 */
	public void subValidEvaluateParallel(SinglePittsburgh individual) {
		double subtraMissRate = subtraMissRateParallel(individual);
		double validMissRate = validMissRateParallel(individual);
		individual.setAppendix(subtraID, subtraMissRate);
		individual.setAppendix(validID, validMissRate);
	}

	/**
	 * 各個体の詳細情報を確認するための付加情報を獲得するメソッド<br>
	 * @param population : {@literal Population<Pittsburgh>}
	 */
	public void setAppendix(Population<SinglePittsburgh> population) {

		/** 0:Dtra, 1:Dtst, 2:Dsubtra, 3:Dvalid */
		int appendixNum = 4;

		try {
			Setting.forkJoinPool.submit( () ->
				population.getIndividuals().parallelStream()
				.forEach( individual -> {
					double[] missRates = new double[appendixNum];

					//Dtra
					missRates[0] = individual.getRuleSet().calcMissRate(Dtra, false);
					//Dtst
					missRates[1] = individual.getRuleSet().calcMissRate(Dtst, false);
					//Dsubtra
					missRates[2] = individual.getRuleSet().calcMissRate(Dsubtra, false);
					//Dvalid
					missRates[3] = individual.getRuleSet().calcMissRate(Dvalid, false);

					individual.setAppendix(missRates);

				} )
			).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Output Divided Sub Datasets
	 * @param fileName : String[] : [0]:Dsubtra, [1]:Dvalid
	 */
	public void outputDividedData(String path, int cv, int rep) {
		String sep = File.separator;
		ArrayList<String> strs = new ArrayList<String>();
		String str;

		String subtraFile = path + sep + "a" + rep + cv + "_subtra.csv";
		String validFile = path + sep + "a" + rep + cv + "_valid.csv";

		String[] mopClass = this.getClass().getCanonicalName().split("\\.");
		String mopName = mopClass[mopClass.length - 1];
		String mopFile = path + sep + mopName + ".txt";
		Output.writeln(mopFile, strs);

		int DataSize;
		int Ndim;
		int Cnum;

		/* ********************************************************* */
		//Dsubtra
		strs.clear();
		str = "";

		DataSize = Dsubtra.getDataSize();
		Ndim = Dsubtra.getNdim();
		Cnum = Dsubtra.getCnum();
		str += DataSize + "," + Ndim + "," + Cnum;
		strs.add(str);

		for(int p = 0; p < DataSize; p++) {
			str = Dsubtra.getPattern(p).getID() + ",";
			strs.add(str);
		}
		Output.writeln(subtraFile, strs);

		/* ********************************************************* */
		//Dvalid
		strs.clear();
		str = "";

		DataSize = Dvalid.getDataSize();
		Ndim = Dvalid.getNdim();
		Cnum = Dvalid.getCnum();
		str += DataSize + "," + Ndim + "," + Cnum;
		strs.add(str);

		for(int p = 0; p < DataSize; p++) {
			str = Dvalid.getPattern(p).getID() + ",";
			strs.add(str);
		}
		Output.writeln(validFile, strs);

	}

}


















