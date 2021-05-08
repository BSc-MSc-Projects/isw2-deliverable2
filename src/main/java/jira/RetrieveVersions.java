package main.java.jira;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import main.java.json.JsonParser;

public class RetrieveVersions {
	private JsonParser jp;
	
	public RetrieveVersions() {
		this.jp = new JsonParser();
	}
	
	public Map<String, LocalDate> getVersions(String projName) {
		Map<String, LocalDate> versHm = new LinkedHashMap<>();
		String url = "https://issues.apache.org/jira/rest/api/2/project/"
				   + projName;
		try {
			JSONObject jobj = jp.readJsonFromUrl(url);
			var versions = jobj.getJSONArray("versions");
			fillVersionInfo(versions, versHm);
			return versHm;
			
		} catch (JSONException | IOException e) {
			Logger.getLogger("ISW-1").log(Level.FINE, "Cannot fetch Json results");
		}
		
		return versHm;
	}
	
	
	// parse the JSONArray into Version objects
	private void fillVersionInfo(JSONArray array, Map<String, LocalDate> versions){
		var total = array.toList().size();
		var i = 0;
		var releaseDate = "releaseDate";
		List<LocalDate> dates = new ArrayList<>();
		List<String> versName = new ArrayList<>();
		List<LocalDate> versDate = new ArrayList<>();
		
		while(i < total){
			if(array.getJSONObject(i).getBoolean("released") && 
					array.getJSONObject(i).keySet().contains(releaseDate)) {
					versName.add(array.getJSONObject(i).getString("name"));
					versDate.add(LocalDate.parse(array.getJSONObject(i).getString(releaseDate)));
					
					dates.add(LocalDate.parse(array.getJSONObject(i).getString(releaseDate)));
			}
			i++;
		}
		Collections.sort(dates);
		sortVersions(dates, versName, versDate, versions);
	}
	
	
	private void sortVersions(List<LocalDate> dates, List<String> versName, List<LocalDate> versDate, 
			Map<String, LocalDate> sortedVers) {
		var i = 0;
		
		for(LocalDate d : dates) {
			while(i < versName.size()) {
				if(versDate.get(i).isEqual(d)){
					sortedVers.putIfAbsent(versName.get(i), versDate.get(i));
					versName.remove(i);
					versDate.remove(i);
				}
				i++;
			}
			i = 0;
		}
	}
}
