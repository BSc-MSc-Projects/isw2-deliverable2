package main.java;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.scm.ScmFile;
import org.eclipse.jgit.revwalk.RevCommit;

import main.java.javabeans.JavaFile;
import main.java.javabeans.Ticket;
import main.java.jira.RetrieveTickets;
import main.java.jira.RetrieveVersions;
import main.java.utils.ClassLister;
import main.java.utils.CsvProducer;
import main.java.utils.LogAnalyzer;

// main class used to gather info
public class Main {
	private String projName = "ZOOKEEPER"; //name of the project to analyze
	private float precRelease = 0.5f; // percentage of release to discard
	
	private List<String> javaFiles; //all the classes of the project
	private List<Ticket> tickList; // list of tickets from Jira
	
	private RetrieveTickets rt;
	private LogAnalyzer la;
	private ClassLister cl;
	private RetrieveVersions rv;
	private CsvProducer csvProd;
	private StringBuilder customMsg;
	
	private Logger logger;
	
	public Main() {
		this.javaFiles = new ArrayList<>(); //all the classes of the project
		this.tickList = new ArrayList<>(); // list of tickets from Jira
		
		this.cl = new ClassLister(this.projName.toLowerCase());
		this.la = new LogAnalyzer(this.projName.toLowerCase());
		this.rt = new RetrieveTickets();
		this.rv = new RetrieveVersions();
		this.csvProd = new CsvProducer(this.projName);
		
		this.logger = Logger.getLogger("MAIN");
		this.customMsg = new StringBuilder();
	}
	
	
	/* Start the analysis of the project */
	public void startAnalysis() throws IOException {
		String msg;
		
		this.logger.log(Level.INFO, "Starting...\n");
		
		this.tickList = this.rt.retrieveTick(this.projName); // get the all the ticket of type "bug" from Jira
		var total = this.tickList.size();
		
		Map<String, LocalDate> vers = this.rv.getVersions(this.projName);
		List<String> keys = new ArrayList<>();
		this.javaFiles = this.cl.getJavaFiles();
		List<JavaFile> jFileList = new ArrayList<>();
		List<RevCommit> entireLog = this.la.findAll();
		
		var counter = 0;
		var size = this.javaFiles.size();
		
		for(String javaFile : this.javaFiles) {
			counter++;
			this.getProgress(counter, size);
			JavaFile jFile = new JavaFile(javaFile);
			jFileList.add(jFile);
		}
		
		this.logger.log(Level.INFO, "-------------------------Completed------------------------------\n");
		
		this.logger.info("Getting commit for each file...");
		this.setCommitsForClasses(jFileList, entireLog);

		// set the parameters for the CsvProducer
		this.csvProd.setJavaFileList(jFileList);
		
		this.logger.log(Level.INFO, "-------------------------Completed------------------------------\n");
		
		//vers = mn.discardPercRelease(vers); // discard half of the release
		keys.addAll(vers.keySet()); // consider all the release
		
		// defines the interval for a release
		LocalDate upper;
		LocalDate lower = vers.get(keys.get(0)); //get the first date to start from
		this.csvProd.setTickList(tickList);
		this.csvProd.setVersions(vers);
		
		counter = 0;

		this.logger.log(Level.INFO, "Creating .csv file\n");
		this.csvProd.setCsvHeader();
		
		//get the data for the first release
		this.logger.log(Level.INFO, "Getting data for the first release...\n");
		this.csvProd.getAllMetrics(null, lower, keys.get(0));
		
		var limit = (int)(keys.size()*this.precRelease);
		
		for(int i = 1; i < limit; i++) {
			counter++;
			this.getProgress(counter, limit);
			upper = vers.get(keys.get(i));
			
			customMsg.append(lower+" "+upper+"\n");
			msg = customMsg.toString();
			customMsg.delete(0, customMsg.length());
			
			this.logger.log(Level.INFO, msg);
			
			this.tickList = discardOldTicket(upper, tickList);
			this.csvProd.setTickList(tickList);
			
			this.csvProd.getAllMetrics(lower, upper, keys.get(i));
			lower = upper;
		}
		
		customMsg.append("Over " + total + " tickets, the analysis found "+ 
		this.csvProd.getNBuggy() + " bug istances\n");
		msg = customMsg.toString();
		customMsg.delete(0, customMsg.length());
		
		this.logger.log(Level.INFO, msg);
		this.logger.log(Level.INFO, "-------------------------Ended------------------------------\n");
	}
	
	
	private void getProgress(int counter, int total) {
		var perc = ((float)counter/(float)total);
		this.customMsg.append("Working... ["+perc*100+"%]");
		String msg = this.customMsg.toString();
		customMsg.delete(0, customMsg.length());
		
		this.logger.log(Level.INFO, msg);
	}
	
	
	/* Discard the ticket that are under a threshold 
	 * 
	 * @param cap: the date threshold*/
	private static List<Ticket> discardOldTicket(LocalDate cap, List<Ticket> tickList){
		List<Ticket> relTick = new ArrayList<>();
		LocalDate tempD;
		for(Ticket t: tickList) {
			tempD = t.getResDateAsDate();
			if(tempD.isAfter(cap))
				relTick.add(t);
		}
		return relTick;
	}
	
	
	/* Set the commit list for each .java class 
	 * 
	 * @param jFileList: list of all the .java files of the project
	 * @param commitList: the whole git log
	 * @param infoLog: instance of Logger, to print information on the progress*/
	private void setCommitsForClasses(List<JavaFile> jFileList, List<RevCommit> commitList) {
		List<ScmFile> filesInComm;
		var index = 1;
		var total = commitList.size();
		
		for(RevCommit commit : commitList) {
			this.getProgress(index, total);
			filesInComm = this.la.filesInCommit(commit);
			for(ScmFile commFile: filesInComm) {
				for(JavaFile jFile : jFileList) {
					if(commFile.getPath().contains(jFile.getClassName())) {
						jFile.insertNewCommit(commit);
						
						// check if the creation date has been set
						if(jFile.getCreationDate() == null)
							jFile.setCreationDate(commit.getAuthorIdent().getWhen().toInstant()
									.atZone(ZoneId.systemDefault()).toLocalDate());
					}
				}
			}
			index++;
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		Main mn = new Main();
		mn.startAnalysis();
	}
}
