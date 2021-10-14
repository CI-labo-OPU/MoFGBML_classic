package fgbml.multitask;

import fgbml.problem.OutputClass;
import ga.Population;

public class Output_MultiLabel extends OutputClass<MultiPittsburgh>{

	public String pittsburghHeader(int objectiveNum) {
		String ln = System.lineSeparator();
		String str = "";

		str += "generation";
		str += "," + "pop_id";
		for(int o = 0; o < objectiveNum; o++) {
			str += "," + "f" + String.valueOf(o);
		}
		str += "," + "rank";
		str += "," + "crowding";


		//Appendix
		str += "," + "ExactMatchError_Dtra";
		str += "," + "Fmeasure_Dtra";
		str += "," + "Recall_Dtra";
		str += "," + "Precision_Dtra";
		str += "," + "HammingLoss_Dtra";
		str += "," + "ExactMatchError_Dtst";
		str += "," + "Fmeasure_Dtst";
		str += "," + "Recall_Dtst";
		str += "," + "Precision_Dtst";
		str += "," + "HammingLoss_Dtst";


		str += "," + "ruleNum";
		str += "," + "ruleLength";

		str += ln;

		return str;
	}

	@Override
	public String outputPittsburgh(Population<MultiPittsburgh> population) {
		String ln = System.lineSeparator();
		String strs = "";
		String str = "";

		int popSize = population.getIndividuals().size();
		int objectiveNum = population.getIndividual(0).getObjectiveNum();

		//Population
		for(int p = 0; p < popSize; p++) {
			str = "";

			//generation
			str += String.valueOf(population.getIndividual(p).getGeneration());
			//id
			str += "," + String.valueOf(p);
			//fitness
			for(int o = 0; o < objectiveNum; o++) {
				str += "," + population.getIndividual(p).getFitness(o);
			}
			//rank
			str += "," + population.getIndividual(p).getRank();
			//crowding distance
			str += "," + population.getIndividual(p).getCrowding();


			//Exact-Match for Dtra
			str += "," + population.getIndividual(p).getAppendix(0);
			//F-measure for Dtra
			str += "," + population.getIndividual(p).getAppendix(1);
			//Recall for Dtra
			str += "," + population.getIndividual(p).getAppendix(2);
			//Precision for Dtra
			str += "," + population.getIndividual(p).getAppendix(3);
			//Hamming Loss for Dtra
			str += "," + population.getIndividual(p).getAppendix(4);
			//Exact-Match for Dtst
			str += "," + population.getIndividual(p).getAppendix(5);
			//F-measure for Dtst
			str += "," + population.getIndividual(p).getAppendix(6);
			//Recall for Dtst
			str += "," + population.getIndividual(p).getAppendix(7);
			//Precision for Dtst
			str += "," + population.getIndividual(p).getAppendix(8);
			//Hamming Loss for Dtst
			str += "," + population.getIndividual(p).getAppendix(9);


			//ruleNum
			str += "," + population.getIndividual(p).getRuleSet().getRuleNum();
			//ruleLength
			str += "," + population.getIndividual(p).getRuleSet().getRuleLength();

			strs += str + ln;
		}

		return strs;
	}


	@Override
	public String outputRuleSet(Population<MultiPittsburgh> population) {
		String ln = System.lineSeparator();
		String strs = "";
		String str = "";

		int popSize = population.getIndividuals().size();
		int Ndim = population.getIndividual(0).getNdim();
		int Cnum = population.getIndividual(0).getRuleSet().getMicRule(0).getConc().length;
		int objectiveNum = population.getIndividual(0).getObjectiveNum();

		//Header
		str = "pop_id";
		str += "," + "rule_id";
		for(int n = 0; n < Ndim; n++) {
			str += "," + "A_" + n;
		}
		for(int c = 0; c < Cnum; c++) {
			str += "," + "z_" + c;
		}
		str += "," + "CFmean";
		for(int c = 0; c < Cnum; c++) {
			str += "," + "CF_" + c;
		}
		str += "," + "fitness";
		strs += str + ln;

		//Data
		for(int pop = 0; pop < popSize; pop++) {
			int ruleNum = population.getIndividual(pop).getRuleNum();

			for(int rule = 0; rule < ruleNum; rule++) {
				//pop_id
				str = String.valueOf(pop);
				//rule_id
				str += "," + rule;
				//Antecedent Part
				for(int n = 0; n < Ndim; n++) {
					str += "," + population.getIndividual(pop).getRuleSet().getMicRule(rule).getRule(n);
				}
				//Consequent Class-Vector
				for(int c = 0; c < Cnum; c++) {
					str += "," + population.getIndividual(pop).getRuleSet().getMicRule(rule).getConc(c);
				}
				//CF-mean
				str += "," + population.getIndividual(pop).getRuleSet().getMicRule(rule).getCf();
				//CF-vector
				for(int c = 0; c < Cnum; c++) {
					str += "," + population.getIndividual(pop).getRuleSet().getMicRule(rule).getCFVector(c);
				}
				//Fitness
				str += "," + population.getIndividual(pop).getRuleSet().getMicRule(rule).getFitness();

				strs += str + ln;
			}

		}

		return strs;
	}

	public String outputRuleSet2(Population<MultiPittsburgh> population) {
		String ln = System.lineSeparator();
		String row = "***************************************";
		String hyphen = "---";

		String strs = "";
		String str = "";

		int popSize = population.getIndividuals().size();
		int Ndim = population.getIndividual(0).getNdim();
		int Lnum = population.getIndividual(0).getRuleSet().getMicRule(0).getConc().length;
		int objectiveNum = population.getIndividual(0).getObjectiveNum();

		for(int pop = 0; pop < popSize; pop++) {
			int ruleNum = population.getIndividual(pop).getRuleNum();

			strs += row + ln;
			strs += "pop_" + pop + ln;
			strs += "ruleNum: " + ruleNum + ln;
			strs += "rank: " + population.getIndividual(pop).getRank() + ln;
			strs += "crowding: " + population.getIndividual(pop).getCrowding() + ln;
//			strs += "--- Dtra ---" + ln;
//			strs += "   Exact-Match Error: " + population.getIndividual(pop).getAppendix(0) + ln;
//			strs += "   F-measure: " + population.getIndividual(pop).getAppendix(1) + ln;
//			strs += "   Recall: " + population.getIndividual(pop).getAppendix(2) + ln;
//			strs += "   Precision: " + population.getIndividual(pop).getAppendix(3) + ln;
//			strs += "   Hamming Loss: " + population.getIndividual(pop).getAppendix(4) + ln;
//			strs += "--- Dtst ---" + ln;
//			strs += "   Exact-Match Error: " + population.getIndividual(pop).getAppendix(5) + ln;
//			strs += "   F-measure: " + population.getIndividual(pop).getAppendix(6) + ln;
//			strs += "   Recall: " + population.getIndividual(pop).getAppendix(7) + ln;
//			strs += "   Precision: " + population.getIndividual(pop).getAppendix(8) + ln;
//			strs += "   Hamming Loss: " + population.getIndividual(pop).getAppendix(9) + ln;
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
				str += ", " + "Class:";
				for(int l = 0; l < Lnum; l++) {
					str += " " + population.getIndividual(pop).getRuleSet().getMicRule(rule).getConc(l);
				}
				//cf Mean
				str += ", " + "CF_mean: " + population.getIndividual(pop).getRuleSet().getMicRule(rule).getCf();
				//CF Vector
				str += ", " + "CF_vector:";
				for(int l = 0; l < Lnum; l++) {
					str += " " + population.getIndividual(pop).getRuleSet().getMicRule(rule).getCFVector(l);
				}
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













