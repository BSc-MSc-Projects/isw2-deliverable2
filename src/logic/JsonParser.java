package logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// this class manages and parses JSON objects/arrays
public class JsonParser {
	
	public void jsonToTicket(List<Ticket> tickList, JSONArray issues, Integer i) {
		try {
			JSONObject fields = new JSONObject(issues.getJSONObject(i%1000).get("fields").toString());
			Ticket tick = new Ticket(issues.getJSONObject(i%1000).get("key").toString(), 
					RetrieveTicketsID.parseDate(fields.getString("resolutiondate")),
					RetrieveTicketsID.parseDate(fields.getString("created")));
			   		tickList.add(tick);
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
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONArray json = new JSONArray(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
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
}
