package fuzzy.multi_label;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import data.DataSetInfo;
import data.MultiDataSetInfo;
import data.MultiPattern;
import data.Pattern;
import fuzzy.RuleSet;
import main.Consts;
import main.Setting;

public class MultiRuleSet extends RuleSet<MultiRule>{
	// ************************************************************

	// ************************************************************
	public MultiRuleSet() {}

	public MultiRuleSet(MultiRuleSet ruleSet) {
		deepCopy(ruleSet);
	}
	// ************************************************************

	@Override
	public Object newInstance() {
		MultiRuleSet instance = new MultiRuleSet();
		return instance;
	}

	public Object newInstance(MultiRuleSet ruleSet) {
		MultiRuleSet instance = new MultiRuleSet(ruleSet);
		return instance;
	}

	@Override
	public void deepCopySpecific(Object ruleSet) {}

	//TODO
	//micRulesの結論部クラスに存在するクラスを保持
	public int[] calcHaveClass() {
		int[] haveCla = new int[1];
		return haveCla;
	}

	//TODO
	//micRulesの結論部クラスに存在しないクラスを保持
	// haveClaの補集合
	public void calcNoClass() {

	}

	/**
	 * Classification pattern<br>
	 * Note: 並列計算を行う(For MOEA/D)<br>
	 * Note: {@literal ArrayList<T extends Rule>}について並列化する．<br>
	 * Note: 誤識別パターンの保持は行うかどうかはdoMemorize引数に任せる<br>
	 * @param line : Multi : Input pattern
	 * @param doMemorizeMissPatterns : boolean : true:memorize miss patterns, false:don't
	 * @return int[] : Classified Class
	 */
	public int[] classifyParallel(Pattern pattern, boolean doMemorizeMissPatterns) {
		int[] answerClass = null;

		if(Consts.MULTI_CF_TYPE == 0) {
			answerClass = cfMeanClassifyParallel(pattern, doMemorizeMissPatterns);
		}
		else if(Consts.MULTI_CF_TYPE == 1) {
			answerClass = cfVectorClassifyParallel(pattern, doMemorizeMissPatterns);
		}


		return answerClass;
	}

	public int[] cfVectorClassifyParallel(Pattern pattern, boolean doMemorizeMissPatterns) {
		MultiPattern line = (MultiPattern)pattern;
		int[] answerClass = new int[line.getLnum()];

		MultiRule[] winners = new MultiRule[line.getLnum()];
		boolean[] canClassify = new boolean[line.getLnum()];

		for(int l = 0; l < line.getLnum(); l++) {
			final int LABEL = l;
			List<Object[]> sorted = null;
			try {
				sorted = Setting.forkJoinPool.submit( () ->
					micRules.parallelStream()
					.map(rule -> {
						Object[] tuple = new Object[2];
						tuple[0] = rule.calcAdaptationPure(line) * rule.getCFVector(LABEL);
						tuple[1] = rule;
						return tuple;
					})
					.sorted(new Comparator<Object[]>() {
						@Override
						public int compare(Object[] tuple1, Object[] tuple2) {
							double value1 = (double)tuple1[0];
							double value2 = (double)tuple2[0];
							if(value1 < value2) {
								return 1;
							} else if(value1 > value2) {
								return -1;
							} else {
								return 0;
							}
						}
					})
					.collect(Collectors.toList())
				).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}

			double max = (double)sorted.get(0)[0];
			winners[LABEL] = (MultiRule)sorted.get(0)[1];
			sorted.remove(0);

			long count = sorted.stream()
					.filter(tuple -> (double)tuple[0] == max)
					.map(tuple -> (MultiRule)tuple[1])
					.filter(rule -> !Arrays.equals(winners[LABEL].getConc(), rule.getConc()))
					.count();
			if(count > 0) {
				canClassify[LABEL] = false;
			}
			else {
				canClassify[LABEL] = true;
			}

			if(canClassify[LABEL] && max != 0.0) {
				answerClass[LABEL] = winners[LABEL].getConc(LABEL);

				//Rule Fitness Update
				if(doMemorizeMissPatterns) {
					winners[LABEL].addFitnessVector(line, LABEL);
				}
			}
			else {
				answerClass[LABEL] = -1;
			}
		}

		//Store Miss Patterns
		if(doMemorizeMissPatterns) {
			if(!Arrays.equals(answerClass, line.getConClass()) && missPatterns.size() < 1000) {
				missPatterns.add(line.getID());
			}
		}

		return answerClass;
	}

	/**
	 * 各ラベルに対するCFの平均を用いて単一勝利ルールによって識別を行う．<br>
	 * @param pattern
	 * @param doMemorizeMissPatterns
	 * @return
	 */
	public int[] cfMeanClassifyParallel(Pattern pattern, boolean doMemorizeMissPatterns) {
		MultiPattern line = (MultiPattern)pattern;
		int[] answerClass = new int[line.getLnum()];

		List<Object[]> sorted = null;
		try {
			sorted = Setting.forkJoinPool.submit( () ->
				micRules.parallelStream()
				.map(rule -> {
					Object[] tuple = new Object[2];
					tuple[0] = rule.calcAdaptationPure(line) * rule.getCf();
					tuple[1] = rule;
					return tuple;

				})
				.sorted(new Comparator<Object[]>() {
					@Override
					public int compare(Object[] tuple1, Object[] tuple2) {
						double value1 = (double)tuple1[0];
						double value2 = (double)tuple2[0];
						if(value1 < value2) {
							return 1;
						} else if(value1 > value2) {
							return -1;
						} else {
							return 0;
						}
					}
				})
				.collect(Collectors.toList())
			).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		double max = (double)sorted.get(0)[0];
		MultiRule winner = (MultiRule)sorted.get(0)[1];
		sorted.remove(0);

		long count = sorted.stream()
						.filter(tuple -> (double)tuple[0] == max)
						.map(tuple -> (MultiRule)tuple[1])
						.filter(rule -> !Arrays.equals(winner.getConc(), rule.getConc()))
						.count();
		boolean canClassify;
		if(count > 0) {
			canClassify = false;
		}
		else {
			canClassify = true;
		}


		if(canClassify && max != 0.0) {
			answerClass = Arrays.copyOf(winner.getConc(), winner.getConc().length);

			//Rule Fitness Update
			if(doMemorizeMissPatterns) {
				winner.addFitness(line);
			}
		}
		else {
			Arrays.fill(answerClass, -1);
		}

		//Store Miss Patterns
		if(doMemorizeMissPatterns) {
			if(!Arrays.equals(answerClass, line.getConClass()) && missPatterns.size() < 1000) {
				missPatterns.add(line.getID());
			}
		}

		return answerClass;
	}

	/**
	 * Classification pattern<br>
	 * Note: 誤識別パターンの保持は行うかどうかはdoMemorize引数に任せる<br>
	 * @param line : Multi : Input pattern
	 * @param doMemorizeMissPatterns : boolean : true:memorize miss patterns, false:don't
	 * @return int[] : Classified Class
	 */
	@Override
	public int[] classify(Pattern pattern, boolean doMemorizeMissPatterns) {
		int[] answerClass = null;

		if(Consts.MULTI_CF_TYPE == 0) {
			answerClass = cfMeanClassify(pattern, doMemorizeMissPatterns);
		}
		else if(Consts.MULTI_CF_TYPE == 1) {
			answerClass = cfVectorClassify(pattern, doMemorizeMissPatterns);
		}


		return answerClass;
	}

	//TODO maxMulの計算を1度行えば良いように変更
	//TODO double[ruleNum] maxMulsを用意して，その値に対してラベルごとのCFを掛け算することで短縮できる．
	public int[] cfVectorClassify(Pattern pattern, boolean doMemorizeMissPatterns) {
		MultiPattern line = (MultiPattern)pattern;
		int[] answerClass = new int[line.getLnum()];
		int[] winRuleIdx = new int[line.getLnum()];

		boolean[] canClassify = new boolean[line.getLnum()];

		//Find winner rules for every label
		for(int l = 0; l < line.getLnum(); l++) {
			double maxMul = 0.0;
			for(int r = 0; r < ruleNum; r++) {
				double multiValue = micRules.get(r).getCFVector(l) * micRules.get(r).calcAdaptationPure(line);
				if(maxMul < multiValue) {
					maxMul = multiValue;
					winRuleIdx[l] = r;
					canClassify[l] = true;
				}
				else if(maxMul == multiValue &&
						!Arrays.equals(micRules.get(r).getConc(), micRules.get(winRuleIdx[l]).getConc())) {
					//MultiValues are same, and Conclusion is NOT exact match.
					canClassify[l] = false;
				}
			}

			if(canClassify[l] && maxMul != 0.0) {
				answerClass[l] = micRules.get(winRuleIdx[l]).getConc(l);

				//Rule Fitness Update
				if(doMemorizeMissPatterns) {
					micRules.get(winRuleIdx[l]).addFitnessVector(line, l);
				}
			}
			else {
				answerClass[l] = -1;
			}
		}

		//Store Miss Patterns
		if(doMemorizeMissPatterns) {
			if(!Arrays.equals(answerClass, line.getConClass()) && missPatterns.size() < 1000) {
				missPatterns.add(line.getID());
			}
		}

		return answerClass;
	}

	/**
	 * 各ラベルに対するCFの平均を用いて単一勝利ルールによって識別を行う．<br>
	 * @param pattern
	 * @param doMemorizeMissPatterns
	 * @return
	 */
	public int[] cfMeanClassify(Pattern pattern, boolean doMemorizeMissPatterns) {
		MultiPattern line = (MultiPattern)pattern;
		int[] answerClass = new int[line.getLnum()];
		int winRuleIdx = 0;

		boolean canClassify = true;
		double maxMul = 0.0;
		for(int r = 0; r < ruleNum; r++) {
			double multiValue = micRules.get(r).getCf() * micRules.get(r).calcAdaptationPure(line);
			if(maxMul < multiValue) {
				maxMul = multiValue;
				winRuleIdx = r;
				canClassify = true;
			}
			else if(maxMul == multiValue &&
					!Arrays.equals(micRules.get(r).getConc(), micRules.get(winRuleIdx).getConc())) {
				//MultiValues are same, and Conclusion is NOT exact match.
				canClassify = false;
			}
		}

		if(canClassify && maxMul != 0.0) {
			answerClass = Arrays.copyOf(micRules.get(winRuleIdx).getConc(), micRules.get(winRuleIdx).getConc().length);

			//Rule Fitness Update
			if(doMemorizeMissPatterns) {
				micRules.get(winRuleIdx).addFitness(line);
			}
		}
		else {
			Arrays.fill(answerClass, -1);
		}

		//Store Miss Patterns
		if(doMemorizeMissPatterns) {
			if(!Arrays.equals(answerClass, line.getConClass()) && missPatterns.size() < 1000) {
				missPatterns.add(line.getID());
			}
		}

		return answerClass;
	}

	/**
	 * データセットに対する誤識別率(Exact-Match)を計算<br>
 	 * Note: 並列計算を行わない．<br>
	 * @param dataSetInfo : MultiDataSetInfo
 	 * @param doMemorizeMissPatterns : boolean : true:memorize miss patterns, false:don't
	 * @param forkJoinPool
	 * @return double : 完全一致 誤識別率[%]
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public double calcMissRate(DataSetInfo dataSetInfo, boolean doMemorizeMissPatterns) {
		MultiDataSetInfo dataset = (MultiDataSetInfo)dataSetInfo;
		double exactMatchNum = 0;	//if classification is exact-match, then this count increments.
		double errorRate = 0.0;

		if(doMemorizeMissPatterns) {
			missPatterns.clear();
		}

		for(int p = 0; p < dataset.getDataSize(); p++) {
			int[] answerClass = classify(dataset.getPattern(p), doMemorizeMissPatterns);
			if(Arrays.equals(answerClass, dataset.getPattern(p).getConClass())) {
				exactMatchNum++;
			}
		}

		//range [0, 1]
		errorRate = ((double)dataset.getDataSize() - exactMatchNum) / (double)dataset.getDataSize();

		return errorRate * 100.0;
	}


	/**
	 * データセットに対する誤識別率(Exact-Match)を計算<br>
 	 * Note: 並列計算を行う．ここでは，誤識別パターンの保持は行わない．<br>
	 * @param dataSetInfo : MultiDataSetInfo
	 * @param forkJoinPool
	 * @return double : 誤識別率[%]
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public double calcMissRateParallel(DataSetInfo dataSetInfo, boolean doMemorizeMissPatterns) {
		MultiDataSetInfo dataset = (MultiDataSetInfo)dataSetInfo;
		double exactMatchNum = 0;	//if classification is exact-match, then this count increments.
		double errorRate = 0.0;

		for(int p = 0; p < dataset.getDataSize(); p++) {
			int[] ans = classifyParallel(dataset.getPattern(p), doMemorizeMissPatterns);

			if(Arrays.equals(ans, dataset.getPattern(p).getConClass())) {
				exactMatchNum++;
			}
		}

		//range [0, 1]
		errorRate = ((double)dataset.getDataSize() - exactMatchNum) / (double)dataset.getDataSize();
		return errorRate * 100.0;
	}

	/**
	 * Fuzzy Machine Learning - ファジィ機械学習<br>
	 * 学習データセット(train)から各ルールの結論部を計算する．
	 * @param Dtra : MultiDataSetInfo
	 * @param forkJoinPool
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void learning(DataSetInfo dataset, ForkJoinPool forkJoinPool) {
		MultiDataSetInfo train = (MultiDataSetInfo)dataset;
		for(MultiRule rule : micRules) {
			rule.calcRuleConc(train, forkJoinPool);
		}

	}

}
