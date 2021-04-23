package logic.labtwo;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import logic.labone.JsonParser;
import logic.labone.Ticket;

public class RetrieveTickets {
	public List<Ticket> retrieveTick(String projName) throws JSONException, IOException {
		Integer j = 0; 
		Integer i = 0; 
		Integer total = 1;
		JsonParser jp = new JsonParser();
		   
		List<Ticket> tickList = new ArrayList<>();
		//Get JSON API for closed bugs w/ AV in the project
		do {
			//Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
			j = i + 1000;
	         
			/*String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
					   + projName + "%22&type=bug%22&fields=key,resolutiondate,versions,fixVersions,created&startAt="
							   + i.toString() + "&maxResults=" + j.toString();
			*/
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
	                + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
	                + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,fixVersions,created&startAt="
	                + i.toString() + "&maxResults=" + j.toString();
			JSONObject json;
			json = jp.readJsonFromUrl(url);
			JSONArray issues = json.getJSONArray("issues");
			total = json.getInt("total");
				
			//Iterate through each ticket
			for (; i < total && i < j; i++) {
				jp.jsonToTicket(tickList, issues, i);
			}
		} while (i < total);
		
		return tickList;
	}
	
	
	// get the releaseDate of all the fix versions, as an hashmap <release_date:version>
	public Map<LocalDate, String> getAllFixVersions(List<Ticket> tickList) {
		Map<LocalDate, String> verDate = new HashMap<>();
		List<Version> vList;
		for(int i = 0; i < tickList.size(); i++) {
			// get the list of fix Versions for a ticket
			vList = tickList.get(i).getFvs();
			if(!vList.isEmpty()) {
				//loop over each fix version
				for(int j = 0; j < vList.size(); j++) {
					// check if the key (version number) is already in the HashMap
					if(!verDate.containsKey(vList.get(j).getReleaseDate())) {
						verDate.put(vList.get(j).getReleaseDate(), vList.get(j).getName());
					}
				}
			}
		}
		verDate = sortFixVersionsByDate(verDate);
		return verDate;
	}
	
	
	// sort the Map by the date of the version
	private Map<LocalDate, String> sortFixVersionsByDate(Map<LocalDate, String> fixVersMap){
		Map<LocalDate, String> sortedVers = new HashMap<>();
		Set<LocalDate> keySet = fixVersMap.keySet();
		List<LocalDate> keys = convertToList(keySet);
		Collections.sort(keys); // sort the dates
		
		for(LocalDate key : keys) {
			sortedVers.put(key, fixVersMap.get(key));
		}
		
		return sortedVers;
	}
	
	
	public List<LocalDate> convertToList(Set<LocalDate> keySet){
		List<LocalDate> converted = new ArrayList<>();
		for(LocalDate key : keySet) {
			converted.add(key);
		}
		return converted;
	}
	
	/*
	private List<Ticket> removeLastTicks(List<Ticket> tickList, LocalDate lastRel){
		List<Ticket> newTickList = new ArrayList<>();
		for(int i = 0; i < tickList.size(); i++) {
			// check if the resolution date of the ticket is before the date of the version
			if(LocalDate.parse(tickList.get(i).getResDate()).isBefore(lastRel))
				newTickList.add(tickList.get(i));
		}
		return newTickList;
	}*/
}
