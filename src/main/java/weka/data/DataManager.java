package main.java.weka.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.java.weka.profile.RunProfile;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

/* Manage all the data part for the process
 * 		- CSV creation/reading
 * 		- .ARFF creation/reading
 * */

public class DataManager {
	private List<String> fileLines;
	private String projPath;
	private RunProfile profile;
	
	
	public DataManager(List<String> lines, String path) {
		this.fileLines = lines;
		this.projPath = path;
	}
	
	
	public void setProfile(RunProfile profile) {
		this.profile = profile;
	}
	
	
	public RunProfile getProfile() {
		return this.profile;
	}
	
	/** Increase the training set, adding the next release
	 * 
	 * @param currRel: release to read
	 * @param index: index in the fileLines List
	 * @param mode: if true, the file is written in append mode 
	 * @param header: the header of the file
	 * @param fileConst: the second part of the file name, used to discrimate train and test set
	 * */
	public int writeOnFile(String currRel, int index, boolean mode, String header, String fileConst)  {
		var trainSet = new File(this.projPath + fileConst);
		
		// write on file in append mode
		try (var fw = new FileWriter(trainSet.getAbsoluteFile(), mode);
				var bw = new BufferedWriter(fw)){
			if(header != null)
				bw.append(header + "\n");
			String line;
			while(this.fileLines.get(index).contains(currRel) && index < this.fileLines.size()-1) {
				line = this.fileLines.get(index);
				bw.append(line+"\n"); // appends the next line
				index++;
			}
			return index;
		}catch (FileNotFoundException e) {
			Logger.getLogger("LAB").log(Level.WARNING, "File not found\n");
		} catch (IOException e) {
			Logger.getLogger("LAB").log(Level.WARNING, e.getCause().getMessage(), e.getMessage());
		}
		return index;
	}
	
	
	/** Convert a .csv file into an ARFF file 
	 * 
	 * @param fullName: the .csv file name of the file to convert
	 * */
	public void csvToArff(String fullName) throws IOException {
		deleteIfExists(fullName.substring(0, fullName.length()-3) + "arff");
		var loader = new CSVLoader();
		loader.setSource(new File(fullName));
		Instances data = loader.getDataSet();
		
		var saver = new ArffSaver();
		saver.setInstances(data);
		saver.setFile(new File(fullName.substring(0, fullName.length()-3) + "arff"));
		saver.writeBatch();
		
		List<String> lines = Files.readAllLines(
				Paths.get(fullName.substring(0, fullName.length()-3) + "arff"));
		for(String line : lines) {
			if(line.contains("@attribute buggyness")) {
				lines.set(lines.indexOf(line), "@attribute buggyness {yes,no}");
			}
		}
		Files.write(Paths.get(fullName.substring(0, fullName.length()-3) + "arff"),
		lines, StandardCharsets.UTF_8);
	}
	
	/** Write a new line in the output .csv file 
	 * 
	 * @param projName: name of the project
	 * @param nTrain: # of training release considered in the run
	 * @param eval: Evaluation instance, containing the results for the classifier
	 * @param classifier: name of the classifier
	 * @param append: if true, the file is written in append mode
	 * */
	public void writeOutput(String projName, int nTrain, Evaluation eval, 
			String classifier, boolean append) {
		var outputFile = new File(this.projPath + " classification.csv");
		var sb = new StringBuilder();
		
		try (var fw = new FileWriter(outputFile.getAbsoluteFile(), append);
				var bw = new BufferedWriter(fw)){
			
			List<Double> percList = this.getPercentages(projName);
			sb.append(projName + "," + nTrain + ","+ percList.get(0) + ","
					+ percList.get(1) + ","+percList.get(2)+","
					+classifier+","+this.profile.getSampling()+","+this.profile.getSelection()+","
					+this.profile.getSensitive()+","+eval.numTruePositives(0)+","+
					eval.numFalsePositives(0)+","+eval.numTrueNegatives(0)+","+
					eval.numFalseNegatives(0)+","+eval.precision(0)+","+
					eval.recall(0)+","+eval.areaUnderROC(0)+","+eval.kappa()+"\n");
			bw.append(sb.toString());
		}catch (FileNotFoundException e) {
			Logger.getLogger("LAB").log(Level.WARNING, "Cannot find the file\n");
		} catch (IOException e) {
			Logger.getLogger("LAB").log(Level.WARNING, e.getCause().getMessage(), e.getMessage());
		}
	}
	
	
	/** Simply write an header line on the outèut .csv file 
	 * 
	 * @param append: true if the file is written in append mode
	 * */
	public void writeHeader(boolean append) {
		var outputFile = new File(this.projPath + " classification.csv");
		var header = "Dataset,#TrainingRelease,%training (data on training / total data),"
				+ "%Defective in training,%Defective in testing,classifier,balancing,"
				+ "Feature Selection,Sensitivity,TP,FP,TN,FN,Precision,Recall,AUC,Kappa"+"\n";
		
		try (var fw = new FileWriter(outputFile.getAbsoluteFile(), append);
				var bw = new BufferedWriter(fw)){
			bw.append(header); //write an header for each run
		}catch (FileNotFoundException e) {
			Logger.getLogger("LAB").log(Level.WARNING, "Cannot find the file\n");
		} catch (IOException e) {
			Logger.getLogger("LAB").log(Level.WARNING, e.getCause().getMessage(), e.getMessage());
		}
	}
	
	
	/** Compute 3 percentage that will be written in the output .csv file
	 * 
	 * @param projName: name of the project
	 * */
	private List<Double> getPercentages(String projPath){
		List<Double> percList = new ArrayList<>();
		int totLines = this.fileLines.size()-1;
		
		try {
			List<String> trainLines = Files.readAllLines(Paths.get(projPath + " train.csv"));
			List<String> testLines = Files.readAllLines(Paths.get(projPath + " test.csv"));
			
			double trainPerc = ((double)trainLines.size()-1)/totLines;
			percList.add(trainPerc);
			
			var defects = 0;
			for(var i = 0; i < trainLines.size();i++) {
				if(trainLines.get(i).contains("yes"))
					defects+=1;
			}
			double trainDefPerc = (double)defects/totLines;
			percList.add(trainDefPerc);
			
			defects = 0;
			for(var j = 0; j < testLines.size();j++) {
				if(testLines.get(j).contains("yes"))
					defects+=1;
			}
			double testDefPerc = (double)defects/totLines;
			percList.add(testDefPerc);
			
		} catch (IOException e) {
			Logger.getLogger("DM").log(Level.SEVERE, "Error while calculating percetage");
		}
		return percList;
	}
	
	/** Check if a file already exists, if so delete it
	 * 
	 * @param fileName: path of the file to delete
	 * */
	private void deleteIfExists(String fileName) {
		var file = new File(fileName);
		try {
			Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			Logger.getLogger("CSV_PROD").log(Level.SEVERE, "deleteIfExists(): error while deleting"
					+ "file");
		}
	}
}
