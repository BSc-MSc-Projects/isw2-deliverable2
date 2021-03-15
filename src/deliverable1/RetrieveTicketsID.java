package deliverable1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class RetrieveTicketsID {




   private static String readAll(Reader rd) throws IOException {
	      StringBuilder sb = new StringBuilder();
	      int cp;
	      while ((cp = rd.read()) != -1) {
	         sb.append((char) cp);
	      }
	      return sb.toString();
	   }

   public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
      InputStream is = new URL(url).openStream();
      try {
         BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
         String jsonText = readAll(rd);
         JSONArray json = new JSONArray(jsonText);
         return json;
       } finally {
         is.close();
       }
   }

   public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
      InputStream is = new URL(url).openStream();
      try {
         BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
         String jsonText = readAll(rd);
         JSONObject json = new JSONObject(jsonText);
         return json;
       } finally {
         is.close();
       }
   }
   
   // used to parse the attirbutes "created" and "resolutiondate"
   public static String parseDate(String datetime){
	   String date = "";
	   int i = 0;
	   
	   while(datetime.charAt(i) != 'T') {
		   date += datetime.charAt(i);
		   i++;
	   }
	   return date;
   }
   
   public static Map<String, Integer> groupByMonth(List<Ticket> tick_list){
	   Map<String, Integer> solved_tick = new HashMap<>();
	   String temp = "";
	   for(int i = 0; i < tick_list.size(); i++) {
		   temp = tick_list.get(i).getResMonth().toString();
		   if(!solved_tick.containsKey(temp)) {
			   solved_tick.put(temp, 1);
		   }
		   else {
			   solved_tick.put(temp, solved_tick.get(temp) + 1); // updates the value for the month
		   }
	   }
	   return solved_tick;
   }
   
   
   public static void fillDataset(Map<String, Integer> tick_map) {
	   Workbook wb = new HSSFWorkbook();
	   String[] months= {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
	                   "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
	   int i = 0;
			   
	   try(OutputStream fileOut = new FileOutputStream("dati_deliv1.xls")) {
		   Sheet sheet1 = wb.createSheet("dati_chart");
		   for(i = 0; i < tick_map.size(); i++) {
			   Row row = sheet1.createRow(i);
			   row.createCell(0).setCellValue(months[i]);
			   row.createCell(1).setCellValue(tick_map.get(months[i]));
		   }
		   Float mean = getMean(tick_map);
		   Float variance = getVariance(tick_map, mean);
		   sheet1.getRow(0).createCell(2).setCellValue("Upper bound");
		   sheet1.getRow(0).createCell(3).setCellValue(mean + 3*Math.sqrt(variance));
		   sheet1.getRow(1).createCell(2).setCellValue("Lower bound");
		   sheet1.getRow(1).createCell(3).setCellValue(mean - 3*Math.sqrt(variance));
		   
		   wb.write(fileOut);
		} catch (FileNotFoundException e) {
			Logger.getLogger("DEV1").log(Level.FINE, "Cannot find file\n");
		} catch (IOException e) {
			Logger.getLogger("DEV1").log(Level.FINE, "Problems opening file\n");
		}
   }
   
   public static Float getMean(Map<String, Integer> tick_map) {
	   Float mean = 0.0f;
	   Object[] keys = tick_map.keySet().toArray();
	   for(int i = 0; i < keys.length; i++) {
		   mean += tick_map.get(keys[i]);
	   }
	   return mean/keys.length;
   }
   
   public static Float getVariance(Map<String, Integer> tick_map, Float mean) {
	   Float var = 0.0f;
	   Object[] keys = tick_map.keySet().toArray();
	   for(int i = 0; i < keys.length; i++) {
		   var += (tick_map.get(keys[i]) - mean) * (tick_map.get(keys[i]) - mean);
	   }
	   return var/keys.length;
   }
   
   
   public static void main(String[] args) throws IOException, JSONException {
		   
	   String projName = "CACTUS";
	   Integer j = 0, i = 0, total = 1;
	   
	   List<Ticket> tick_list = new ArrayList<>();
	   //Get JSON API for closed bugs w/ AV in the project
	   do {
		   //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
		   j = i + 1000;
         
		   String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
				   + projName + "%22AND(%22status%22=%22closed%22OR"
				   + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
				   + i.toString() + "&maxResults=" + j.toString();
		   JSONObject json = readJsonFromUrl(url);
		   JSONArray issues = json.getJSONArray("issues");
         
		   total = json.getInt("total");
		   for (; i < total && i < j; i++) {
			   //Iterate through each ticket
			   try {
				   JSONObject fields = new JSONObject(issues.getJSONObject(i%1000).get("fields").toString());
				   Ticket tick = new Ticket(issues.getJSONObject(i%1000).get("key").toString(), 
						   RetrieveTicketsID.parseDate(fields.getString("resolutiondate")),
						   RetrieveTicketsID.parseDate(fields.getString("created")));
				   tick_list.add(tick);
				   
			   }catch (JSONException err){
				   Logger.getLogger("DEV1").log(Level.FINE, err.toString());
			   } 
		   }
	   } while (i < total);
	   System.out.println(tick_list.size());
	   Map<String, Integer> tick_map = RetrieveTicketsID.groupByMonth(tick_list);
	   RetrieveTicketsID.fillDataset(tick_map);
   }
   
}
