package fgbml.multilabel.binary_relevance;

import java.util.Arrays;

import data.MultiDataSetInfo;
import data.MultiPattern;
import data.SingleDataSetInfo;
import data.SinglePattern;
import fgbml.problem.FGBML;
import ga.Population;
import method.StaticFunction;

public abstract class Problem_BR_Multi extends FGBML<BRpittsburgh>{
	// ************************************************************
	final int traID = 0;
	final int tstID = 1;

	MultiDataSetInfo Dtra;
	MultiDataSetInfo Dtst;

	SingleDataSetInfo[] Dtra_single;
	SingleDataSetInfo[] Dtst_single;

	/** 0:Dtra, 1:Dtst */
	boolean[] doMemorizeMissPatterns = new boolean[] {true, false};

	// ************************************************************
	public Problem_BR_Multi(MultiDataSetInfo Dtra, MultiDataSetInfo Dtst) {
		this.Dtra = Dtra;
		this.Dtst = Dtst;

		int Cnum = Dtra.getCnum();
		this.Dtra_single = new SingleDataSetInfo[Cnum];
		this.Dtst_single = new SingleDataSetInfo[Cnum];
		for(int c = 0; c < Cnum; c++) {
			this.Dtra_single[c] = transformMulti(this.Dtra, c);
			this.Dtst_single[c] = transformMulti(this.Dtst, c);
		}

	}

	// ************************************************************

	/**
	 * MultiDataSetInfoからSingleDataSetInfoへ変換
	 * @param multi
	 * @param label
	 * @return
	 */
	public SingleDataSetInfo transformMulti(MultiDataSetInfo multi, int label) {
		int dataSize = multi.getDataSize();
		int Ndim = multi.getNdim();
		int Cnum = 2;

		SingleDataSetInfo single = new SingleDataSetInfo();
		single.setDataSize(dataSize);
		single.setNdim(Ndim);
		single.setCnum(Cnum);

		for(int p = 0; p < dataSize; p++) {
			double[] line = new double[Ndim + 1];

			for(int n = 0; n < Ndim; n++) {
				line[n] = multi.getPattern(p).getDimValue(n);
			}
			line[Ndim] = multi.getPattern(p).getConClass(label);

			SinglePattern pattern = new SinglePattern(p, line);
			single.addPattern(pattern);
		}

		return single;
	}

	public SingleDataSetInfo getDtra_single(int label) {
		return this.Dtra_single[label];
	}

	public SingleDataSetInfo getSingleDataSet(int dataID, int label) {
		switch(dataID) {
			case traID:
				return Dtra_single[label];
			case tstID:
				return Dtst_single[label];
			default:
				return Dtra_single[label];
		}
	}

	public MultiDataSetInfo getMultiDataSet(int dataID) {
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
	public void setAppendix(BRpittsburgh individual) {
		if(!individual.getEvaluated()) {
			/** 0:Exact-Match(Dtra), 1:F-measure(Dtra), 2:Recall(Dtra), 3:Precision(Dtra), 4:Hamming Loss(Dtra)<br>
			 *  5:Exact-Match(Dtst), 6:F-measure(Dtst), 7:Recall(Dtst), 8:Precision(Dtst), 9:Hamming Loss(Dtst) */
			int appendixNum = 10;

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
			individual.setEvaluated(true);
		}

	}

	@Override
	public void setAppendix(Population<BRpittsburgh> population) {

	}

	public int[][] getClassified(int dataID, BRpittsburgh individual) {
		MultiDataSetInfo multi = getMultiDataSet(dataID);
		int dataSize = multi.getDataSize();
		int Cnum = multi.getCnum();

		int[][] classified = new int[dataSize][Cnum];

		for(int p = 0; p < dataSize; p++) {
			for(int c = 0; c < Cnum; c++) {
				SingleDataSetInfo dataset = getSingleDataSet(dataID, c);
				classified[p][c] = individual.getBRruleset().classify(dataset.getPattern(p), doMemorizeMissPatterns[dataID])[c];
			}
		}

		return classified;
	}

	public int[][] getClassifiedParallel(int dataID, BRpittsburgh individual) {
		MultiDataSetInfo multi = getMultiDataSet(dataID);
		int dataSize = multi.getDataSize();
		int Cnum = multi.getCnum();

		int[][] classified = new int[dataSize][Cnum];

		for(int p = 0; p < dataSize; p++) {
			for(int c = 0; c < Cnum; c++) {
				SingleDataSetInfo dataset = getSingleDataSet(dataID, c);
				classified[p][c] = individual.getBRruleset().classifyParallel(dataset.getPattern(p), doMemorizeMissPatterns[dataID])[c];
			}

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
		MultiDataSetInfo dataset = getMultiDataSet(dataID);

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
		MultiDataSetInfo dataset = getMultiDataSet(dataID);

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
		MultiDataSetInfo dataset = getMultiDataSet(dataID);

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


}
