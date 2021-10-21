package main;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

//各種定数 定義クラス
/**
 * コマンドライン引数として，
 * カレントディレクトリ
 * setting.propertiesのファイル名
 * を与える．<br>
 * <br>
 * 例: > Java -jar xx.jar ./ consts<br>
 * <br>
 * setConsts()メソッドに，以上の引数を与えてプロパティファイルを読み込む．
 *
 *
 */
public class Consts {

	//OS
	public static int WINDOWS = 0;	/**/ //windows
	public static int UNIX = 1;	/**/ //unix

	//Experiment's Parameters - 実験設定パラメータ
	public static boolean IS_RANDOM_PATTERN_SELECT = false;	//ランダムなパターンで組む
	public static boolean IS_NOT_EQUAL_DIVIDE_NUM = false;	//部分個体群とデータ分割数を一緒にしない
	public static boolean IS_ALL_MIGLATION = false;	//true: 各島の最良個体を全島で共有する, false: 各島の最良個体を隣の島に移住

	//GA Parameters - GA用設定パラメータ
	/** for optimizer */
	public static int MINIMIZE = 1;
	/** for optimizer */
	public static int MAXIMIZE = -1;
	/** for Subdivision */
	public static double SUBRATE = 0.8;
	/** 出力された部分データセットを用いるかどうか */
	public static boolean LOAD_SUBDATASET = false;

	//Parallel Parameters - 並列用パラメータ
	public static boolean IS_RULESETS_SORT = false;	//評価の際にルール数でソートするかどうか
	public static boolean IS_RULE_PARALLEL = true;	//ルールで並列化するかどうか（データのパターンでなく）
	public static boolean IS_ISLAND_TIME = false;	//サーバ1大の時に各島の時間も測る．(評価だけ並列の時)

	//GBML's parameters
	/** don't careにしない条件部の数 */
	public static int ANTECEDENT_LEN = 5;	/**/
	/** don't care適応確率 */
	public static double DONT_CARE_RT = 0.8;	/**/
	/** don't careを確率で行う */
	public static boolean IS_PROBABILITY_DONT_CARE = false;
	/** ミシガン操作時にルールを追加する（置き換えでなく） */
	public static boolean DO_ADD_RULES = true;
	/** ES型個体群更新戦略 */
	public static boolean IS_ES_UPDATE = false;

	/** Michigan適用確率 */
	public static double RULE_OPE_RT = 0.5;/**/
	/** Michigan交叉確率 */
	public static double RULE_CROSS_RT = 0.9;/**/
	/** ルール入れ替え割合 */
	public static double RULE_CHANGE_RT = 0.2;/**/
	/** Michigan型GAの際のルール生成数 (true: 1, false: RULE_CHANGE_RT) */
	public static boolean RATE_OR_ONLY = false;

	public static boolean DO_LOG_PER_LOG = true;	//ログでログを出力
	/** Pittsburgh交叉確率 */
	public static double RULESET_CROSS_RT = 0.9;/**/

	//NSGA-II's Parameters
	public static int NSGA2 = 0;	//NSGA-IIの番号
	public static int OBJECTIVE_DEGREES = 0;	//目的関数の回転度
	public static boolean DO_CD_NORMALIZE = false;	//Crowding Distance を正規化するかどうか
	public static boolean HAS_PARENT = false;

	//EMO's parameters
	public static int SECOND_OBJECTIVE_TYPE = 0;	//2目的目, 0:rule, 1:length, 2:rule * length, 4:length/rule

	//MOEA/D's parameters
	public static int[] VECTOR_DIVIDE_NUM = new int[] {99};			//分割数
	public static double MOEAD_ALPHA = 0.9;			//参照点のやつ
	public static double MOEAD_THETA = 5.0;			//シータ
	public static boolean IS_NEIGHBOR_SIZE = false;	//近傍サイズ false:個数指定, true:パーセント指定
	public static int NEIGHBOR_SIZE_RT = 10;			//近傍サイズ%
	public static int NEIGHBOR_SIZE = 10;			//近傍サイズ[個]
	public static int SELECTION_NEIGHBOR_NUM = 5;	//選択近傍サイズ
	public static int UPDATE_NEIGHBOR_NUM = 10;		//更新近傍サイズ

	/** weighted sum */
    public static int WS  = 1;
    /** Tchebycheff */
    public static int TCHEBY = 2;
    /** PBI */
    public static int PBI = 3;
    /** InvertedPBI */
    public static int IPBI = 4;
    /** Accuracy Oriented Function */
    public static int AOF = 5;
    /** 2019.12. Trial Scalarize Function */
    public static int AOF2 = 6;
    public static boolean IS_AOF_VECTOR_INT = true;	//AOFのベクトルを書くルール数で固定する（これをすると島モデルやりづらい）
	public static boolean DO_NORMALIZE = false;	//正規化するかどうか
    public static boolean IS_BIAS_VECTOR = false;	//false: NObiasVector, true: biasVector

    public static double IS_FIRST_IDEAL_DOWN = 0.0;	//１目的目のみ下に動かす．（やらない場合は０に）
    public static boolean IS_WS_FROM_NADIA = false;	//WSをナディアポイントから

	//Fuzzy System's parameters
    /**
     * <h1>ファジィ集合の初期化方法</h1>
     * <h1>The methodology of knowledge base (fuzzy sets) initilization</h1>
     * 0: 2-5分割 homogeneous triangle fuzzy partitions.<br>
     * 1: Input XML file<br>
     * 2: Inhomogeneous<br>
     */
    public static int FUZZY_SET_INITIALIZE = 0;
    /** Input XML file name */
    public static String XML_FILE = "a.xml";
    /** #of Inhomogeneous Partitions */
    public static int PARTITION_NUM = 5;
    /** Inhomogeneous Fuzzyfying Grade */
	public static double FUZZY_GRADE = 0.5;
	public static int FUZZY_SET_NUM = 14;			//ファジィ集合の種類数
	/** 初期ル―ル数 */
	public static int INITIATION_RULE_NUM = 30;/**/
	public static int MAX_FUZZY_DIVIDE_NUM = 5;	//条件部の分割数の最大値
	/** 1識別器あたりの最大ルール数 */
	public static int MAX_RULE_NUM = 60;/**/
	/** 1識別器あたりの最小ルール数 */
	public static int MIN_RULE_NUM = 1;/**/
	/** ヒューリスティック生成法 */
	public static boolean DO_HEURISTIC_GENERATION = true;
	public static int DC_LABEL = 0;	//don't careを表すファジィ集合ラベル
	public static boolean DO_FUZZY_PARALLEL = false;	//ファジィ計算において並列化を行うかどうか
	public static boolean DO_PREFER_NOCLASS = true;	//足りていないクラスを結論部として持つルールを優先的に生成するかどうか

	//Multi-Label Problem parameters
	public static boolean MULTI_LABEL_PROBLEM = false;	//マルチラベル問題用

	/** 0:各ラベルへのCF平均, 1:各ラベルへのCFベクトル */
	public static int MULTI_CF_TYPE = 1;

	//One Objective Weights
	public static int W1 = 1000;
	public static int W2 = -1;
	public static int W3 = -1;

	//Other parametaers
	/** 表示する世代間隔 */
	public static int PER_SHOW_GENERATION_NUM = 100;/**/
	/** 詳細表示するドット間隔 */
	public static int PER_SHOW_GENERATION_DETAIL = 10;/**/
	public static int WAIT_SECOND = 300000;
	public static int TIME_OUT_TIME = 30000;
	public static int SLEEP_TIME = 1000;

	public static int TRAIN = 0;/**/	//学習用データインデックス
	public static int TEST = 1;/**/	//評価用データインデックス


	//Folders' Name
	public static String ROOTFOLDER = "result";/**/
	public static String RULESET = "ruleset";/**/
	public static String INDIVIDUAL = "individual";/**/
	public static String POPULATION = "population";/**/
	public static String OFFSPRING = "offspring";/**/
	public static String SUBDATA = "subdata";/**/
	public static String VECSET = "vecset";
	public static String SOLUTION = "solution";
	public static String LOGS = "logs";
	public static String LOGS_READABLE = "logs_readable";
	public static String DATA = "data";/**/
	public static String TIMES = "times";/**/
	public static String OTHERS = "write";

	//Mistery Parameters
	public static double IS_CLASS_CLOSS_RATE = 0.0;	//クラス交叉確率(MOEA/D)
	public static boolean IS_DEMOCRACY = false;	//多数決にする

	public static void setConsts(String dir, String source) {

		URLClassLoader urlLoader = null;
		ResourceBundle bundle = null;
		try {
			urlLoader = new URLClassLoader(new URL[] {new File(dir).toURI().toURL()});
			bundle = ResourceBundle.getBundle(source, Locale.getDefault(), urlLoader);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		if(bundle.containsKey("WINDOWS")) { WINDOWS = Integer.parseInt(bundle.getString("WINDOWS")); }
		if(bundle.containsKey("UNIX")) { UNIX = Integer.parseInt(bundle.getString("UNIX")); }
		if(bundle.containsKey("IS_RANDOM_PATTERN_SELECT")) { IS_RANDOM_PATTERN_SELECT = Boolean.parseBoolean(bundle.getString("IS_RANDOM_PATTERN_SELECT")); }
		if(bundle.containsKey("IS_NOT_EQUAL_DIVIDE_NUM")) { IS_NOT_EQUAL_DIVIDE_NUM = Boolean.parseBoolean(bundle.getString("IS_NOT_EQUAL_DIVIDE_NUM")); }
		if(bundle.containsKey("IS_ALL_MIGLATION")) { IS_ALL_MIGLATION = Boolean.parseBoolean(bundle.getString("IS_ALL_MIGLATION")); }
		if(bundle.containsKey("MINIMIZE")) { MINIMIZE = Integer.parseInt(bundle.getString("MINIMIZE")); }
		if(bundle.containsKey("MAXIMIZE")) { MAXIMIZE = Integer.parseInt(bundle.getString("MAXIMIZE")); }
		if(bundle.containsKey("SUBRATE")) { SUBRATE = Double.parseDouble(bundle.getString("SUBRATE")); }
		if(bundle.containsKey("LOAD_SUBDATASET")) { LOAD_SUBDATASET = Boolean.parseBoolean(bundle.getString("LOAD_SUBDATASET")); }
		if(bundle.containsKey("IS_RULESETS_SORT")) { IS_RULESETS_SORT = Boolean.parseBoolean(bundle.getString("IS_RULESETS_SORT")); }
		if(bundle.containsKey("IS_RULE_PARALLEL")) { IS_RULE_PARALLEL = Boolean.parseBoolean(bundle.getString("IS_RULE_PARALLEL")); }
		if(bundle.containsKey("IS_ISLAND_TIME")) { IS_ISLAND_TIME = Boolean.parseBoolean(bundle.getString("IS_ISLAND_TIME")); }
		if(bundle.containsKey("ANTECEDENT_LEN")) { ANTECEDENT_LEN = Integer.parseInt(bundle.getString("ANTECEDENT_LEN")); }
		if(bundle.containsKey("DONT_CARE_RT")) { DONT_CARE_RT = Double.parseDouble(bundle.getString("DONT_CARE_RT")); }
		if(bundle.containsKey("IS_PROBABILITY_DONT_CARE")) { IS_PROBABILITY_DONT_CARE = Boolean.parseBoolean(bundle.getString("IS_PROBABILITY_DONT_CARE")); }
		if(bundle.containsKey("DO_ADD_RULES")) { DO_ADD_RULES = Boolean.parseBoolean(bundle.getString("DO_ADD_RULES")); }
		if(bundle.containsKey("IS_ES_UPDATE")) { IS_ES_UPDATE = Boolean.parseBoolean(bundle.getString("IS_ES_UPDATE")); }
		if(bundle.containsKey("RULE_OPE_RT")) { RULE_OPE_RT = Double.parseDouble(bundle.getString("RULE_OPE_RT")); }
		if(bundle.containsKey("RULE_CROSS_RT")) { RULE_CROSS_RT = Double.parseDouble(bundle.getString("RULE_CROSS_RT")); }
		if(bundle.containsKey("RULE_CHANGE_RT")) { RULE_CHANGE_RT = Double.parseDouble(bundle.getString("RULE_CHANGE_RT")); }
		if(bundle.containsKey("RATE_OR_ONLY")) { RATE_OR_ONLY = Boolean.parseBoolean(bundle.getString("RATE_OR_ONLY")); }
		if(bundle.containsKey("DO_LOG_PER_LOG")) { DO_LOG_PER_LOG = Boolean.parseBoolean(bundle.getString("DO_LOG_PER_LOG")); }
		if(bundle.containsKey("RULESET_CROSS_RT")) { RULESET_CROSS_RT = Double.parseDouble(bundle.getString("RULESET_CROSS_RT")); }
		if(bundle.containsKey("NSGA2")) { NSGA2 = Integer.parseInt(bundle.getString("NSGA2")); }
		if(bundle.containsKey("OBJECTIVE_DEGREES")) { OBJECTIVE_DEGREES = Integer.parseInt(bundle.getString("OBJECTIVE_DEGREES")); }
		if(bundle.containsKey("DO_CD_NORMALIZE")) { DO_CD_NORMALIZE = Boolean.parseBoolean(bundle.getString("DO_CD_NORMALIZE")); }
		if(bundle.containsKey("HAS_PARENT")) { HAS_PARENT = Boolean.parseBoolean(bundle.getString("HAS_PARENT")); }
		if(bundle.containsKey("SECOND_OBJECTIVE_TYPE")) { SECOND_OBJECTIVE_TYPE = Integer.parseInt(bundle.getString("SECOND_OBJECTIVE_TYPE")); }
		if(bundle.containsKey("VECTOR_DIVIDE_NUM")) {
			int num = bundle.getString("VECTOR_DIVIDE_NUM").split(",").length;
			VECTOR_DIVIDE_NUM = new int[num];
			for(int i = 0; i < num; i++) {
				VECTOR_DIVIDE_NUM[i] = Integer.parseInt(bundle.getString("VECTOR_DIVIDE_NUM").split(",")[i]);
			}
		}
		if(bundle.containsKey("MOEAD_ALPHA")) { MOEAD_ALPHA = Double.parseDouble(bundle.getString("MOEAD_ALPHA")); }
		if(bundle.containsKey("MOEAD_THETA")) { MOEAD_THETA = Double.parseDouble(bundle.getString("MOEAD_THETA")); }
		if(bundle.containsKey("IS_NEIGHBOR_SIZE")) { IS_NEIGHBOR_SIZE = Boolean.parseBoolean(bundle.getString("IS_NEIGHBOR_SIZE")); }
		if(bundle.containsKey("NEIGHBOR_SIZE_RT")) { NEIGHBOR_SIZE_RT = Integer.parseInt(bundle.getString("NEIGHBOR_SIZE_RT")); }
		if(bundle.containsKey("NEIGHBOR_SIZE")) { NEIGHBOR_SIZE = Integer.parseInt(bundle.getString("NEIGHBOR_SIZE")); }
		if(bundle.containsKey("SELECTION_NEIGHBOR_NUM")) { SELECTION_NEIGHBOR_NUM = Integer.parseInt(bundle.getString("SELECTION_NEIGHBOR_NUM")); }
		if(bundle.containsKey("UPDATE_NEIGHBOR_NUM")) { UPDATE_NEIGHBOR_NUM = Integer.parseInt(bundle.getString("UPDATE_NEIGHBOR_NUM")); }
		if(bundle.containsKey("WS")) { WS = Integer.parseInt(bundle.getString("WS")); }
		if(bundle.containsKey("TCHEBY")) { TCHEBY = Integer.parseInt(bundle.getString("TCHEBY")); }
		if(bundle.containsKey("PBI")) { PBI = Integer.parseInt(bundle.getString("PBI")); }
		if(bundle.containsKey("IPBI")) { IPBI = Integer.parseInt(bundle.getString("IPBI")); }
		if(bundle.containsKey("AOF")) { AOF = Integer.parseInt(bundle.getString("AOF")); }
		if(bundle.containsKey("AOF2")) { AOF2 = Integer.parseInt(bundle.getString("AOF2")); }
		if(bundle.containsKey("IS_AOF_VECTOR_INT")) { IS_AOF_VECTOR_INT = Boolean.parseBoolean(bundle.getString("IS_AOF_VECTOR_INT")); }
		if(bundle.containsKey("DO_NORMALIZE")) { DO_NORMALIZE = Boolean.parseBoolean(bundle.getString("DO_NORMALIZE")); }
		if(bundle.containsKey("IS_BIAS_VECTOR")) { IS_BIAS_VECTOR = Boolean.parseBoolean(bundle.getString("IS_BIAS_VECTOR")); }
		if(bundle.containsKey("IS_FIRST_IDEAL_DOWN")) { IS_FIRST_IDEAL_DOWN = Double.parseDouble(bundle.getString("IS_FIRST_IDEAL_DOWN")); }
		if(bundle.containsKey("IS_WS_FROM_NADIA")) { IS_WS_FROM_NADIA = Boolean.parseBoolean(bundle.getString("IS_WS_FROM_NADIA")); }
		if(bundle.containsKey("FUZZY_SET_INITIALIZE")) { FUZZY_SET_INITIALIZE = Integer.parseInt(bundle.getString("FUZZY_SET_INITIALIZE")); }
		if(bundle.containsKey("XML_FILE")) { XML_FILE = bundle.getString("XML_FILE"); }
		if(bundle.containsKey("PARTITION_NUM")) { PARTITION_NUM = Integer.parseInt(bundle.getString("PARTITION_NUM")); }
		if(bundle.containsKey("FUZZY_GRADE")) { FUZZY_GRADE = Double.parseDouble(bundle.getString("FUZZY_GRADE")); }


		if(bundle.containsKey("FUZZY_SET_NUM")) {
			if(FUZZY_SET_INITIALIZE == 0) {
				FUZZY_SET_NUM = 14;
			} else {
				FUZZY_SET_NUM = Integer.parseInt(bundle.getString("FUZZY_SET_NUM"));
			}
		}
		if(bundle.containsKey("INITIATION_RULE_NUM")) { INITIATION_RULE_NUM = Integer.parseInt(bundle.getString("INITIATION_RULE_NUM")); }
		if(bundle.containsKey("MAX_FUZZY_DIVIDE_NUM")) { MAX_FUZZY_DIVIDE_NUM = Integer.parseInt(bundle.getString("MAX_FUZZY_DIVIDE_NUM")); }
		if(bundle.containsKey("MAX_RULE_NUM")) { MAX_RULE_NUM = Integer.parseInt(bundle.getString("MAX_RULE_NUM")); }
		if(bundle.containsKey("MIN_RULE_NUM")) { MIN_RULE_NUM = Integer.parseInt(bundle.getString("MIN_RULE_NUM")); }
		if(bundle.containsKey("DO_HEURISTIC_GENERATION")) { DO_HEURISTIC_GENERATION = Boolean.parseBoolean(bundle.getString("DO_HEURISTIC_GENERATION")); }
		if(bundle.containsKey("DC_LABEL")) { DC_LABEL = Integer.parseInt(bundle.getString("DC_LABEL")); }
		if(bundle.containsKey("DO_FUZZY_PARALLEL")) { DO_FUZZY_PARALLEL = Boolean.parseBoolean(bundle.getString("DO_FUZZY_PARALLEL")); }
		if(bundle.containsKey("DO_PREFER_NOCLASS")) { DO_PREFER_NOCLASS = Boolean.parseBoolean(bundle.getString("DO_PREFER_NOCLASS")); }
		if(bundle.containsKey("MULTI_CF_TYPE")) { MULTI_CF_TYPE = Integer.parseInt(bundle.getString("MULTI_CF_TYPE")); }
		if(bundle.containsKey("W1")) { W1 = Integer.parseInt(bundle.getString("W1")); }
		if(bundle.containsKey("W2")) { W2 = Integer.parseInt(bundle.getString("W2")); }
		if(bundle.containsKey("W3")) { W3 = Integer.parseInt(bundle.getString("W3")); }
		if(bundle.containsKey("PER_SHOW_GENERATION_NUM")) { PER_SHOW_GENERATION_NUM = Integer.parseInt(bundle.getString("PER_SHOW_GENERATION_NUM")); }
		if(bundle.containsKey("PER_SHOW_GENERATION_DETAIL")) { PER_SHOW_GENERATION_DETAIL = Integer.parseInt(bundle.getString("PER_SHOW_GENERATION_DETAIL")); }
		if(bundle.containsKey("WAIT_SECOND")) { WAIT_SECOND = Integer.parseInt(bundle.getString("WAIT_SECOND")); }
		if(bundle.containsKey("TIME_OUT_TIME")) { TIME_OUT_TIME = Integer.parseInt(bundle.getString("TIME_OUT_TIME")); }
		if(bundle.containsKey("SLEEP_TIME")) { SLEEP_TIME = Integer.parseInt(bundle.getString("SLEEP_TIME")); }
		if(bundle.containsKey("TRAIN")) { TRAIN = Integer.parseInt(bundle.getString("TRAIN")); }
		if(bundle.containsKey("TEST")) { TEST = Integer.parseInt(bundle.getString("TEST")); }
		if(bundle.containsKey("ROOTFOLDER")) { ROOTFOLDER = bundle.getString("ROOTFOLDER"); }
		if(bundle.containsKey("RULESET")) { RULESET = bundle.getString("RULESET"); }
		if(bundle.containsKey("INDIVIDUAL")) { INDIVIDUAL = bundle.getString("INDIVIDUAL"); }
		if(bundle.containsKey("POPULATION")) { POPULATION = bundle.getString("POPULATION"); }
		if(bundle.containsKey("OFFSPRING")) { OFFSPRING = bundle.getString("OFFSPRING"); }
		if(bundle.containsKey("SUBDATA")) { SUBDATA = bundle.getString("SUBDATA"); }
		if(bundle.containsKey("VECSET")) { VECSET = bundle.getString("VECSET"); }
		if(bundle.containsKey("SOLUTION")) { SOLUTION = bundle.getString("SOLUTION"); }
		if(bundle.containsKey("LOGS")) { LOGS = bundle.getString("LOGS"); }
		if(bundle.containsKey("LOGS_READABLE")) { LOGS_READABLE = bundle.getString("LOGS_READABLE"); }
		if(bundle.containsKey("DATA")) { DATA = bundle.getString("DATA"); }
		if(bundle.containsKey("TIMES")) { TIMES = bundle.getString("TIMES"); }
		if(bundle.containsKey("OTHERS")) { OTHERS = bundle.getString("OTHERS"); }
		if(bundle.containsKey("IS_CLASS_CLOSS_RATE")) { IS_CLASS_CLOSS_RATE = Double.parseDouble(bundle.getString("IS_CLASS_CLOSS_RATE")); }
		if(bundle.containsKey("IS_DEMOCRACY")) { IS_DEMOCRACY = Boolean.parseBoolean(bundle.getString("IS_DEMOCRACY")); }
		if(bundle.containsKey("MULTI_LABEL_PROBLEM")) { MULTI_LABEL_PROBLEM = Boolean.parseBoolean(bundle.getString("MULTI_LABEL_PROBLEM")); }

		bundle = null;
	}

	public String getStaticValues() {
		StringBuilder sb = new StringBuilder();
		String sep = System.lineSeparator();
		sb.append("Class: " + this.getClass().getCanonicalName() + sep);
		sb.append("Settings: " + sep);
		for(Field field : this.getClass().getDeclaredFields()) {
			try {
				field.setAccessible(true);
				sb.append(field.getName() + " = " + field.get(this) + sep);
			} catch(IllegalAccessException e) {
				sb.append(field.getName() + " = " + "access denied" + sep);
			}
		}
		return sb.toString();
	}
}
