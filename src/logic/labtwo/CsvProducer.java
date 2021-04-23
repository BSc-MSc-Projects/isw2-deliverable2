package logic.labtwo;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/* Create .csv files as needed*/

public class CsvProducer {
	
	// produce a .csv file that reports if a class is buggy or not in a given release
	public void csvMonthlyBuggyness(String projName, List<String> classes, List<String> commClasses) {
		File f = new File(projName + "_month_buggy.csv");
		
		try(FileOutputStream fos = new FileOutputStream(f); 
				DataOutputStream dos = new DataOutputStream(fos)){
			for(String javaClass : classes) {
				if(findClass(javaClass, commClasses)) {
					dos.writeBytes(projName + ";" + javaClass + ";" + "Yes" + "\n");
				}
				else
					dos.writeBytes(projName + ";" + javaClass + ";" + "No" + "\n");
			}
			
		} catch (FileNotFoundException e) {
			Logger.getLogger("LAB-2").log(Level.WARNING, "Cannot find the file\n");
		} catch (IOException e) {
			Logger.getLogger("LAB-2").log(Level.WARNING, "IOException occured\n");
		}
	}
	
	
	private boolean findClass(String classPath, List<String> commFiles) {
		for(String file : commFiles) {
			if(classPath.contains(file))
				return true;
		}
		return false;
	}
}
