package fgbml.mofgbml;

import fgbml.SinglePittsburgh;
import fgbml.problem.OutputClass;
import ga.Population;

public class Output_MoFGBML extends OutputClass<SinglePittsburgh> {

	/**
	 * <h1>Output Information of Individual</h1>
	 * Header:<br>
	 * id, f0, f1, ..., Dtra, Dtst, ruleNum, ruleLength, rank, crowding
	 * @param fileName : String
	 * @param population : Population{@literal <Pittsburgh>} : population
	 */
	@Override
	public String outputPittsburgh(Population<SinglePittsburgh> population) {
		String ln = System.lineSeparator();
		String strs = "";
		String str = "";

		int popSize = population.getIndividuals().size();
		int objectiveNum = population.getIndividual(0).getObjectiveNum();

		//Header
		str = "id";
		for(int o = 0; o < objectiveNum; o++) {
			str += "," + "f" + String.valueOf(o);
		}
		//Appendix
		str += "," + "Dtra";
		str += "," + "Dtst";
		str += "," + "ruleNum";
		str += "," + "ruleLength";

		//EMO
		str += "," + "rank";
		str += "," + "crowding";
		strs += str + ln;

		//Population
		for(int p = 0; p < popSize; p++) {
			//id
			str = String.valueOf(p);
			//fitness
			for(int o = 0; o < objectiveNum; o++) {
				str += "," + population.getIndividual(p).getFitness(o);
			}
			//Dtra
			str += "," + population.getIndividual(p).getAppendix(0);
			//Dtst
			str += "," + population.getIndividual(p).getAppendix(1);
			//ruleNum
			str += "," + population.getIndividual(p).getRuleSet().getRuleNum();
			//ruleLength
			str += "," + population.getIndividual(p).getRuleSet().getRuleLength();

			//rank
			str += "," + population.getIndividual(p).getRank();
			//crowding distance
			str += "," + population.getIndividual(p).getCrowding();

			strs += str + ln;
		}

		return strs;
	}

	@Override
	public String outputRuleSet(Population<SinglePittsburgh> population) {
		String ln = System.lineSeparator();
		String row = "***************************************";
		String hyphen = "---";

		String strs = "";
		String str = "";

		int popSize = population.getIndividuals().size();
		int Ndim = population.getIndividual(0).getNdim();
		int objectiveNum = population.getIndividual(0).getObjectiveNum();

		for(int pop = 0; pop < popSize; pop++) {
			int ruleNum = population.getIndividual(pop).getRuleNum();

			strs += row + ln;
			strs += "pop_" + pop + ln;
			strs += "ruleNum: " + ruleNum + ln;
			strs += "rank: " + population.getIndividual(pop).getRank() + ln;
			strs += "crowding: " + population.getIndividual(pop).getCrowding() + ln;
			for(int o = 0; o < objectiveNum; o++) {
				strs += "f"+o+": " + population.getIndividual(pop).getFitness(o) + ln;
			}
			strs += hyphen + ln;

			//Rules
			for(int rule = 0; rule < ruleNum; rule++) {
				//id
				str = "Rule_" + String.format("%02d", rule) + ":";
				//rule
				for(int n = 0; n < Ndim; n++) {
					str += " " + String.format("%2d", population.getIndividual(pop).getRuleSet().getMicRule(rule).getRule(n));
				}
				//class
				str += ", " + "Class: " + String.format("%2d", population.getIndividual(pop).getRuleSet().getMicRule(rule).getConc());
				//cf
				str += ", " + "CF: " + population.getIndividual(pop).getRuleSet().getMicRule(rule).getCf();
				//fitness
				str += ", " + "Fitness: " + population.getIndividual(pop).getRuleSet().getMicRule(rule).getFitness();

				strs += str + ln;
			}
			strs += row + ln;
			strs += "" + ln;
		}

		return strs;
	}


}
