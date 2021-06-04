package fgbml.multitask;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import data.MultiDataSetInfo;
import data.MultiPattern;
import fgbml.problem.FGBML;
import ga.Population;
import main.Consts;
import main.Setting;
import method.StaticFunction;

public abstract class Problem_MultiLabel extends FGBML<MultiPittsburgh>{

	// ************************************************************
	final int traID = 0;
	final int tstID = 1;

	MultiDataSetInfo Dtra;
	MultiDataSetInfo Dtst;

	/** 0:Dtra, 1:Dtst */
	boolean[] doMemorizeMissPatterns = new boolean[] {true, false};

	// ************************************************************
	public Problem_MultiLabel(MultiDataSetInfo Dtra, MultiDataSetInfo Dtst) {
		appendixNum = 10;

		this.Dtra = Dtra;
		this.Dtst = Dtst;
	}

	// ************************************************************

	public MultiDataSetInfo getDataSet(int dataID) {
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
	 * <h1>Assignment each appendix information for individuals in population</h1>
	 * 	0:Exact-Match(Dtra), 1:F-measure(Dtra), 2:Hamming Loss(Dtra),<br>
	 *  3:Exact-Match(Dtst), 4:F-measure(Dtst), 5:Hamming Loss(Dtst)<br>
	 *	<br>
	 * @param population : {@literal Population<MultiPittsburgh>}
	 */
	@Override
	public void setAppendix(Population<MultiPittsburgh> population) {
		/** 0:Exact-Match(Dtra), 1:F-measure(Dtra), 2:Recall(Dtra), 3:Precision(Dtra), 4:Hamming Loss(Dtra)<br>
		 *  5:Exact-Match(Dtst), 6:F-measure(Dtst), 7:Recall(Dtst), 8:Precision(Dtst), 9:Hamming Loss(Dtst) */
		int appendixNum = 10;

		try {
			Setting.forkJoinPool.submit( () ->
				population.getIndividuals().parallelStream()
				.forEach( individual -> {
					double[] appendix = new double[appendixNum];

					//Dtra
					int[][] classified = getClassified(traID, individual);
					//Exact-Match
					appendix[0] = calcExactMatchError(traID, classified);
					//F
					double[] F = calcFmeasures(traID, classified);
					//F-measure
					appendix[1] = F[0];
					//Recall
					appendix[2] = F[1];
					//Precision
					appendix[3] = F[2];
					//Hamming Loss
					appendix[4] = calcHammingLoss(traID, classified);

					//Dtst
					classified = getClassified(tstID, individual);
					//Exact-Match
					appendix[5] = calcExactMatchError(tstID, classified);
					//F
					F = calcFmeasures(tstID, classified);
					//F-measure
					appendix[6] = F[0];
					//Recall
					appendix[7] = F[1];
					//Precision
					appendix[8] = F[2];
					//Hamming Loss
					appendix[9] = calcHammingLoss(tstID, classified);

					individual.setAppendix(appendix);
				} )
			).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param dataID : int : 0:Dtra, 1:Dtst
	 * @param individual : MultiPittsburgh
	 * @param isEvaluation : boolean
	 * @return int[][] : [dataSize][Lnum]
	 */
	public int[][] getClassified(int dataID, MultiPittsburgh individual){
		MultiDataSetInfo dataset = getDataSet(dataID);
		int dataSize = dataset.getDataSize();
		int Lnum = dataset.getCnum();

		int[][] classified = new int[dataSize][Lnum];

		if(doMemorizeMissPatterns[dataID]) {
			individual.getRuleSet().clearMissPatterns();
			//Crear Fitness of Michigan rule
			for(int rule = 0; rule < individual.getRuleSet().getMicRules().size(); rule++) {
				individual.getRuleSet().getMicRule(rule).clearFitness();
			}
		}

		for(int p = 0; p < dataSize; p++) {
			if(Consts.MULTI_CF_TYPE == 0) {
				classified[p] = individual.getRuleSet().cfMeanClassify(dataset.getPattern(p), doMemorizeMissPatterns[dataID]);
			}
			else if(Consts.MULTI_CF_TYPE == 1) {
				classified[p] = individual.getRuleSet().cfVectorClassify(dataset.getPattern(p), doMemorizeMissPatterns[dataID]);
			}
		}

		if(doMemorizeMissPatterns[dataID]) {
			individual.getRuleSet().removeRuleByFitness();
			individual.getRuleSet().calcRuleLength();
			individual.ruleset2michigan();
			individual.michigan2pittsburgh();
		}

		return classified;
	}

	/**
	 * <h1>for MOEA/D</h1>
	 * @param dataID : int : 0:Dtra, 1:Dtst
	 * @param individual : MultiPittsburgh
	 * @return int[][] : [dataSize][Lnum]
	 */
	public int[][] getClassifiedParallel(int dataID, MultiPittsburgh individual){
		MultiDataSetInfo dataset = getDataSet(dataID);
		int dataSize = dataset.getDataSize();
		int Lnum = dataset.getCnum();

		int[][] classified = new int[dataSize][Lnum];

		if(doMemorizeMissPatterns[dataID]) {
			individual.getRuleSet().clearMissPatterns();
			//Crear Fitness of Michigan rule
			for(int rule = 0; rule < individual.getRuleSet().getMicRules().size(); rule++) {
				individual.getRuleSet().getMicRule(rule).clearFitness();
			}
		}

		for(int p = 0; p < dataSize; p++) {
			if(Consts.MULTI_CF_TYPE == 0) {
				classified[p] = individual.getRuleSet().cfMeanClassifyParallel(dataset.getPattern(p), doMemorizeMissPatterns[dataID]);
			}
			else if(Consts.MULTI_CF_TYPE == 1) {
				classified[p] = individual.getRuleSet().cfVectorClassifyParallel(dataset.getPattern(p), doMemorizeMissPatterns[dataID]);
			}
		}

		if(doMemorizeMissPatterns[dataID]) {
			individual.getRuleSet().removeRuleByFitness();
			individual.getRuleSet().calcRuleLength();
			individual.ruleset2michigan();
			individual.michigan2pittsburgh();
		}

		return classified;
	}

	/**
	 *
	 * @param dataID : int : 0:Dtra, 1:Dtst
	 * @param individual : MultiPittsburgh
	 * @return double : Hamming Loss [%]
	 */
	public double calcHammingLoss(int dataID, int[][] classified) {
		MultiDataSetInfo dataset = getDataSet(dataID);

		double loss = 0.0;
		int Lnum = dataset.getCnum();
		int dataSize = dataset.getDataSize();

		for(int p = 0; p < dataSize; p++) {
			double distance = StaticFunction.HammingDistance(classified[p], dataset.getPattern(p).getConClass());
			loss += distance / (double)Lnum;
		}

		loss = loss/(double)dataSize;
		return loss * 100.0;
	}

	/**
	 *
	 * @param dataID : int : 0:Dtra, 1:Dtst
	 * @param classified : int[][] : [dataSize][Lnum]
	 * @return double[] : 0:F-measure [%], 1:Recall[%], 2:Precision[%]
	 */
	public double[] calcFmeasures(int dataID, int[][] classified) {
		MultiDataSetInfo dataset = getDataSet(dataID);

		int dataSize = dataset.getDataSize();

		double recall = 0.0;
		double precision = 0.0;
		double Fmeasure = 0.0;
		for(int p = 0; p < dataSize; p++) {
			MultiPattern pattern = dataset.getPattern(p);
			precision += StaticFunction.PrecisionMetric(classified[p], pattern.getConClass());
			recall += StaticFunction.RecallMetric(classified[p], pattern.getConClass());
		}

		recall = recall / (double)dataSize;
		precision = precision / (double)dataSize;
		if((precision + recall) == 0) {
			Fmeasure = 0;
		}
		else {
			Fmeasure = (2.0 * recall * precision) / (recall + precision);
		}

		return new double[] {Fmeasure * 100.0, recall * 100.0, precision * 100.0};
	}

	/**
	 *
	 * @param dataID : int : 0:Dtra, 1:Dtst
	 * @param classified : int[][] : [dataSize][Lnum]
	 * @return double : 完全一致 誤識別率[%]
	 */
	public double calcExactMatchError(int dataID, int[][] classified) {
		MultiDataSetInfo dataset = getDataSet(dataID);

		double exactMatchNum = 0;	//if classification is exact-match, then this count increments.
		double errorRate = 0.0;

		int dataSize = dataset.getDataSize();
		for(int p = 0; p < dataSize; p++) {
			MultiPattern pattern = dataset.getPattern(p);
			if(Arrays.equals(classified[p], pattern.getConClass())) {
				exactMatchNum++;
			}
		}

		//range [0, 1]
		errorRate = ((double)dataSize - exactMatchNum) / (double)dataSize;

		return errorRate * 100.0;
	}

	/**
	 *
	 * @param dataID : int : 0:Dtra, 1:Dtst
	 * @param classified : int[][] : [dataSize][Lnum]
	 * @return double : 完全一致 識別率[%]
	 */
	public double calcSubsetAccuracy(int dataID, int[][] classified) {
		MultiDataSetInfo dataset = getDataSet(dataID);

		double exactMatchNum = 0;	//if classification is exact-match, then this count increments.
		double errorRate = 0.0;

		int dataSize = dataset.getDataSize();
		for(int p = 0; p < dataSize; p++) {
			MultiPattern pattern = dataset.getPattern(p);
			if(Arrays.equals(classified[p], pattern.getConClass())) {
				exactMatchNum++;
			}
		}

		//range [0, 1]
		errorRate = (double)exactMatchNum / (double)dataSize;

		return errorRate * 100.0;

	}


	/**
	 *
	 * @param dataID : int : 0:Dtra, 1:Dtst
	 * @param individual : MultiPittsburgh
	 * @return double : Hamming Loss
	 */
	@Deprecated
	public double calcHammingLoss(int dataID, MultiPittsburgh individual) {
		MultiDataSetInfo dataset = getDataSet(dataID);

		double loss = 0.0;
		final int Lnum = dataset.getCnum();
		int dataSize = dataset.getDataSize();

		if(doMemorizeMissPatterns[dataID]) {
			individual.getRuleSet().clearMissPatterns();
			//Crear Fitness of Michigan rule
			for(int rule = 0; rule < individual.getRuleSet().getMicRules().size(); rule++) {
				individual.getRuleSet().getMicRule(rule).clearFitness();
			}
		}

		Optional<Double> partSum =
			dataset.getPatterns().stream()
				.map(p -> StaticFunction.HammingDistance(individual.getRuleSet().classify(p, doMemorizeMissPatterns[dataID]),
														 p.getConClass()) )
				.map(distance -> distance/(double)Lnum )
				.reduce( (acc, l) -> acc+l );

		loss = partSum.orElse(0.0);
		loss = loss/(double)dataSize;
		return loss * 100.0;
	}

	/**
	 *
	 * @param dataID : int : 0:Dtra, 1:Dtst
	 * @param individual : MultiPittsburgh
	 * @return double : F-measure
	 */
	@Deprecated
	public double[] calcFmeasures(int dataID, MultiPittsburgh individual) {
		MultiDataSetInfo dataset = getDataSet(dataID);

		int dataSize = dataset.getDataSize();

		int[][] classified = getClassified(dataID, individual);

		double recall = 0.0;
		double precision = 0.0;
		double Fmeasure = 0.0;
		for(int p = 0; p < dataSize; p++) {
			MultiPattern pattern = dataset.getPattern(p);
			precision += StaticFunction.PrecisionMetric(classified[p], pattern.getConClass());
			recall += StaticFunction.RecallMetric(classified[p], pattern.getConClass());
		}

		recall = recall / (double)dataSize;
		precision = precision / (double)dataSize;
		if((precision + recall) == 0) {
			Fmeasure = 0;
		}
		else {
			Fmeasure = (2.0 * recall * precision) / (recall + precision);
		}

		return new double[] {Fmeasure * 100.0, recall, precision};
	}

	/**
	 *
	 * @param dataID : int : 0:Dtra, 1:Dtst
	 * @param individual : MultiPittsburgh
	 * @return double : 完全一致 誤識別率[%]
	 */
	@Deprecated
	public double calcExactMatchError(int dataID, MultiPittsburgh individual) {
		MultiDataSetInfo dataset = getDataSet(dataID);

		double exactMatchNum = 0;	//if classification is exact-match, then this count increments.
		double errorRate = 0.0;

		if(doMemorizeMissPatterns[dataID]) {
			individual.getRuleSet().clearMissPatterns();
			//Crear Fitness of Michigan rule
			for(int rule = 0; rule < individual.getRuleSet().getMicRules().size(); rule++) {
				individual.getRuleSet().getMicRule(rule).clearFitness();
			}
		}

		int dataSize = dataset.getDataSize();
		for(int p = 0; p < dataSize; p++) {
			MultiPattern pattern = dataset.getPattern(p);
			int[] answerClass = individual.getRuleSet().classify(pattern, doMemorizeMissPatterns[dataID]);
			if(Arrays.equals(answerClass, pattern.getConClass())) {
				exactMatchNum++;
			}
		}

		//range [0, 1]
		errorRate = ((double)dataSize - exactMatchNum) / (double)dataSize;

		return errorRate * 100.0;
	}
}
