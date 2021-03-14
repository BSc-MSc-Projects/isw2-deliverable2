package deliverable1;

import java.time.LocalDate;
import java.time.Month;

// this class aims to keep the important information for the ticket
public class Ticket {
	private String id;
	private String resolution_date;
	private String creation_date;
	
	public Ticket(String id, String res_date, String creat_date) {
		this.id = id;
		this.resolution_date = res_date;
		this.creation_date = creat_date;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getResDate() {
		return this.resolution_date;
	}
	
	public String getCrDate() {
		return this.creation_date;
	}
	
	public Month getResMonth() {
		return LocalDate.parse(this.resolution_date).getMonth();
	}
	
	public Month getCrdMonth() {
		return LocalDate.parse(this.creation_date).getMonth();
	}
}
