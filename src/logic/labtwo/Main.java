package logic.labtwo;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONException;

import logic.labone.Ticket;

// main class used to gather info
public class Main {
	private String projName = "AVRO"; //name of the project to analyze
	private Map<LocalDate, String> fixVers; //hash map containing the versions, with release dates
	
	private List<String> javaFiles; //all the classes of the project
	private List<LocalDate> commitDates; // all the dates for the commits
	private List<Ticket> tickList; // list of tickets from Jira
	private List<String> modFiles; //list of all the classes that have been touched in the commits
	
	private RetrieveTickets rt;
	private LogAnalyzer la;
	private ClassLister cl;
	
	public Main() {
		this.fixVers = new HashMap<>(); //hash map containing the versions, with release dates
		
		this.javaFiles = new ArrayList<>(); //all the classes of the project
		this.commitDates = new ArrayList<>(); // all the dates for the commits
		this.tickList = new ArrayList<>(); // list of tickets from Jira
		this.modFiles = new ArrayList<>(); //list of all the classes that have been touched in the commits
		
		this.cl = new ClassLister();
		this.la = new LogAnalyzer();
		this.rt = new RetrieveTickets();
	}
	
	public static void main(String[] args) throws JSONException, IOException {
		Main mn = new Main();
		
		mn.tickList = mn.rt.retrieveTick(mn.projName); // get the all the ticket of type "bug" from Jira
		mn.javaFiles = mn.cl.getJavaFiles(); //get all the .java files in the project folder
		mn.commitDates = mn.la.getCommitsTime();
		mn.fixVers = mn.rt.getAllFixVersions(mn.tickList);
		//List<LocalDate> keys = mn.rt.convertToList(mn.fixVers.keySet());
		//LocalDate lastDate = keys.get(keys.size() - 1); // get the date of the last release (version)
		LocalDate lastDate = LocalDate.now();
		LocalDate lowerBound = LocalDate.of(lastDate.getYear(), Month.of(lastDate.getMonthValue()-1), 
				lastDate.getDayOfMonth());
		
		
		// cycle to verify the commits that affected the single class
		for(String javaClass : mn.javaFiles) {
			//System.out.println("Qui\n");
			RevCommit rv = mn.la.findCommitsForFile(javaClass, lowerBound, lastDate);
			if(rv != null && checkIfBugCommit(rv.getFullMessage(), mn.tickList)) {
				//System.out.println(rv.getName());
				mn.modFiles.add(javaClass);
			}
		}
		System.out.println(mn.modFiles.size());
		CsvProducer csvProd = new CsvProducer();
		csvProd.csvMonthlyBuggyness(mn.projName, mn.javaFiles, mn.modFiles);
	}
	
	
	private static boolean checkIfBugCommit(String cMsg, List<Ticket> tickList) {
		for(Ticket tick: tickList) {
			if(cMsg.contains(tick.getId()))
				return true;
		}
		return false;
	}
}
