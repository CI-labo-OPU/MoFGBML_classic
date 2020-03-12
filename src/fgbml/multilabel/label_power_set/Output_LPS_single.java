package fgbml.multilabel.label_power_set;

import fgbml.SinglePittsburgh;
import fgbml.problem.OutputClass;
import ga.Population;

public class Output_LPS_single extends OutputClass<SinglePittsburgh>{
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
		str += "," + "rank";
		str += "," + "crowding";
		//Appendix
		str += "," + "Dtra";
		str += "," + "ruleNum";
		str += "," + "ruleLength";
		strs += str + ln;

		//Population
		for(int p = 0; p < popSize; p++) {
			//id
			str = String.valueOf(p);
			//fitness
			for(int o = 0; o < objectiveNum; o++) {
				str += "," + population.getIndividual(p).getFitness(o);
			}
			//rank
			str += "," + population.getIndividual(p).getRank();
			//crowding distance
			str += "," + population.getIndividual(p).getCrowding();
			//Dtra
			str += "," + population.getIndividual(p).getAppendix(0);
			//ruleNum
			str += "," + population.getIndividual(p).getRuleSet().getRuleNum();
			//ruleLength
			str += "," + population.getIndividual(p).getRuleSet().getRuleLength();

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
