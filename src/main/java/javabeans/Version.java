package main.java.javabeans;

import java.time.LocalDate;

/* This class abstracts the version of a project*/
public class Version {
	private String name;
	private String releaseDate;
	private Integer id;
	private Integer projId;
	
	public Version(String name, String releaseDate, Integer id, Integer projId) {
		this.name = name;
		this.releaseDate = releaseDate;
		this.id = id;
		this.projId = projId;
	}
	
	public Version() {
		
	}
	
	public String getName() {
		return this.name;
	}
	
	public LocalDate getReleaseDate() {
		return LocalDate.parse(this.releaseDate);
	}
	
	public Integer getId() {
		return this.id;
	}
	
	public Integer getProjId() {
		return this.projId;
	}
}
