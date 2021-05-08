package main.java.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/* Produces a list of all the .java files in a directory*/

public class ClassLister {
	private String project;
	
	public ClassLister(String projName) {
		this.project = projName;
	}
	
	public void listJFiles(File[] dir, List<String> classes) {
		for(File f : dir) {
			if(f.isDirectory())
				listJFiles(f.listFiles(), classes);
			else if(f.getName().contains(".java")) { //it is a .java file
				String javaCl = f.getAbsolutePath();
				javaCl = javaCl.substring(javaCl.indexOf(this.project) + this.project.length()+1);
				classes.add(javaCl);
			}
		}
	}
	
	public List<String> getJavaFiles(){
		File dir = new File("/home" + "/pierciro" + "/Scrivania/" + this.project);
		List<String> jFiles = new ArrayList<>();
		listJFiles(dir.listFiles(), jFiles);
		return jFiles;
	}
}
