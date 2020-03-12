package fgbml.multilabel.label_power_set;

import java.util.Arrays;

import data.MultiDataSetInfo;
import data.MultiPattern;
import data.SinglePattern;
import fgbml.SinglePittsburgh;
import fgbml.problem.FGBML;
import method.StaticFunction;

public abstract class Problem_LPS_multi extends FGBML<SinglePittsburgh> {
	// ************************************************************
	final int traID = 0;
	final int tstID = 1;

	MultiDataSetInfo Dtra;
	MultiDataSetInfo Dtst;

	/** 0:Dtra, 1:Dtst */
	boolean[] doMemorizeMissPatterns = new boolean[] {true, false};

	// ************************************************************
	public Problem_LPS_multi(MultiDataSetInfo Dtra, MultiDataSetInfo Dtst) {
		this.Dtra = Dtra;
		this.Dtst = Dtst;
	}

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
	 */
	public void setAppendix(SinglePittsburgh individual) {
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

	public int[][] getClassified(int dataID, SinglePittsburgh individual) {
		MultiDataSetInfo dataset = getDataSet(dataID);
		int dataSize = dataset.getDataSize();
		int Cnum = dataset.getCnum();

		int[][] classified = new int[dataSize][Cnum];

		for(int p = 0; p < dataSize; p++) {
			SinglePattern pattern = transformM2S(dataset.getPattern(p));
			int conClass = individual.getRuleSet().classify(pattern, doMemorizeMissPatterns[dataID])[0];
			classified[p] = transformS2M(conClass, Cnum);
		}

		return classified;
	}

	public int[][] getClassifiedParallel(int dataID, SinglePittsburgh individual) {
		MultiDataSetInfo dataset = getDataSet(dataID);
		int dataSize = dataset.getDataSize();
		int Cnum = dataset.getCnum();

		int[][] classified = new int[dataSize][Cnum];

		for(int p = 0; p < dataSize; p++) {
			SinglePattern pattern = transformM2S(dataset.getPattern(p));
			int conClass = individual.getRuleSet().classifyParallel(pattern, doMemorizeMissPatterns[dataID])[0];
			classified[p] = transformS2M(conClass, Cnum);
		}

		return classified;
	}

	public SinglePattern transformM2S(MultiPattern multi) {
		int Ndim = multi.getNdim();
		int Cnum = multi.getConClass().length;

		double[] x = new double[Ndim + 1];
		for(int n = 0; n < Ndim; n++) {
			x[n] = multi.getDimValue(n);
		}
		int conClass = 0;
		for(int c = 0; c < Cnum; c++) {
			int bit = 1;
			for(int cc = 0; cc < c; cc++) {
				bit *= 2;
			}
			conClass += bit * multi.getConClass(c);
		}
		x[Ndim] = conClass;

		SinglePattern pattern = new SinglePattern(multi.getID(), x);
		return pattern;
	}

	public int[] transformS2M(int conClass, int Cnum) {
		int[] labels = new int[Cnum];
		Arrays.fill(labels, 0);

		for(int c = 0; c < Cnum; c++) {
			int quotient = conClass / 2;
			int remainder = conClass % 2;

			labels[(Cnum-1) - c] = remainder;
			conClass = quotient;
			if(quotient == 0) {
				break;
			}
		}

		return labels;
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


}





























