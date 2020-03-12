package emo.algorithms.moead;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import ga.Individual;
import main.Consts;
import method.StaticFunction;

@SuppressWarnings("rawtypes")
public abstract class ScalarizeFunction<T extends Individual> {
	// ************************************************************
	protected int id;
	protected int objectiveNum;
	protected double[] weight;

	/** ideal point */
	protected double[] z;
	/** nadir point */
	protected double[] e;
	/** IDs of Neighbor Vectors for Mating Selection */
	protected int[] matingNeighbors;

	/** IDs of Neighbor Vectors for Update Best Population */
	protected int[] updateNeighbors;

	/** Best Solution of this */
	T best;
	/** Best Scalar Function Value */
	double bestScalar;

	/** The best Fitnesses Value found so far */
	double[] FV;

	// ************************************************************
	public ScalarizeFunction(int id, int objectiveNum) {
		this.id = id;
		this.objectiveNum = objectiveNum;
		this.weight = new double[objectiveNum];
		this.z = new double[objectiveNum];
		this.e = new double[objectiveNum];
		this.FV = new double[objectiveNum];
	}

	// ************************************************************

	public abstract double function(double[] f);

	/**
	 * <h1>Normalize f</h1>
	 * @param f : double[] : objective value
	 * @param z : double[] : ideal point
	 * @param e : double[] : nadir point
	 * @return double[] : Normalized f[]
	 */
	public double[] normalize(double[] f) {
		double[] normalized = new double[f.length];
		for(int o = 0; o < f.length; o++) {
			normalized[o] = (f[o] - z[o]) / (e[o] - z[o]);
		}
		return normalized;
	}

	/**
	 * Deep Copy <br>
	 * @param individual
	 */
	@SuppressWarnings({ "unchecked" })
	public void updateBest(T individual) {
		double scalar = function(individual.getFitness());

		if(scalar <= bestScalar) {
			try {
				Class<?> entity = individual.getClass();
				T clone = (T)entity.newInstance();
				clone.deepCopy(individual);
				best = clone;
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
			bestScalar = scalar;
			FV = Arrays.copyOf(individual.getFitness(), individual.getFitness().length);
		}
	}

	public void calcMatingNeighbors(ScalarizeFunction[] vectors) {

		int T = Consts.SELECTION_NEIGHBOR_NUM;
		this.matingNeighbors = new int[T];

		List<ScalarizeFunction> temp = Arrays.asList(vectors);
		Object[] a = temp.stream()
				.map(vector -> {
					/** (ID, distance from this weight) */
					double[] tuple = new double[2];
					tuple[0] = vector.getID();
					tuple[1] = StaticFunction.distanceVectors(this.weight, vector.weight);
					return tuple;
				})
				.sorted(new Comparator<double[]>() {
					@Override
					public int compare(double[] tuple1, double[] tuple2) {
						if(tuple1[1] < tuple2[1]) {
							return -1;
						} else if(tuple1[1] > tuple2[1]) {
							return 1;
						} else {
							if(tuple1[0] < tuple2[0]) {
								return -1;
							} else {
								return 1;
							}
						}
					}
				})
				.toArray();

		for(int i = 0; i < T; i++) {
			matingNeighbors[i] = (int)((double[])a[i])[0];
		}

	}

	public void calcUpdateNeighbors(ScalarizeFunction[] vectors) {

		int T = Consts.UPDATE_NEIGHBOR_NUM;
		this.updateNeighbors = new int[T];

		List<ScalarizeFunction> temp = Arrays.asList(vectors);
		Object[] a = temp.stream()
				.map(vector -> {
					/** (ID, distance from this weight) */
					double[] tuple = new double[2];
					tuple[0] = vector.getID();
					tuple[1] = StaticFunction.distanceVectors(this.weight, vector.weight);
					return tuple;
				})
				.sorted(new Comparator<double[]>() {
					@Override
					public int compare(double[] tuple1, double[] tuple2) {
						if(tuple1[1] < tuple2[1]) {
							return -1;
						} else if(tuple1[1] > tuple2[1]) {
							return 1;
						} else {
							if(tuple1[0] < tuple2[0]) {
								return -1;
							} else {
								return 1;
							}
						}
					}
				})
				.toArray();

		for(int i = 0; i < T; i++) {
			updateNeighbors[i] = (int)((double[])a[i])[0];
		}

	}

	public int getObjectiveNum() {
		return this.objectiveNum;
	}

	/**
	 * Deep Copy
	 * @param weight : double[] :
	 */
	public void setWeight(double[] weight) {
		this.weight = Arrays.copyOf(weight, weight.length);
	}

	public double[] getWeight() {
		return this.weight;
	}

	/**
	 * Shallow Copy
	 * @param z : double[] : ideal point
	 */
	public void setZ(double[] z) {
		this.z = z;
	}

	/**
	 *
	 * @return double[] : ideal point
	 */
	public double[] getZ() {
		return this.z;
	}

	/**
	 * Shallow Copy
	 * @param e : double[] : nadir point
	 */
	public void setE(double[] e) {
		this.e = e;
	}

	/**
	 *
	 * @return double[] : nadir point
	 */
	public double[] getE() {
		return this.e;
	}

	/**
	 * Deep Copy
	 * @param neighbor : int[] : IDs of Neighbor vectors
	 */
	public void setMatingNeighbors(int[] neighbors) {
		this.matingNeighbors = Arrays.copyOf(neighbors, neighbors.length);
	}

	public int[] getMatingNeighbors() {
		return this.matingNeighbors;
	}

	/**
	 * Deep Copy
	 * @param neighbor : int[] : IDs of Neighbor vectors
	 */
	public void setUpdateNeighbors(int[] neighbors) {
		this.updateNeighbors = Arrays.copyOf(neighbors, neighbors.length);
	}

	public int[] getUpdateNeighbors() {
		return this.updateNeighbors;
	}

	/**
	 * Deep Copy
	 * @param FV : double[] :
	 */
	public void setFV(double[] FV) {
		this.FV = Arrays.copyOf(FV, FV.length);
	}

	public double[] getFV() {
		return this.FV;
	}

	/**
	 * Deep Copy
	 * @param best : Individual :
	 */
	@SuppressWarnings("unchecked")
	public void setBest(T best) {
		try {
			Class<?> entity = best.getClass();
			T clone = (T)entity.newInstance();
			clone.deepCopy(best);
			this.best = clone;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}

	}

	public T getBest() {
		return this.best;
	}

	public void setBestScalar(double bestScalar) {
		this.bestScalar = bestScalar;
	}

	public double getBestScalar() {
		return this.bestScalar;
	}

	public void setID(int id) {
		this.id = id;
	}

	public int getID() {
		return this.id;
	}

}
