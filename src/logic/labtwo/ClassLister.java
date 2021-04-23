package logic.labtwo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/* Produces a list of all the .java files in a directory*/

public class ClassLister {
	
	public void listJFiles(File[] dir, List<String> classes) {
		for(File f : dir) {
			if(f.isDirectory())
				listJFiles(f.listFiles(), classes);
			else if(f.getName().contains(".java")) { //it is a .java file
				String javaCl = f.getAbsolutePath().substring(30);
				if(javaCl.startsWith("/"))
					javaCl = javaCl.substring(1);
				classes.add(javaCl);
			}
		}
	}
	
	public List<String> getJavaFiles(){
		File dir = new File("/home" + "/pierciro" + "/Scrivania" + "/avro");
		List<String> jFiles = new ArrayList<>();
		listJFiles(dir.listFiles(), jFiles);
		return jFiles;
	}
	
	public static void main(String[] args) {
		ClassLister classLister = new ClassLister();
		List<String> files = classLister.getJavaFiles();
		
		for(String file : files) {
			System.out.println(file);
		}
	}
}
