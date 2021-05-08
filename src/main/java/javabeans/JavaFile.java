package main.java.javabeans;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

/* All the info of the file of a project with extension .java*/

public class JavaFile {
	private String name; // the absolute path in the .git directory
	private List<RevCommit> commits; //all the log history of this class
	private LocalDate creationDate; //date of creation, coincides with the date of the first commit
	
	public JavaFile(String name) {
		this.name = name;
		this.commits = new ArrayList<>();
		this.creationDate = null;
	}
	
	public void setCommitList(List<RevCommit> commits) {
		this.commits = commits;
	}
	
	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void insertNewCommit(RevCommit rc) {
		this.commits.add(rc);
	}
	
	
	// return only the class name, and not the absolute path
	public String getClassName() {
		var sb = new StringBuilder();
		var index = 0;
		for(index = this.name.length()-1; this.name.charAt(index) != '/'; index--) {
			sb.append(this.name.charAt(index));
		}
		sb.append(this.name.charAt(index)); // appends the '/'
		sb.reverse();
		return sb.toString();
	}
	
	public List<RevCommit> getCommitList(){
		return this.commits;
	}
	
	public LocalDate getCreationDate() {
		return this.creationDate;
	}
}
