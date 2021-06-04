# MoFGBML
This is an eclipse project for MoFGBML.

---
## Execution
Any algorithm is executed in ```main()``` in "Main.java".  
If you want to select experimented algorithm, you should set the suitable parameter ```experimentName``` in "setting.properties".

Almost parameters can be set in two .properties files, but the main function requires several arguments.  
The following parameters are required as command line arguments:

```java : main(String[] args)
// Current Directory (for loading .properties files)
String currentDir = args[0];
// The name of .properties file for Consts.java
String constsSource = args[1];
// The name of .properties file for Setting.java
String settingSource = args[2];

// The name of used dataset
String dataName = args[3];
// The number of used CPU cores (with ForkJoinPool class)
int parallelCores = Integer.parseInt(args[4]);
// The name of root directory for results
String saveDir = args[5];

// The index of MOP (Multiobjective Optimization Problem)
int mopNo = Integer.parseInt(args[6]);
```

We exported this MoFGBML project into a jar file "MoFGBML.jar". Also, we placed the jar file, .properties files, and dataset/iris/ in the "JAR" directory. At first, please try the jar file and verify the execution.

### Execution Procedure

```bash
# 1. Move to /JAR/ directory
MoFGBML/
$ cd JAR

# 2. Execute JAR file
MoFGBML/JAR/
$ Java -jar MoFGBML.jar ./ consts setting iris 5 TEST 1
```

If you executed above commands, the computational experiment will be started and the arguments will be set as follows:

```java : Example of setting command line arguments
// This parameter will be required when this project is exported as JAR file and the JAR file is executed.
String currentDir = "./";
// "consts.properties" is placed
String constsSource = "consts";
// "setting.properties" is placed
String settingSource = "setting";

// Iris dataset will be used
String dataName = "iris";
// Five CPU cores will be used by "ForkJoinPool" class
int parallelCores = 5;
// "/result/TEST/" will be made and results will be stored at "/result/TEST/iris-yyyyMMdd-HHmm/"
String saveDir = "TEST";

// MOP1 (mofgbml.MOP1.java) will be set
int mopNo = 1;
```

---

## Several Significant Parameters

### int : emoType (in setting.properties)
It selects the EMOA (evolutionary multi-objective optimization algorithm). Each number is assigned for an EMOA as follows:
 - 0: NSGA-II
 - 1: MOEA/D with Weighted-Sum function
 - 2: MOEA/D with Tchebycheff function
 - 3: MOEA/D with PBI function
 - ~4: MOEA/D with Inverted PBI function~
 - 5: MOEA/D with AOF (Accuracy-Oriented scalarizin Function)

---

## Main Structure

### main package
The main codes for execution are placed at "main" package.
The main codes are:
 + Main.java : The main class.
 + Consts.java : The constant values are defined in this class.
 + Setting.java : The experimental parameters are defined in this class.
 + Experiment.java : An interface code for computational experiments.

The experimental settings can be defined in ".properties" files.  
The Consts.class loads "consts.properties" and the Setting.class loads "setting.properties".  
If you want to set experimental parameters, you can set the values in ".properties" files.  

### fgbml package
This package is for FGBML algorithms. It includes individual classes (Michigan and Pittsburgh types) based on genetics-based machine learning algorithms, and sub-packages for several FGBML algorithms (e.g., Multi-objective FGBML).

This package already includes an "mofgbml" package. The sub-package is for MoFGBML algorithm. The main code is "MoFGBML.java" implementing an interface "Experiment.java". If you want to experiment MoFGBML algorithm, please set the parameter ```experimentName = MoFGBML``` in "setting.properties". The parameter is referred in "Main.java" and it will call the ```startExperiment()``` function in the interface "Experiment.java".
