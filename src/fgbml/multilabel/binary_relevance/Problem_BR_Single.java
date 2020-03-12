package fgbml.multilabel.binary_relevance;

import java.util.concurrent.ExecutionException;

import data.SingleDataSetInfo;
import fgbml.SinglePittsburgh;
import fgbml.problem.FGBML;
import ga.Population;
import main.Setting;

public abstract class Problem_BR_Single extends FGBML<SinglePittsburgh>{
	// ************************************************************
	final int traID = 0;

	SingleDataSetInfo Dtra;

	/** 0:Dtra */
	boolean[] doMemorizeMissPatterns = new boolean[] {true};


	// ************************************************************
	public Problem_BR_Single(SingleDataSetInfo Dtra) {
		appendixNum = 1;
		this.Dtra = Dtra;
	}

	// ************************************************************
	public SingleDataSetInfo getDataSet(int dataID) {
		switch(dataID) {
		case traID:
			return Dtra;
		default:
			return Dtra;
		}
	}

	@Override
	public void setAppendix(Population<SinglePittsburgh> population) {
		/** 0:Error Rate (Dtra) */
		int appendixNum = 1;

		try {
			Setting.forkJoinPool.submit( () ->
				population.getIndividuals().parallelStream()
				.forEach( individual -> {
					double[] missRates = new double[appendixNum];

					//Dtra
					missRates[0] = individual.getRuleSet().calcMissRate(Dtra, false);

					individual.setAppendix(missRates);

				} )
			).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

	}

	public double calcMissRate(int dataID, SinglePittsburgh individual) {
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

	public double calcMissRateParallel(int dataID, SinglePittsburgh individual) {
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
