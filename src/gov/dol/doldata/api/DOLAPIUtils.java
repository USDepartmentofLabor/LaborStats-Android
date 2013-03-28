/**
 * Created by the U.S. Department of Labor
 * This source is released to the Public domain
 */
package gov.dol.doldata.api;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DOLAPIUtils {
	
	public static String getRequestHeader(String apiURI, String apiHost, String apiKey, String sharedSecret) throws NoSuchAlgorithmException, InvalidKeyException
	{
		//Timestamp
		String timestamp = getAPITimestamp();
		
		//Remove API_HOST from the apiURI
		//That part is not signed
		apiURI = apiURI.substring(apiHost.length());
		
		//Signature
		MessageFormat formatter = new MessageFormat("{0}&Timestamp={1}&ApiKey={2}");
		String dataToSign = formatter.format(new String[] {apiURI, timestamp, apiKey});
		String signature = getAPISignature(dataToSign, sharedSecret);
		
		//Final header
		formatter = new MessageFormat("Timestamp={0}&ApiKey={1}&Signature={2}");
		String result = formatter.format(new String[] {timestamp, apiKey, signature});
		
		return result;
	}
	
	private static String getAPITimestamp()
	{
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		//Time in GMT
		return dateFormatGmt.format(new Date());
	}
	
	private static String getAPISignature(String data, String sharedSecret) throws NoSuchAlgorithmException, InvalidKeyException
	{
	    byte[] keyData = sharedSecret.getBytes();
	    
	    // Create the key
	    SecretKeySpec key = new SecretKeySpec(keyData, "HmacSHA1");

	    // Now an HMAC can be created, passing in the key and the
	    // SHA digest.
	    Mac mac = Mac.getInstance("HmacSHA1");
	    mac.init(key);

	    // The HMAC can be updated much like a digest
	    byte[] sign = mac.doFinal(data.getBytes());
	 
	    // Create Hex String from Byte array
        StringBuffer hexString = new StringBuffer();
        for (int i=0; i< sign.length; i++)
        	hexString.append(String.format("%02x", sign[i]));
        return hexString.toString();
	}
	
	public static List<Map<String, String>> parseJSON(String jsonString) {
		try {
			//Get JSON Object
			JSONObject object = new JSONObject(jsonString);
			
			//pass the "d" security wrapper
			JSONArray wrapper = object.optJSONArray("d");
			
			//Special case when .Net adds an extra "results" wrapper
			if (wrapper == null) {
				object = object.getJSONObject("d");
				wrapper = object.optJSONArray("results");
			}
			
			//We'll store the list of Maps here
			List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
			
			//Loop through all the results and store them in the map
			for (int i = 0; i < wrapper.length(); i++) {
				JSONObject obj = wrapper.getJSONObject(i);

			    Map<String,String> map = new HashMap<String,String>();
			    
			    //Iterate through all the columns. Convert JSONObject to Map
			    Iterator<?> iter = obj.keys();
			    while(iter.hasNext()){
			        String key = (String)iter.next();
			        String value = obj.getString(key);
			        map.put(key,value);
			    }
			    resultList.add(map);
			}
			
			return resultList;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
