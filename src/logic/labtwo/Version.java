package logic.labtwo;

import java.time.LocalDate;

/* This class abstracts the version of a project*/
public class Version {
	private String name;
	private String releaseDate;
	private String id;
	private String projId;
	
	public Version(String name, String releaseDate, String id, String projId) {
		this.name = name;
		this.releaseDate = releaseDate;
		this.id = id;
		this.projId = projId;
	}
	
	public String getName() {
		return this.name;
	}
	
	public LocalDate getReleaseDate() {
		return LocalDate.parse(this.releaseDate);
	}
	
	public Integer getId() {
		return Integer.getInteger(this.id);
	}
	
	public Integer getProjId() {
		return Integer.getInteger(this.projId);
	}
}
