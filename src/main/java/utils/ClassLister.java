package main.java.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;


/* Produces a list of all the .java files in a directory*/

public class ClassLister {
	private String project;
	
	public ClassLister(String projName) {
		this.project = projName;
	}
	
	
	/* Retrieve all the files with extension .java from the .git repository */
	public List<String> getJavaFiles(){
		var dir = new File("/home" + "/pierciro" + "/Scrivania/" + this.project);
		List<File> jFiles = new ArrayList<>();
		Collection<File> files = FileUtils.listFiles(dir, TrueFileFilter.INSTANCE,
				DirectoryFileFilter.DIRECTORY);
		
		jFiles.addAll(files);
		List<String> newFileList = new ArrayList<>();
		for(File file : jFiles) {
			if(file.getPath().endsWith((".java")))
				newFileList.add(file.getPath()
						.substring(file.getAbsolutePath().indexOf(this.project) + this.project.length()+1));
		}
		return newFileList;	
	}
}
