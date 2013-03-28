/**
 * Created by the U.S. Department of Labor
 * This source is released to the Public domain
 */
package gov.dol.doldata.api;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

public class DOLDataRequest {
	// instance variables
	private DOLDataRequestCallback callback;
	private DOLDataContext context;
	public RequestTask requestTask = null;
	

	/**
	 * @return the context
	 */
	public DOLDataContext getContext() {
		return context;
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(DOLDataContext context) {
		this.context = context;
	}

	public DOLDataRequestCallback getCallback() {
		return callback;
	}

	/**
	 * Constructor
	 * 
	 * @param callback
	 * @param context
	 */
	public DOLDataRequest(DOLDataRequestCallback callback,
			DOLDataContext context) {
		super();
		this.callback = callback;
		this.context = context;
	}

	/**
	 * Set Callback
	 * 
	 * @param callback
	 */
	public void setCallback(DOLDataRequestCallback callback) {
		this.callback = callback;
	}

	/**
	 * Main method to make API calls
	 * 
	 * @param method
	 * @param arguments
	 */
	public void callAPIMethod(String method, HashMap<String, String> arguments, String numbersKey) {
		StringBuffer url = new StringBuffer(context.getApiHost()
				+ context.getApiURI() + "/" + method);
		StringBuffer queryString = new StringBuffer();

		// Enumerate the arguments and add them to the request
		if (arguments != null) {
			for (HashMap.Entry<String, String> entry : arguments.entrySet()) {
				String key = entry.getKey();
				String value = "";
				try {
					value = URLEncoder.encode(entry.getValue(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				if (key.equals("top") || key.equals("skip")
						|| key.equals("select") || key.equals("orderby")
						|| key.equals("filter")) {
					// If it is the first parameter we append a ?, if not we
					// concatenate with &
					if (queryString.length() == 0) {
						queryString.append("?");
					} else {
						queryString.append("&");
					}
					// append the querystring key and value
					queryString.append("$" + key + "=" + value);
				}

			}
		}

		// If there are valid arguments then append it to the URL
		if (queryString.length() > 0) {
			url.append(queryString.toString());
		}
		// MakeHTTP request
		//makeHTTPRequestAsync(url.toString());
		
		new RequestTask().execute(url.toString(), numbersKey);
	}	
	
	
	/**
	 * Helper class for storing the AsyncTask results
	 * @author antonionieves
	 *
	 */
	private class RequestResults {
		private boolean isError;
		private String result;
		private String numbersKey;
		public RequestResults(boolean isError, String result, String key) {
			super();
			this.isError = isError;
			this.result = result;
			this.numbersKey = key;
		}
	}
	
	/**
	 * Triggers an asynchronous HTTP request to the OData API
	 * @author antonionieves
	 *
	 */
	private class RequestTask extends AsyncTask<String, Void, RequestResults> {

		@Override
		protected RequestResults doInBackground(String... params) {
			try {
				//Httpclient
				HttpClient hclient = new DefaultHttpClient();
				HttpGet request = new HttpGet(params[0]);

				// Authorization Header
				String authHeader = "";

				//Try to get an authorization header
				try {
					authHeader = DOLAPIUtils.getRequestHeader(params[0], context.getApiHost(), context.getApiKey(), context.getApiSecret());
				} catch (final Exception e) {
					// Send error to callback

					return new RequestResults(true, e.getLocalizedMessage(), params[1]);					
				}

				//At this oint we have the hader text. Add it to the request
				request.addHeader("Authorization", authHeader);

				//Specify desired format for the OData service
				request.addHeader("Accept", "application/json");

				//Execute request
				HttpResponse response = hclient.execute(request);

				//Request completed. Check status code
				int statusCode = response.getStatusLine().getStatusCode();

				//If 200, return results to callback
				if (statusCode == HttpStatus.SC_OK) {
					String str = EntityUtils.toString(response.getEntity());					
					return new RequestResults(false, str,params[1]);					

				} else {
					//HTTP status code is not 200; return error.
					String errorMessage;

					switch (statusCode) {
					case 401:
						errorMessage = "Unauthorized";
						break;
					case 400:
						errorMessage = "Bad Request";
						break;
					case 404:
						errorMessage = "Request not found";
						break;
					case 500:
						errorMessage = "Server could not process request";
						break;	
					case 504:
						errorMessage = "Request timed out";
						break;
					default:
						errorMessage = "Error " + statusCode + " returned";
						break;
					}

					return new RequestResults(true, errorMessage,params[1]);

				}
			} catch (IOException e) {

				return new RequestResults(true, e.getLocalizedMessage(),params[1]);

			}
		}

		/**\
		 *  Called after AsyncTask has completed
		 *  From here we must call the callback helpers
		 */
		@Override
		protected void onPostExecute(RequestResults r) {


			if (r.isError) {
				callbackWithError(r.result);
			} else {
				callbackWithResults(r.result, r.numbersKey);
			}

		}		

	}

	/**
	 * Callback method to return results to the caller
	 * 
	 * @param results
	 */
	private void callbackWithResults(final String results, String numbersKey) {
		//Parse JSON
		List<Map<String, String>> objects = DOLAPIUtils.parseJSON(results);
		// return results to the callback
		callback.DOLDataResultsCallback(objects, numbersKey);
	}

	/**
	 * Callback method to return errors to the caller
	 * 
	 * @param error
	 */
	private void callbackWithError(final String error) {
		// Return error to the callback
		callback.DOLDataErrorCallback(error);
	}
	
	
}
