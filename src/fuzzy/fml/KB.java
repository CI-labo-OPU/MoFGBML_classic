package fuzzy.fml;

import java.io.File;
import java.util.ArrayList;

import data.SingleDataSetInfo;
import fuzzy.FuzzyPartitioning;
import fuzzy.fml.params.HomoTriangle_2_3_4_5;
import fuzzy.fml.params.HomoTriangle_2_3_4_5_6_7;
import fuzzy.fml.params.HomoTriangle_2_3_4_5_6_7_8_9;
import fuzzy.fml.params.HomoTriangle_3;
import jfml.FuzzyInferenceSystem;
import jfml.JFML;
import jfml.knowledgebase.KnowledgeBaseType;
import jfml.knowledgebase.variable.FuzzyVariableType;
import jfml.term.FuzzyTermType;
import main.Consts;

/**
 * Fuzzy Markup LanguageのKnowledgeBaseを扱うクラス
 *
 */

public class KB {
	// ************************************************************
	//FML
	float domainLeft = 0f;
	float domainRight = 1f;

	int Ndim = 1;
	/**
	 * [Ndim][fuzzySetNum]
	 */
	FuzzySet[][] FSs;

	// ************************************************************
	public KB() {}

	// ************************************************************

	public void classEntropyInit(SingleDataSetInfo tra, int K, double F) {
		ArrayList<ArrayList<double[]>> trapezoids = FuzzyPartitioning.startPartition(tra, K, F);

		this.Ndim = trapezoids.size();

		FSs = new FuzzySet[Ndim][];
		float[] dontCare = new float[] {0f, 1f};

		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			float[][] points = new float[trapezoids.get(dim_i).size()][4];
			for(int k = 0; k < points.length; k++) {
				for(int param_i = 0; param_i < 4; param_i++) {
					points[k][param_i] = (float)trapezoids.get(dim_i).get(k)[param_i];
				}
			}

			FSs[dim_i] = new FuzzySet[points.length + 1];
			FSs[dim_i][0] = new FuzzySet("0", FuzzyTermType.TYPE_rectangularShape, dontCare);
			for(int k = 0; k < points.length; k++) {
				FSs[dim_i][k+1] = new FuzzySet( String.valueOf(k+1),
												FuzzyTermType.TYPE_trapezoidShape,
												points[k]);
			}
		}

	}

	public void threeTriangle(int Ndim) {
		this.Ndim = Ndim;

		FSs = new FuzzySet[Ndim][];
		float[] dontCare = new float[] {0f, 1f};
		float[][] params = HomoTriangle_3.getParams();

		for(int i = 0; i < Ndim; i++) {
			FSs[i] = new FuzzySet[params.length + 1];
			FSs[i][0] = new FuzzySet("0", FuzzyTermType.TYPE_rectangularShape, dontCare);
			for(int j = 0; j < params.length; j++) {
				FSs[i][j+1] = new FuzzySet(String.valueOf(j+1), FuzzyTermType.TYPE_triangularShape, params[j]);
			}
		}
	}

	/**
	 * 2-"Consts.MAX_FUZZY_DIVIDE_NUM"分割の等分割三角型ファジィ集合 + Don't Careの15種を全attributeに定義<br>
	 * @param _Ndim
	 */
	public void homogeneousInit(int Ndim) {
		this.Ndim = Ndim;

		FSs = new FuzzySet[Ndim][];

		float[] dontCare = new float[] {0f, 1f};
		float[][] params;
		switch(Consts.MAX_FUZZY_DIVIDE_NUM) {
		case 5:
			params = HomoTriangle_2_3_4_5.getParams();
			break;
		case 7:
			params = HomoTriangle_2_3_4_5_6_7.getParams();
			break;
		case 9:
			params = HomoTriangle_2_3_4_5_6_7_8_9.getParams();
			break;
		default:
			params = HomoTriangle_2_3_4_5.getParams();
			break;
		}

		for(int i = 0; i < Ndim; i++) {
			FSs[i] = new FuzzySet[params.length + 1];
			//Don't Care
			FSs[i][0] = new FuzzySet("0", FuzzyTermType.TYPE_rectangularShape, dontCare);

			for(int j = 0; j < params.length; j++) {
				FSs[i][j+1] = new FuzzySet(String.valueOf(j+1), FuzzyTermType.TYPE_triangularShape, params[j]);
			}
		}
	}

	/**
	 * <h1>与えられたparamsからファジィ集合を定義する．</h1>
	 * @param Ndim
	 * @param _params
	 */
	public void paramInit(int Ndim, float[][] _params) {
		this.Ndim = Ndim;

		FSs = new FuzzySet[Ndim][];

		float[] dontCare = new float[] {0f, 1f};
		float[][] params = _params;

		for(int i = 0; i < Ndim; i++) {
			FSs[i] = new FuzzySet[params.length + 1];
			//Don't Care
			FSs[i][0] = new FuzzySet("0", FuzzyTermType.TYPE_rectangularShape, dontCare);

			for(int j = 0; j < params.length; j++) {
				FSs[i][j+1] = new FuzzySet(String.valueOf(j+1), FuzzyTermType.TYPE_triangularShape, params[j]);
//				FSs[i][j+1] = new FuzzySet(String.valueOf(j+1), FuzzyTermType.TYPE_gaussianShape, params[j]);
			}
		}
	}

	/**
	 * XMLファイルを読み込んでFSs[][]を初期化するメソッド．<br>
	 * @param fileName String : 読み込むXMLファイルのパス
	 */
	public void inputFML(String fileName) {
		//Load XML file.
		File fml = new File(fileName);
		FuzzyInferenceSystem fs = JFML.load(fml);
		KnowledgeBaseType kb = fs.getKnowledgeBase();

		//#of Feature
		Ndim = kb.getKnowledgeBaseVariables().size();
		FSs = new FuzzySet[Ndim][];
		domainLeft = ((FuzzyVariableType)kb.getKnowledgeBaseVariables().get(0)).getDomainleft();
		domainRight = ((FuzzyVariableType)kb.getKnowledgeBaseVariables().get(0)).getDomainright();

		//#s of Fuzzy Sets for Each Feature.
		int[] termNum = new int[Ndim];

		for(int i = 0; i < Ndim; i++) {
			//Get Name of Variable
			String variableName = kb.getKnowledgeBaseVariables().get(i).getName();

			termNum[i] = ((FuzzyVariableType)kb.getVariable(variableName)).getTerms().size();
			FSs[i] = new FuzzySet[termNum[i]];
			for(int j = 0; j < termNum[i]; j++) {
				//Get Name of Fuzzy Set
				String termName =  ((FuzzyVariableType)kb.getVariable(variableName)).getTerms().get(j).getName();
				int shapeType = ((FuzzyTermType)kb.getVariable(variableName).getTerm(termName)).getType();
				float[] params = ((FuzzyTermType)kb.getVariable(variableName).getTerm(termName)).getParam();

				//Make Fuzzy Set
				FSs[i][j] = new FuzzySet(termName, shapeType, params);
			}
		}
	}

	public void outputFML(String fileName) {
		FuzzyInferenceSystem fs = new FuzzyInferenceSystem();
		KnowledgeBaseType kb = new KnowledgeBaseType();
		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			float domainLeft = 0f;
			float domainRight = 1f;
			FuzzyVariableType variable = new FuzzyVariableType(String.valueOf(dim_i), domainLeft, domainRight);
			for(int f = 0; f < FSs[dim_i].length; f++) {
				variable.addFuzzyTerm(FSs[dim_i][f].getTerm());
			}
			kb.addVariable(variable);
		}
		fs.setKnowledgeBase(kb);
		File fml = new File(fileName);
		JFML.writeFSTtoXML(fs, fml);
	}

	public double calcMembership(int attribute, int fuzzySet, double x) {
		double ans = FSs[attribute][fuzzySet].calcMembership(x);
		return ans;
	}

	/**
	 * [Ndim][fuzzySetNum]
	 */
	public FuzzySet[][] getFSs(){
		return this.FSs;
	}

	public FuzzySet[] getFSs(int dim) {
		return this.FSs[dim];
	}

}
