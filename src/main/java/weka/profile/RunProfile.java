package main.java.weka.profile;

/* Specify the methods to apply in each run, e.g: use cost sensitive classification
 * with feature selection*/

public class RunProfile {
	private boolean selection; //use selection or best first as feature selection
	private String sampling;
	private String costSensitive;
	
	
	public RunProfile(boolean isSelection, String sampl, String costSens) {
		this.selection = isSelection;
		this.sampling = sampl;
		this.costSensitive = costSens;
	}
	
	
	/* Getter methods*/
	
	public boolean getSelection() {
		return this.selection;
	}
	
	
	public String getSampling() {
		return this.sampling;
	}
	
	
	public String getSensitive() {
		return this.costSensitive;
	}
}
