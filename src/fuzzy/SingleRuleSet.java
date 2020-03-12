package fuzzy;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import data.DataSetInfo;
import data.Pattern;
import data.SingleDataSetInfo;
import data.SinglePattern;
import main.Setting;

public class SingleRuleSet extends RuleSet<SingleRule>{
	// ************************************************************

	// ************************************************************
	public SingleRuleSet() {}

	/**
	 * Deep Copy
	 * @param ruleSet
	 */
	public SingleRuleSet(SingleRuleSet ruleSet) {
		deepCopy(ruleSet);
	}

	// ************************************************************

	@Override
	public Object newInstance() {
		SingleRuleSet instance = new SingleRuleSet();
		return instance;
	}

	public Object newInstance(SingleRuleSet ruleSet) {
		SingleRuleSet instance = new SingleRuleSet(ruleSet);
		return instance;
	}

	@Override
	public void deepCopySpecific(Object ruleSet) {}

	//TODO
	//micRulesの結論部クラスに存在するクラスを保持
	/**
	 * 例: 4クラス問題のとき
	 * Rule1: Class 0, Rule2: Class 0, Rule3: Class 2
	 * → haveCla = {0, 2} をreturnする
	 */
	public int[] calcHaveClass() {
		int haveCla[] = micRules.stream()
								.mapToInt(r -> r.getConc())
								.distinct()
								.sorted()
								.toArray();
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
	 * @param line : Pattern : Input pattern
	 * @param doMemorizeMissPatterns : boolean : true:memorize miss patterns, false:don't
	 * @return int[] : Classified Class
	 */
	public int[] classifyParallel(Pattern pattern, boolean doMemorizeMissPatterns) {
		SinglePattern line = (SinglePattern)pattern;
		int[] answerClass = new int[1];

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
						} else if(value1 > value2)  {
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
		SingleRule winner = (SingleRule)sorted.get(0)[1];
		sorted.remove(0);

		long count = sorted.stream()
						.filter(tuple -> (double)tuple[0] == max)
						.map(tuple -> (SingleRule)tuple[1])
						.filter(rule -> winner.getConc() != rule.getConc() )
						.count();
		boolean canClassify;
		if(count > 0) {
			canClassify = false;
		}
		else {
			canClassify = true;
		}


		if(canClassify && max != 0.0) {
			answerClass[0] = winner.getConc();

			//Rule Fitness Update
			if(doMemorizeMissPatterns) {
				if(answerClass[0] == line.getConClass()) {
					winner.addFitness();
					winner.addNCP();
				}
				else {
					winner.addNMP();
				}
			}
		}
		else {
			Arrays.fill(answerClass, -1);
		}

		//Store Miss Patterns
		if(doMemorizeMissPatterns) {
			if(answerClass[0] != line.getConClass() && missPatterns.size() < 1000) {
				missPatterns.add(line.getID());
			}
		}

		return answerClass;
	}


	/**
	 * Classification pattern<br>
	 * Note: 誤識別パターンの保持は行うかどうか + Rule Fitness更新 はdoMemorize引数に任せる<br>
	 * @param line : Pattern : Input pattern
	 * @param doMemorizeMissPatterns : boolean : true:memorize miss patterns, false:don't
	 * @return int : Classified Class
	 */
	public int[] classify(Pattern pattern, boolean doMemorizeMissPatterns) {
		SinglePattern line = (SinglePattern)pattern;
		int[] answerClass = new int[1];
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
					micRules.get(r).getConc() != micRules.get(winRuleIdx).getConc()) {
				canClassify = false;
			}
		}

		if(canClassify && maxMul != 0.0) {
			answerClass[0] = micRules.get(winRuleIdx).getConc();

			//Rule Fitness Update
			if(doMemorizeMissPatterns) {
				if(answerClass[0] == line.getConClass()) {
					micRules.get(winRuleIdx).addFitness();
					micRules.get(winRuleIdx).addNCP();
				}
				else {
					micRules.get(winRuleIdx).addNMP();
				}
			}
		}
		else {
			answerClass[0] = -1;
		}

		//誤識別パターンの保持
		if(doMemorizeMissPatterns) {
			if(answerClass[0] != line.getConClass() && missPatterns.size() < 1000) {
				missPatterns.add(line.getID());
			}
		}

		return answerClass;
	}

	/**
	 * データセットに対する誤識別率を計算<br>
 	 * Note: 並列計算を行わない．<br>
	 * @param dataSetInfo : DataSetInfo
 	 * @param doMemorizeMissPatterns : boolean : true:memorize miss patterns, false:don't
	 * @param forkJoinPool
	 * @return double : 誤識別率[%]
	 */
	@SuppressWarnings("rawtypes")
	public double calcMissRate(DataSetInfo dataset, boolean doMemorizeMissPatterns) {
		SingleDataSetInfo dataSetInfo = (SingleDataSetInfo)dataset;
		double missNums = 0;
		if(doMemorizeMissPatterns) {
			this.missPatterns.clear();

			for(int rule = 0; rule < this.micRules.size(); rule++) {
				this.micRules.get(rule).clearFitness();
			}
		}

		for(int p = 0; p < dataSetInfo.getDataSize(); p++) {
			int answerClass = classify(dataSetInfo.getPattern(p), doMemorizeMissPatterns)[0];
			if(answerClass != dataSetInfo.getPattern(p).getConClass()) {
				missNums++;
			}
		}

		return ( missNums / (double)dataSetInfo.getDataSize() ) * 100.0;
	}

	/**
	 * データセットに対する誤識別率を計算<br>
 	 * Note: ルールごとに並列計算を行う
	 * @param dataSetInfo : DataSetInfo
	 * @param forkJoinPool
	 * @return double : 誤識別率[%]
	 */
	@SuppressWarnings("rawtypes")
	public double calcMissRateParallel(DataSetInfo dataset, boolean doMemorizeMissPatterns) {
		SingleDataSetInfo dataSetInfo = (SingleDataSetInfo)dataset;
		double missNums = 0;
		for(int p = 0; p < dataSetInfo.getDataSize(); p++) {
			int[] ans = classifyParallel(dataSetInfo.getPattern(p), doMemorizeMissPatterns);
			if(ans[0] != dataSetInfo.getPattern(p).getConClass()) {
				missNums++;
			}
		}
		return ( missNums / (double)dataSetInfo.getDataSize() ) * 100.0;
	}

	/**
	 * Fuzzy Machine Learning - ファジィ機械学習<br>
	 * 学習データセット(train)から各ルールの結論部を計算する．
	 * @param Dtra
	 * @param forkJoinPool
	 */
	@SuppressWarnings("rawtypes")
	public void learning(DataSetInfo dataset, ForkJoinPool forkJoinPool) {
		SingleDataSetInfo train = (SingleDataSetInfo)dataset;
		for(SingleRule rule : micRules) {
			rule.calcRuleConc(train, forkJoinPool);
		}
	}

	/****************/


}
