package logic.labone;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import logic.labtwo.Version;

// this class keeps the important information for the Jira ticket
public class Ticket {
	private String id;
	private String resolutionDate;
	private String creationDate;
	private List<Version> avList; //list of the affected version/s in the ticket
	private List<Version> fvList; //list of the fix version/s
	
	public Ticket(String id, String resDate, String creatDate) {
		this.id = id;
		this.resolutionDate = resDate;
		this.creationDate = creatDate;
		
		this.avList = new ArrayList<>();
		this.fvList = new ArrayList<>();
	}
	
	public String getId() {
		return this.id;
	}
	
	//this method is created to test Travis CI automated build
	public Integer getIntId() {
		return Integer.getInteger(this.id);
	}
	
	public String getResDate() {
		return this.resolutionDate;
	}
	
	public LocalDate getResDateAsDate() {
		return LocalDate.parse(this.resolutionDate);
	}
	
	public String getCrDate() {
		return this.creationDate;
	}
	
	public Integer getResMonth() {
		return LocalDate.parse(this.resolutionDate).getMonthValue();
	}
	
	public Integer getCrMonth() {
		return LocalDate.parse(this.creationDate).getMonthValue();
	}
	
	public Integer getResYear() {
		return LocalDate.parse(this.resolutionDate).getYear();
	}
	
	public Integer getCrYear() {
		return LocalDate.parse(this.creationDate).getYear();
	}
	
	public void addAv(Version av) {
		this.avList.add(av);
	}
	
	public List<Version> getAvs(){
		return this.avList;
	}
	
	// adds one fixed version to the list
	public void addFv(Version fv) {
		this.fvList.add(fv);
	}
	
	public List<Version> getFvs(){
		return this.fvList;
	}
}
