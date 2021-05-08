package main.java.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import main.java.javabeans.Ticket;
import main.java.javabeans.Version;

// this class manages and parses JSON objects/arrays
public class JsonParser {
	
	
	/* Parse the JSONArray containing all the Jira ticket for a given project
	 * 
	 * @param tickList: the List in which the parsed ticket will be stored
	 * @param issues: the ticket list in JSON format
	 * @param i: index to access the JSONArray */
	public void jsonToTicket(List<Ticket> tickList, JSONArray issues, Integer i) {
		try {
			int size;
			int index;
			JSONObject jo;
			
			JSONObject fields = new JSONObject(issues.getJSONObject(i%1000).get("fields").toString());
			Ticket tick = new Ticket(issues.getJSONObject(i%1000).get("key").toString(), 
					this.parseDate(fields.getString("resolutiondate")),
					this.parseDate(fields.getString("created")));
			   		tickList.add(tick);
			
		    JSONArray versions = new JSONArray(fields.getJSONArray("versions"));
			
		    // Parse the info for the Affected Versions
			size = versions.toList().size();
			for(index = 0; index < size; index++) {
				jo = versions.getJSONObject(index);
				Version ver = new Version(jo.getString("name"), jo.getString("releaseDate"), 
						jo.getInt("id"), -1);
				tick.addAv(ver);
			}
			
			//Parse the info for the Fix Versions
			JSONArray fixedVersions = new JSONArray(fields.getJSONArray("fixVersions"));
			size = fixedVersions.toList().size();
			for(index = 0; index < size; index++) {
				jo = fixedVersions.getJSONObject(index);
				if (jo.getBoolean("released")) {
					Version vers = new Version(jo.getString("name"), jo.getString("releaseDate"),
							jo.getInt("id"), -1);
					tick.addFv(vers);
				}
			}
			
		}catch (JSONException err){
			   Logger.getLogger("DEV1").log(Level.FINE, err.toString());
		}
	}
	
	
	/* Read a JSONArray from the given URL
	 * 
	 * @param url: the URL from which the JSONArray is read */
	public JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))){
			String jsonText = readAll(rd);
			return new JSONArray(jsonText);
		} finally {
			is.close();
		}
	}
	
	
	/* Read from a Reader all the characters contained
	 * 
	 * @param rd: the Reader object */
	private String readAll(Reader rd) throws IOException {
	      StringBuilder sb = new StringBuilder();
	      int cp;
	      while ((cp = rd.read()) != -1) {
	         sb.append((char) cp);
	      }
	      return sb.toString();
	}
	
	
	/* Read a JSONObject from a url 
	 * 
	 * @param url: the URL from which to read*/
	public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))){
			String jsonText = readAll(rd);
			return new JSONObject(jsonText);
		} finally {
			is.close();
		}
	}
	
	
	/* Parse the attributes "created" and "resolutiondate" 
	 * 
	 * @param datetime: a String in DateTime format */ 
	private String parseDate(String datetime){
		int i = 0;
		StringBuilder sb = new StringBuilder();
		   
		while(datetime.charAt(i) != 'T') {
			sb.append(datetime.charAt(i));
			i++;
		}
		return sb.toString();
	}
}
