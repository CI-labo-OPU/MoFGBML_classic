package ga;

import java.util.concurrent.ExecutionException;

import emo.problems.MOP;
import main.Setting;

/**
 * 各MOPごとで，このクラスを継承したManagerを作成する
 *
 * @param <T> : T is Class which extends Population<>
 */

@SuppressWarnings("rawtypes")
public class PopulationManager<T extends Population> {
	// ************************************************************
	public T population;
	public T offspring;

	// ************************************************************
	public PopulationManager() {}

	// ************************************************************

	/**
	 * <h1>並列実装で行う個体群の評価</h1>
	 *
	 * @param mop : MOP
	 */
	@SuppressWarnings("unchecked")
	public void populationEvaluation(MOP mop) {
		try {
			Setting.forkJoinPool.submit( () ->
				population.getIndividuals().parallelStream()
				.forEach( individual -> mop.evaluate(individual) )
			).get();

		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <h1>並列実装で行う子個体群の評価</h1>
	 *
	 * @param mop : MOP
	 */
	@SuppressWarnings("unchecked")
	public void offspringEvaluation(MOP mop) {
		try {
			Setting.forkJoinPool.submit( () ->
				offspring.getIndividuals().parallelStream()
				.forEach( individual -> mop.evaluate(individual) )
			).get();

		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	/** Shallow Copy */
	public void setPopulation(T population) {
		this.population = population;
	}

	public T getPopulation(){
		return this.population;
	}

	/** Shallow Copy */
	public void setOffspring(T offspring) {
		this.offspring = offspring;
	}

	public T getOffspring(){
		return this.offspring;
	}

}
