package logic;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

// this class manages the excel file in which we write our data
public class ExcelManipulator {
	public void fillDataset(Map<String, Integer> tickMap, List<String> yearsMonths) {
		   Workbook wb = new HSSFWorkbook();
		   int index = 0;
		   Float mean = getMean(tickMap);
		   Float variance = getVariance(tickMap, mean);
		   
		   Double upperBound = mean + 3*Math.sqrt(variance);
		   Double lowerBound = mean - 3*Math.sqrt(variance);
		   
		   if(lowerBound < 0) {
			   lowerBound = 0.0;
		   }
		   String currKey;
		   
		   try(OutputStream fileOut = new FileOutputStream("dati_deliv1.xls")) {
			   Sheet sheet1 = wb.createSheet("dati_chart");
			   for(index = 0; index < tickMap.size(); index++) {
				   currKey = yearsMonths.get(index);
				   Row row = sheet1.createRow(index);
				   row.createCell(0).setCellValue(currKey);
				   row.createCell(1).setCellValue(tickMap.get(currKey));
				   
				   // needed to draw the 3 horizontal lines in the chart
				   row.createCell(2).setCellValue(currKey);
				   row.createCell(3).setCellValue(upperBound);
				   row.createCell(4).setCellValue(currKey);
				   row.createCell(5).setCellValue(lowerBound);
				   row.createCell(6).setCellValue(currKey);
				   row.createCell(7).setCellValue(mean); 
			   }
			   wb.write(fileOut);
			} catch (FileNotFoundException e) {
				Logger.getLogger("DEV1").log(Level.FINE, "Cannot find file\n");
			} catch (IOException e) {
				Logger.getLogger("DEV1").log(Level.FINE, "Problems opening file\n");
			}
	   }
	   
	   public Float getMean(Map<String, Integer> tickMap) {
		   Float mean = 0.0f;
		   Object[] keys = tickMap.keySet().toArray();
		   for(int i = 0; i < keys.length; i++) {
			   mean += tickMap.get(keys[i]);
		   }
		   return mean/keys.length;
	   }
	   
	   public Float getVariance(Map<String, Integer> tickMap, Float mean) {
		   Float var = 0.0f;
		   Object[] keys = tickMap.keySet().toArray();
		   for(int i = 0; i < keys.length; i++) {
			   var += (tickMap.get(keys[i]) - mean) * (tickMap.get(keys[i]) - mean);
		   }
		   return var/keys.length;
	   }
}
