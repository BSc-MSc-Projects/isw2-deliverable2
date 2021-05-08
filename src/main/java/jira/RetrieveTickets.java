package main.java.jira;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import main.java.javabeans.Ticket;
import main.java.json.JsonParser;

public class RetrieveTickets {
	public List<Ticket> retrieveTick(String projName) throws JSONException, IOException {
		Integer j = 0; 
		Integer i = 0; 
		Integer total = 1;
		var jp = new JsonParser();
		   
		List<Ticket> tickList = new ArrayList<>();
		//Get JSON API for closed bugs w/ AV in the project
		do {
			//Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
			j = i + 1000;
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
	                + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
	                + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,fixVersions,created&startAt="
	                + i.toString() + "&maxResults=" + j.toString();
			JSONObject json;
			json = jp.readJsonFromUrl(url);
			var issues = json.getJSONArray("issues");
			total = json.getInt("total");
				
			//Iterate through each ticket
			for (; i < total && i < j; i++) {
				jp.jsonToTicket(tickList, issues, i);
			}
		} while (i < total);
		
		var tickArray = new Ticket[tickList.size()];
		tickArray = tickList.toArray(tickArray);
		
		mergeSort(tickArray, 0, tickArray.length-1);
		return Arrays.asList(tickArray);
	}
	
	
	public void mergeSort(Ticket[] a, int left, int right) {
		if(left < right) {
			var center = ((left+right)/2);
			mergeSort(a, left, center);
			mergeSort(a, center+1, right);
			merge(a, left, center, right);
		}
	}
	
	
	public void merge(Ticket[] a, int left, int center, int right) {
		var i = left;
	    var j = center + 1;
	    var k = 0;
	    var b = new Ticket[right-left+1];

	    while (i <= center && j <= right) {
	       if(a[i].getResDateAsDate().isBefore(a[j].getResDateAsDate()) ||
	    		   a[i].getResDateAsDate().isEqual(a[j].getResDateAsDate())) {
	          b[k] = a[i];
	          i = i + 1;
	       }
	       else {
	    	   b[k] = a[j];
	    	   j = j + 1;
	       }
	       k = k + 1;
	    }

	    while (i <= center) {
	       b[k] = a[i];
	       i = i + 1;
	       k = k + 1;
	    }
	    while(j <= right) {
	       b[k] = a[j];
	       j = j + 1;
	       k = k + 1;
	    }

	    for (k = left; k <= right; k++)
	       a[k] = b[k-left];
	}
	
	
	public List<LocalDate> convertToList(Set<LocalDate> keySet){
		List<LocalDate> converted = new ArrayList<>();
		for(LocalDate key : keySet) {
			converted.add(key);
		}
		return converted;
	}
}
