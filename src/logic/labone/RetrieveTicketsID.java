package logic.labone;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class RetrieveTicketsID {
   
   // used to parse the attirbutes "created" and "resolutiondate"
   public static String parseDate(String datetime){
	   int i = 0;
	   StringBuilder sb = new StringBuilder();
	   
	   while(datetime.charAt(i) != 'T') {
		   sb.append(datetime.charAt(i));
		   i++;
	   }
	   return sb.toString();
   }
   
   public static Map<String, Integer> groupByMonth(List<Ticket> tickList){
	   Map<String, Integer> solvedTick = new HashMap<>();
	   String temp = "";
	   for(int i = 0; i < tickList.size(); i++) {
		   temp = tickList.get(i).getResMonth().toString() + "-" + tickList.get(i).getResYear().toString();
		   if(!solvedTick.containsKey(temp)) {
			   solvedTick.put(temp, 1);
		   }
		   else {
			   solvedTick.put(temp, solvedTick.get(temp) + 1); // updates the value for the month
		   }
	   }
	   return solvedTick;
   }
   
   
   public static List<String> getYears(List<Ticket> tickList) {
	   
	   List<LocalDate> years = new ArrayList<>();
	   LocalDate date;
	   
	   for(int i = 0; i < tickList.size(); i++) {
		   date = LocalDate.parse(tickList.get(i).getResDate());
		   if(!years.contains(date)) {
			   years.add(date);
		   }
	   }
	   Collections.sort(years);
	   
	   List<String> keys = new ArrayList<>();
	   LocalDate dt;
	   for(int i = 0; i < years.size(); i++) {
		   dt = years.get(i);
		   String monthYear = dt.getMonthValue() + "-" + dt.getYear();
		   if(!keys.contains(monthYear)) {
			   keys.add(monthYear);
		   }
	   }
	   return keys;
   }
   
   
   public static void main(String[] args) throws IOException, JSONException {
		   
	   String projName = "CACTUS";
	   Integer j = 0; 
	   Integer i = 0; 
	   Integer total = 1;
	   JsonParser jp = new JsonParser();
	   ExcelManipulator ex = new ExcelManipulator();
	   
	   List<Ticket> tickList = new ArrayList<>();
	   //Get JSON API for closed bugs w/ AV in the project
	   do {
		   //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
		   j = i + 1000;
         
		   String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
				   + projName + "%22AND(%22status%22=%22closed%22OR"
				   + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
				   + i.toString() + "&maxResults=" + j.toString();
		   JSONObject json = jp.readJsonFromUrl(url);
		   JSONArray issues = json.getJSONArray("issues");
         
		   total = json.getInt("total");
		   
		   //Iterate through each ticket
		   for (; i < total && i < j; i++) {
			   jp.jsonToTicket(tickList, issues, i);
		   }
	   } while (i < total);
	   Map<String, Integer> tickMap = groupByMonth(tickList);
	   List<String> yearsMonths = getYears(tickList);
	   //ex.fillDataset(tickMap, yearsMonths);
	   ex.makeCsv(tickMap, yearsMonths, projName);
	   System.out.println("Completato \n");
   }
   
}
