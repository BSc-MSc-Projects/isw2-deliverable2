package main.java.weka.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import main.java.weka.profile.RunProfile;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.Remove;

/* Configure weka Instances and Classifiers */

public class Configurer {
	private RunProfile currProf;
	private int[] filter = {3,4,5,6,7,8,9,10,11,12};
	private String[] osOpts = {"-B", "1.0", "-Z"};
	private String[] usOpts = {"-M", "1.0"};
	
	
	public void setProfile(RunProfile prof) {
		this.currProf = prof;
	}
	
	
	public RunProfile getProfile() {
		return this.currProf;
	}
	
	/** Use weka api initialize training and testing set
	 * 
	 * @param testSet: name of the .arff file containing testing set instances
	 * @param trainSet: name of the .arff file containing training set instances
	 * */
	public List<Instances> initializeTrainAndTest(String testSet, String trainSet) throws Exception {
		// get the data from the data set
		var train = new DataSource(trainSet);
		Instances training = train.getDataSet();
		var test = new DataSource(testSet);
		Instances testing = test.getDataSet();
		List<Instances> inst = new ArrayList<>();
		
		var numAttr = training.numAttributes(); // get the total number of attributes
		training.setClassIndex(numAttr - 1);
		testing.setClassIndex(numAttr-1);
		
		//first filter on data set columns
		training = this.filterIstances(training, this.filter);
		testing = this.filterIstances(testing, this.filter);
		
		// apply a sampling technique (if specified)
		training = this.applySampling(training);
		
		// apply Best First FS
		if(this.currProf.getSelection()) {
			int[] newFilter = this.bestFirstSearch(training);
			training = this.filterIstances(training, newFilter);
			testing = this.filterIstances(testing, newFilter);
		}
		
		inst.add(training);
		inst.add(testing);
		return inst;
	}
	
	
	/** Filters the columns of a given data set, returning filtered datas
	 * 
	 * @param datas: Instances from the data set
	 * @param columns: columns indexes to keep
	 * */
	private Instances filterIstances(Instances datas, int[] columns) throws Exception {
		var removeFilter = new Remove();
		removeFilter.setAttributeIndicesArray(columns);
		removeFilter.setInvertSelection(true);
		removeFilter.setInputFormat(datas);
		return Filter.useFilter(datas, removeFilter);
	}
	
	
	/* Use Best First as feature selection technique */
	private int[] bestFirstSearch(Instances training) throws Exception {
		var attSelection = new AttributeSelection();
		var eval = new CfsSubsetEval();
		var bf = new BestFirst();
		attSelection.setEvaluator(eval);
		attSelection.setSearch(bf);
		attSelection.SelectAttributes(training);
		return attSelection.selectedAttributes();
	}
	
	
	/** Apply a sampling technique, based on the value given by the RunProfile instance
	 * 
	 * @param training: the training set to balance with sampling
	 * */
	private Instances applySampling(Instances training) throws Exception {
		String sampling = this.currProf.getSampling();
		Instances newData = training;
		switch(sampling){
		case "UNDER":
			var underSampl = new SpreadSubsample();
			underSampl.setInputFormat(training);
			underSampl.setOptions(this.usOpts);
			newData = Filter.useFilter(training, underSampl);
			break;
		case "OVER":
			var res = new Resample();
			Double perc = this.findMajorityClassPerc(training);
			res.setInputFormat(training);
			
			List<String> ops =  new LinkedList<>(Arrays.asList(this.osOpts));
			ops.add(perc.toString());
			String[] osOptions = ops.toArray(new String[0]);
			
			res.setOptions(osOptions);
			newData = Filter.useFilter(training, res);
			break;
		case "SMOTE":
			var smote = new SMOTE();
			smote.setInputFormat(training);
			newData = Filter.useFilter(training, smote);
			break;
			
		default:
			break;
		}
		return newData;
	}
	
	
	/** Find the percentage over all the instances of the majority class
	 * 
	 * @param  inst: the instances of the training set
	 * */
	private double findMajorityClassPerc(Instances inst) {
		var numYes = 0;
		var numNo = 0;
		for(Instance i : inst) {
			if(i.stringValue(i.attribute(i.numAttributes()-1)).equals("yes"))
				numYes+=1;
			else
				numNo+=1;
		}
		if(numYes > numNo)
			return ((double)(numYes*2)/inst.size())*100;
		else
			return ((double)(numNo*2)/inst.size())*100;
	}
}
