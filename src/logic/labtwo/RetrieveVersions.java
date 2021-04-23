package logic.labtwo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import logic.labone.JsonParser;
import logic.labone.Ticket;

public class RetrieveVersions {
	private JsonParser jp;
	private List<Ticket> tickList;
	private List<Version> versList;
	
	public RetrieveVersions() {
		this.jp = new JsonParser();
		this.tickList = new ArrayList<>();
		this.versList = new ArrayList<>();
	}
	
	public void getVersions(String projName) {
		String url = "https://issues.apache.org/jira/rest/api/2/project/"
				   + projName;
		try {
			JSONObject jobj = jp.readJsonFromUrl(url);
			JSONArray versions = jobj.getJSONArray("versions");
			//System.out.println(versions.toString());
			this.versList = fillVersionInfo(versions);
			
		} catch (JSONException | IOException e) {
			Logger.getLogger("ISW-1").log(Level.FINE, "Cannot fetch Json results");
		}
	}
	
	public static void main(String[] args) {
		RetrieveVersions rt = new RetrieveVersions();
		rt.getVersions("STORM");
	}
	
	private List<Version> fillVersionInfo(JSONArray array){
		List<Version> versList = new ArrayList<>();
		int tot = array.toList().size();
		for(int i = 0; i < tot; i++) {
			Version ver = new Version(array.getJSONObject(i).getString("releaseDate"), 
					array.getJSONObject(i).getString("name"), array.getJSONObject(i).getString("id"),
					array.getJSONObject(i).getString("projectId"));
			versList.add(ver);
		}
		return versList;
	}
}
