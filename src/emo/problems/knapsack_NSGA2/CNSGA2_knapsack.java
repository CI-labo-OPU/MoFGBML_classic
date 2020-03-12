package emo.problems.knapsack_NSGA2;

import emo.algorithms.nsga2.Individual_nsga2;
import emo.algorithms.nsga2.NSGA2;
import ga.Individual;

public class CNSGA2_knapsack extends NSGA2 {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isDominate(Individual p, Individual q, int[] optimizer) {
		boolean isDominate = false;

		if(!p.isFeasible() && !q.isFeasible()) {
			if(((Individual_knapsack)p).getSumConst() < ((Individual_knapsack)q).getSumConst()) {
				isDominate = true;
			} else if(((Individual_knapsack)p).getSumConst() > ((Individual_knapsack)q).getSumConst()) {
				isDominate = false;
			} else {
				if(p.getID() > q.getID()) {
					isDominate = true;
				} else {
					isDominate = false;
				}
			}
		}

		if(p.isFeasible() && q.isFeasible()) {
			for(int o = 0; o < optimizer.length; o++) {
				if(optimizer[o] * p.getFitness(o) > optimizer[o] * q.getFitness(o)) {
					isDominate = false;
					break;
				}
				else if(optimizer[o] * p.getFitness(o) < optimizer[o] * q.getFitness(o)) {
					isDominate = true;
				}
			}
		}

		if(p.isFeasible() && !q.isFeasible()) {
			isDominate = true;
		}

		if(!p.isFeasible() && q.isFeasible()) {
			isDominate = false;
		}

		return isDominate;
	}

	/**
	 *
	 * @param a
	 * @param b
	 * @return true: winner a, false: winner b.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean selectionCriteria(Individual_nsga2 aa, Individual_nsga2 bb) {
		boolean winner = true;
		Individual_knapsack a = (Individual_knapsack)aa;
		Individual_knapsack b = (Individual_knapsack)bb;

		//Both A and B are feasible.
		if(a.isFeasible() && b.isFeasible()) {
			if(a.getRank() < b.getRank()) {
				winner = true;
			} else if(a.getRank() > b.getRank()) {
				winner = false;
			} else {
				if(a.getCrowding() > b.getCrowding()) {
					winner = true;
				} else if(a.getCrowding() < b.getCrowding()) {
					winner = false;
				} else {// a.crowding == b.crowding
					if(a.getID() < b.getID()) {
						winner = true;
					} else {
						winner = false;
					}
				}
			}

			return winner;
		}

		//Both A and B are infeasible
		if(!a.isFeasible() && !b.isFeasible()) {
			double constraintA = 0;
			double constraintB = 0;
			for(int o = 0; o < a.getObjectiveNum(); o++) {
				constraintA += a.getConstraint(o);
				constraintB += b.getConstraint(o);
			}
			if(constraintA < constraintB) {
				winner = true;
			} else if(constraintA > constraintB){
				winner = false;
			} else {
				if(a.getID() < b.getID()) {
					winner = true;
				} else {
					winner = false;
				}
			}
			return winner;
		}

		if(a.isFeasible() && !b.isFeasible()) {
			winner = true;
			return winner;
		}

		if(!a.isFeasible() && b.isFeasible()) {
			winner = false;
			return winner;
		}
		return winner;
	}
}
