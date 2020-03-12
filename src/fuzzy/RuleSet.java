package fuzzy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ForkJoinPool;

import data.DataSetInfo;
import data.Pattern;

/**
 *
 * @param <T> : Ruleクラスを継承したクラス
 */
public abstract class RuleSet<T extends Rule> {
	// ************************************************************
	//Fuzzy If-then rule
	protected ArrayList<T> micRules = new ArrayList<T>();
	protected int ruleNum;
	protected int ruleLength;

	//誤識別パターン保持
	protected ArrayList<Integer> missPatterns = new ArrayList<Integer>();

	// ************************************************************


	// ************************************************************

	public abstract Object newInstance();



	public abstract void deepCopySpecific(Object ruleSet);

	public abstract int[] classify(Pattern line, boolean doMemorizeMissPatterns);

	public abstract int[] classifyParallel(Pattern line, boolean doMemorizeMissPatterns);

	@SuppressWarnings("rawtypes")
	public abstract double calcMissRate(DataSetInfo dataSetInfo, boolean doMemorizeMissPatterns);

	@SuppressWarnings("rawtypes")
	public abstract double calcMissRateParallel(DataSetInfo dataSetInfo, boolean doMemorizeMissPatterns);

	@SuppressWarnings("rawtypes")
	public abstract void learning(DataSetInfo train, ForkJoinPool forkJoinPool);

	/**
	 * Deep Copy
	 * @param ruleSet
	 */
	@SuppressWarnings("unchecked")
	public void deepCopy(Object ruleSet) {
		RuleSet<T> cast = (RuleSet<T>) ruleSet;

		this.ruleNum = cast.ruleNum;
		this.ruleLength = cast.ruleLength;
		missPatterns = new ArrayList<>( cast.missPatterns );
		this.micRules.clear();
		for(int i = 0; i < cast.micRules.size(); i++) {
			try {
				Class<?> entity = cast.getMicRule(i).getClass();
				T rule = (T)entity.newInstance();
				((Rule)rule).deepCopy(cast.getMicRule(i));
				micRules.add((T)rule);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}

		deepCopySpecific(ruleSet);
	}

	public void calcRuleLength() {
		ruleNum = micRules.size();
		ruleLength = 0;
		for(int i = 0; i < ruleNum; i++) {
			ruleLength += ((Rule)micRules.get(i)).calcRuleLength();
		}
	}

	public void clearMissPatterns() {
		this.missPatterns.clear();
	}

	/**
	 * MicRulesをruleの基数でソートする
	 */
//	@SuppressWarnings("unchecked")
	public void radixSort() {
		Collections.sort(this.micRules, new Comparator<Rule>() {
			@Override
			public int compare(Rule a, Rule b) {
				int Ndim = a.getRule().length;
				for(int i = 0; i < Ndim; i++) {
					if(a.getRule(i) < b.getRule(i)) {
						return -1;
					}
					else if (a.getRule(i) > b.getRule(i)){
						return 1;
					}
					else {
						continue;
					}
				}
				return 0;
			}
		});
	}

	public void removeRuleByFitness() {
		for(int i = 0; i < micRules.size(); i++) {
			int head = 0;
			while(micRules.size() > head) {
				//(fitness == 0)
				if( ((Rule)micRules.get(head)).getFitness() == 0 ) {
					micRules.remove(head);
				}
				else {
					head++;
				}
			}
		}
	}

	public void removeRule() {
		for(int i = 0; i < micRules.size(); i++) {
			int head = 0;
			while(micRules.size() > head) {
				// (CF <= 0) or (length == 0)
				if( ((Rule)micRules.get(head)).getCf() <= 0 ||
					((Rule)micRules.get(head)).getRuleLength() == 0) {
					micRules.remove(head);
				}
				else {
					head++;
				}
			}
		}

		//Same Antecedent Judge
		ArrayList<Integer> sameList = new ArrayList<Integer>();
		for(int i = 0; i < micRules.size(); i++) {
			for(int j = 0; j < i; j++) {
				if(!sameList.contains(j)) {
					if(Arrays.equals(((Rule)micRules.get(i)).rule, ((Rule)micRules.get(j)).rule)) {
						sameList.add(i);
					}
				}
			}
		}
		for(int i = 0; i < sameList.size(); i++) {
			micRules.remove( sameList.get(i) - i);
		}

	}

	/**
	 * Deep Copy
	 * @param rule
	 */
	@SuppressWarnings("unchecked")
	public void addRule(T rule) {
		try {
			Class<?> entity = rule.getClass();
			T clone = (T)entity.newInstance();
			((Rule)clone).deepCopy(rule);
			micRules.add((T)clone);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public int getRuleNum() {
		return this.ruleNum;
	}

	public int getRuleLength() {
		return this.ruleLength;
	}

	public ArrayList<Integer> getMissPatterns(){
		return this.missPatterns;
	}

	public Integer getMissPattern(int index) {
		return this.missPatterns.get(index);
	}

	public ArrayList<T> getMicRules(){
		return this.micRules;
	}

	public T getMicRule(int index) {
		return this.micRules.get(index);
	}

	/**
	 * Deep Copy
	 * @param index : int
	 * @param rule : Rule
	 */
	@SuppressWarnings("unchecked")
	public void setMicRule(int index, T rule) {
		try {
			Class<?> entity = rule.getClass();
			T clone = (T)entity.newInstance();
			((Rule)clone).deepCopy(rule);
			this.micRules.set(index, (T)clone);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

}
