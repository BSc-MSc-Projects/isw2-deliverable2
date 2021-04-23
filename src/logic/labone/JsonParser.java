package logic.labone;

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

import logic.labtwo.Version;

// this class manages and parses JSON objects/arrays
public class JsonParser {
	
	public void jsonToTicket(List<Ticket> tickList, JSONArray issues, Integer i) {
		try {
			int size;
			int index;
			JSONObject jo;
			
			JSONObject fields = new JSONObject(issues.getJSONObject(i%1000).get("fields").toString());
			Ticket tick = new Ticket(issues.getJSONObject(i%1000).get("key").toString(), 
					RetrieveTicketsID.parseDate(fields.getString("resolutiondate")),
					RetrieveTicketsID.parseDate(fields.getString("created")));
			   		tickList.add(tick);
			
		    JSONArray versions = new JSONArray(fields.getJSONArray("versions"));
			
		    // Parse the info for the Affected Versions
			size = versions.toList().size();
			for(index = 0; index < size; index++) {
				jo = versions.getJSONObject(index);
				Version ver = new Version(jo.getString("name"), jo.getString("releaseDate"), 
						jo.getString("id"), "");
				tick.addAv(ver);
			}
			
			//Parse the info for the Fix Versions
			JSONArray fixedVersions = new JSONArray(fields.getJSONArray("fixVersions"));
			size = fixedVersions.toList().size();
			for(index = 0; index < size; index++) {
				jo = fixedVersions.getJSONObject(index);
				if (jo.getBoolean("released")) {
					Version vers = new Version(jo.getString("name"), jo.getString("releaseDate"),
							jo.getString("id"), "");
					tick.addFv(vers);
				}
			}
			
		}catch (JSONException err){
			   Logger.getLogger("DEV1").log(Level.FINE, err.toString());
		}
	}
	
	private String readAll(Reader rd) throws IOException {
	      StringBuilder sb = new StringBuilder();
	      int cp;
	      while ((cp = rd.read()) != -1) {
	         sb.append((char) cp);
	      }
	      return sb.toString();
	}

	public JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))){
			String jsonText = readAll(rd);
			return new JSONArray(jsonText);
		} finally {
			is.close();
		}
	}

	public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))){
			String jsonText = readAll(rd);
			return new JSONObject(jsonText);
		} finally {
			is.close();
		}
	}
}