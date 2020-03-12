package ga;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import emo.problems.MOP;
import main.Setting;

/**
 *	Populationクラスを継承するときは，Individualクラスを継承したクラスをTに設定する． <br>
 *	基本的にメソッドの追加や，オーバーライドの必要はない．<br>
 *
 * @param <T> T is Class which extends Individual<>.
 */

@SuppressWarnings("rawtypes")
public class Population<T extends Individual> {
	// ************************************************************
	protected ArrayList<T> individuals = new ArrayList<T>();

	// ************************************************************
	public Population() {}

	// ************************************************************
	/**
	 * <h1>並列実装で行う個体群の評価</h1>
	 * @param mop MOPを継承したクラス
	 */
	@SuppressWarnings({ "unchecked" })
	public void evaluateIndividuals(MOP mop) {
		try {
			Setting.forkJoinPool.submit( () ->
				individuals.parallelStream()
				.forEach( individual -> mop.evaluate(individual) )
			).get();

		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 個体群ArrayListに個体popを追加する．<br>
	 * DeepCopyしてaddする．<br>
	 * @param individual Individualクラスを継承したクラス
	 */
	@SuppressWarnings({ "unchecked" })
	public void addIndividual(T individual) {
		try {
			Class<?> entity = individual.getClass();
			T clone = (T)entity.newInstance();
			((Individual)clone).deepCopy((Individual)individual);
			this.individuals.add((T)clone);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}

	}

	public void removeIndividual(int index) {
		this.individuals.remove(index);
	}

	/**
	 * 個体群ArrayListを受け取り，自身の個体群としてセットする．
	 * @param individuals ArrayList{@literal <Individual>} 個体群リスト
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setIndividuals(ArrayList<Individual> individuals) {
		for(int i = 0; i < individuals.size(); i++) {
			this.addIndividual((T)individuals.get(i));
		}
	}

	public T getIndividual(int index) {
		return this.individuals.get(index);
	}

	public ArrayList<T> getIndividuals(){
		return this.individuals;
	}
}
