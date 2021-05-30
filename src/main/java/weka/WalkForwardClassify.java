package main.java.weka;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.java.jira.RetrieveVersions;
import main.java.weka.data.Configurer;
import main.java.weka.data.DataManager;
import main.java.weka.profile.RunProfile;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

/* Predict the buggyness of a given project, using the "walk forward" technique
 * and a set of classifier 
 * */

public class WalkForwardClassify {
	private List<String> fileLines; // keeps all the lines of the file
	private String projPath;
	private String pName;
	
	private List<String> versList;
	private RetrieveVersions rv;
	private DataManager dm;
	private Configurer conf;
	
	private int trainIndex = 1;
	private String[] sampleList = {"NONE", "OVER", "UNDER", "SMOTE"};
	private String[] costSensEnum = {"NONE", "THRESH", /*"SENS"*/};
	
	private int nTrain = 0;
	
	
	public WalkForwardClassify(String projName, String dir) throws IOException {
		this.pName = projName;
		this.projPath = dir+projName;
		
		//get the lines of .csv file once for all
		fileLines = Files.readAllLines(Paths.get(projName + " metrics.csv"));
		this.versList = new ArrayList<>();
		
		// get the release once for all
		rv = new RetrieveVersions();
		this.versList = rv.getVersionNames(this.pName, 0.5f);
		
		this.dm = new DataManager(this.fileLines, dir+projName);
		this.conf = new Configurer();
	}
	
	
	/* Perform a complete run for each Profile for the given project*/
	public void completeRun() throws Exception{
		List<Boolean> selectionArray = new ArrayList<>();
		selectionArray.add(false);
		selectionArray.add(true);
		List<RunProfile> profiles= new ArrayList<>();
		
		// huge 3 for cycle to produce all combination
		for(Boolean sel : selectionArray) {
			for(String sample : this.sampleList) {
				for(String costSens : this.costSensEnum) {
					var newProfile = new RunProfile(sel, sample, costSens);
					profiles.add(newProfile);
				}
			}
		}
		
		//run for each RunProfile
		System.out.println("Starting:\n");
		this.dm.writeHeader(false);
		for(RunProfile prof : profiles) {
			this.dm.setProfile(prof);
			this.conf.setProfile(prof);
			
			this.computeMetrics(prof);
		}
	}
	
	
	/** Use weka API to compute metrics and predictions on the dataset for the
	 * given project
	 * 
	 *  @param profile: the RunProfile instance for this run
	*/
	public void computeMetrics(RunProfile profile) throws Exception {
		
		// compute the first train and test set
		System.out.println("First iteration \n");
		this.nTrain++;
		this.runClassifiers(false, this.fileLines.get(0), 0, profile);
		
		for(var i = 1; i < this.versList.size()-2; i++) {
			System.out.println("Iteration "+this.nTrain);
			this.nTrain++;
			this.runClassifiers(true, null, i, profile);
		}
		
		//reset the indexes
		this.trainIndex = 1;
		this.nTrain = 0;
	}
	
	
	/** Uses the data collected in the .csv files to train and then test
	 * some classifiers
	 * 
	 * @param append: true if the training set file has to be written in append mode
	 * @param header: the header of the csv file
	 * @param currVerdIndex: the index of the current version, to access the List of version
	 * @param profile: the RunProfile instance for this run
	 * */
	private void runClassifiers(boolean append, String header, int currVersIndex, 
			RunProfile profile) 
			throws Exception {
		var testConst = " test.csv";
		var trainConst = " train.csv";
		var testIndex = 0;
		
		// write the instances on the .csv files and convert them into .arff files
		this.trainIndex = this.dm.writeOnFile(this.versList.get(currVersIndex), this.trainIndex, 
				append, header, trainConst);
		testIndex = this.trainIndex; // the test index starts on the next release
		this.dm.writeOnFile(this.versList.get(currVersIndex+1), testIndex, false, 
				this.fileLines.get(0), testConst);
		this.dm.csvToArff(this.projPath + testConst);
		this.dm.csvToArff(this.projPath + trainConst);
		
		//get the Instances (weka class)
		List<Instances> instList = this.conf.initializeTrainAndTest(this.projPath+" test.arff", 
				this.projPath+" train.arff");
		
		// check for cost sensitive classification
		if(profile.getSensitive().equals("THRESH")) {
			this.classifyWithCostSensitive(instList);
		}
		else if(profile.getSensitive().equals("SENS")) {
			//TODO: cerca di capire come cazzo si implementa sta cosa della threshold
		}
		else {
			this.classify(instList);
		}
	}
	
	
	/** Run the 3 models using cost sensitive classification 
	 * 
	 * @param instList: list containing training set (first element) 
	 * and testing set (second element)
	 * */
	public void classifyWithCostSensitive(List<Instances> instList) {
		// initialize classifiers
		var naiveBayes = new NaiveBayes();
		var ibk = new IBk();
		var randomForest = new RandomForest();
		
		var cm = this.getCostMatrix(10, 1);
		var nbCs = new CostSensitiveClassifier();
		var ibkCs = new CostSensitiveClassifier();
		var rfCs = new CostSensitiveClassifier();
					
		// set the classifiers and cost matrix
		nbCs.setClassifier(naiveBayes);
		nbCs.setCostMatrix(cm);
		ibkCs.setClassifier(ibk);
		ibkCs.setCostMatrix(cm);
		rfCs.setClassifier(randomForest);
		rfCs.setCostMatrix(cm);
		
		//build the classifiers
		try {
			nbCs.buildClassifier(instList.get(0));
			ibkCs.buildClassifier(instList.get(0));
			rfCs.buildClassifier(instList.get(0));
			
			var nbEval = new Evaluation(instList.get(1), nbCs.getCostMatrix());
			var ibkEval = new Evaluation(instList.get(1), ibkCs.getCostMatrix());
			var rfEval = new Evaluation(instList.get(1), rfCs.getCostMatrix());
			
			nbEval.evaluateModel(nbCs, instList.get(1));
			ibkEval.evaluateModel(ibkCs, instList.get(1));
			rfEval.evaluateModel(rfCs, instList.get(1));
			
			//write values on file
			this.dm.writeOutput(this.projPath,this.nTrain, nbEval, "NaiveBayes", true);
			this.dm.writeOutput(this.projPath,this.nTrain, ibkEval, "IBk", true);
			this.dm.writeOutput(this.projPath,this.nTrain, rfEval, "Random Forest", true);
		} catch (Exception e) {
			Logger.getLogger("WFC").log(Level.SEVERE, /*"Error while running cost "
					+ "sensitive classification"*/e.getMessage());
		}
	}
	
	
	/** Create a new cost matrix for cost sensitive evaluation
	 * 
	 * @param fnWeight: weight of the false negative 
	 * @param fpWeight: weight of the false positive
	 * */
	private CostMatrix getCostMatrix(double fnWeight, double fpWeight) {
		var cm = new CostMatrix(2);
		cm.setCell(0, 0, 0.0);
		cm.setCell(0, 1, fnWeight);
		cm.setCell(1, 0, fpWeight);
		cm.setCell(1, 1, 0.0);
		return cm;
	}
	
	
	/** Run the 3 classifiers without any sensitive learning mechanism 
	 * 
	 * @param instList: list containing training set (first element) 
	 * and testing set (second element)
	 * @throws Exception 
	 * */
	private void classify(List<Instances> instList) throws Exception {
		// initialize classifiers
		var naiveBayes = new NaiveBayes();
		var ibk = new IBk();
		var randomForest = new RandomForest();
				
		//build the classifiers
			naiveBayes.buildClassifier(instList.get(0));
			ibk.buildClassifier(instList.get(0));
			randomForest.buildClassifier(instList.get(0));
					
			var nbEval = new Evaluation(instList.get(1));
			var ibkEval = new Evaluation(instList.get(1));
			var rfEval = new Evaluation(instList.get(1));
					
			nbEval.evaluateModel(naiveBayes, instList.get(1));
			ibkEval.evaluateModel(ibk, instList.get(1));
			rfEval.evaluateModel(randomForest, instList.get(1));
					
			//write values on file
			this.dm.writeOutput(this.projPath,this.nTrain, nbEval, "NaiveBayes", true);
			this.dm.writeOutput(this.projPath,this.nTrain, ibkEval, "IBk", true);
			this.dm.writeOutput(this.projPath,this.nTrain, rfEval, "Random Forest", true);
	}
	
	
	public static void main(String[] args) throws Exception {
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		var pName = "BOOKKEEPER";
		var dir = "bookkeeper-files/";
		var mwc = new WalkForwardClassify(pName, dir);
		mwc.completeRun();
	}
}
