package logic.delivone;

import java.time.LocalDate;
import java.time.Month;

// this class aims to keep the important information for the ticket
public class Ticket {
	private String id;
	private String resolutionDate;
	private String creationDate;
	
	public Ticket(String id, String resDate, String creatDate) {
		this.id = id;
		this.resolutionDate = resDate;
		this.creationDate = creatDate;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getResDate() {
		return this.resolutionDate;
	}
	
	public String getCrDate() {
		return this.creationDate;
	}
	
	public Month getResMonth() {
		return LocalDate.parse(this.resolutionDate).getMonth();
	}
	
	public Month getCrMonth() {
		return LocalDate.parse(this.creationDate).getMonth();
	}
	
	public Integer getResYear() {
		return LocalDate.parse(this.resolutionDate).getYear();
	}
	
	public Integer getCrYear() {
		return LocalDate.parse(this.creationDate).getYear();
	}
}
