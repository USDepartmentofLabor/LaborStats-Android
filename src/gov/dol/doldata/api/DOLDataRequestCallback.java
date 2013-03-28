package gov.dol.doldata.api;

import java.util.List;
import java.util.Map;

public interface DOLDataRequestCallback {
	//Return results
	public void DOLDataResultsCallback(List<Map<String, String>> results, String numbersKey);
	
	//Error Callback
	public void DOLDataErrorCallback(String error);
}