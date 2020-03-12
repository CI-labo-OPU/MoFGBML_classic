package fgbml.multilabel.binary_relevance;

import fuzzy.SingleRuleSet;

public class Output_BR_multi {

	public String outputPittsburgh(BRpittsburgh individual) {
		String ln = System.lineSeparator();
		String strs = "";
		String str = "";

		int objectiveNum = individual.getObjectiveNum();

		//Header
		str = "id";
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
		strs += str + ln;

		//id
		str = String.valueOf(0);
		//fitness
		for(int o = 0; o < objectiveNum; o++) {
			str += "," + individual.getFitness(o);
		}
		//rank
		str += "," + individual.getRank();
		//crowding distance
		str += "," + individual.getCrowding();
		//Exact-Match for Dtra
		str += "," + individual.getAppendix(0);
		//F-measure for Dtra
		str += "," + individual.getAppendix(1);
		//Recall for Dtra
		str += "," + individual.getAppendix(2);
		//Precision for Dtra
		str += "," + individual.getAppendix(3);
		//Hamming Loss for Dtra
		str += "," + individual.getAppendix(4);
		//Exact-Match for Dtst
		str += "," + individual.getAppendix(5);
		//F-measure for Dtst
		str += "," + individual.getAppendix(6);
		//Recall for Dtst
		str += "," + individual.getAppendix(7);
		//Precision for Dtst
		str += "," + individual.getAppendix(8);
		//Hamming Loss for Dtst
		str += "," + individual.getAppendix(9);
		//ruleNum
		str += "," + individual.getBRruleset().getRuleNum();
		//ruleLength
		str += "," + individual.getBRruleset().getRuleLength();

		strs += str + ln;

		return strs;
	}

	public String outputRuleSet(BRpittsburgh individual) {
		String ln = System.lineSeparator();
		String row = "***************************************";
		String hyphen = "---";

		String strs = "";
		String str = "";

		int Ndim = individual.getNdim();
		int Cnum = individual.getBRruleset().getCnum();
		int objectiveNum = individual.getObjectiveNum();

		for(int c = 0; c < Cnum; c++) {
			SingleRuleSet ruleSet = individual.getBRruleset().getBRrulesets(c);
			int ruleNum = ruleSet.getRuleNum();

			strs += row + ln;
			strs += "label_" + c + ln;
			strs += "ruleNum: " + ruleNum + ln;
			strs += hyphen + ln;

			//Rules
			for(int rule = 0; rule < ruleNum; rule++) {
				//id
				str = "Rule_" + String.format("%02d", rule) + ":";
				//rule
				for(int n = 0; n < Ndim; n++) {
					str += " " + String.format("%2d", ruleSet.getMicRule(rule).getRule(n));
				}
				//class
				str += ", " + "Class:";
				str += " " + ruleSet.getMicRule(rule).getConc();
				//CF
				str += ", " + "CF:";
				str += " " + ruleSet.getMicRule(rule).getCf();

				//fitness
				str += ", " + "Fitness: " + ruleSet.getMicRule(rule).getFitness();

				strs += str + ln;
			}
			strs += row + ln;
			strs += "" + ln;
		}

		return strs;
	}

}
