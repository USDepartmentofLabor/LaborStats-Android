package gov.dol.doldata.api;

public class DOLDataContext {
	private String apiKey;
	private String apiSecret;
	private String apiHost;
	private String apiURI;
	
	/**
	 * @param apiKey
	 * @param apiSecret
	 * @param apiHost
	 * @param apiURI
	 */
	public DOLDataContext(String apiKey, String apiSecret, String apiHost,
			String apiURI) {
		super();
		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
		this.apiHost = apiHost;
		this.apiURI = apiURI;
	}
	
	/**
	 * @return the apiKey
	 */
	public String getApiKey() {
		return apiKey;
	}
	/**
	 * @param apiKey the apiKey to set
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	/**
	 * @return the apiSecret
	 */
	public String getApiSecret() {
		return apiSecret;
	}
	/**
	 * @param apiSecret the apiSecret to set
	 */
	public void setApiSecret(String apiSecret) {
		this.apiSecret = apiSecret;
	}
	/**
	 * @return the apiHost
	 */
	public String getApiHost() {
		return apiHost;
	}
	/**
	 * @param apiHost the apiHost to set
	 */
	public void setApiHost(String apiHost) {
		this.apiHost = apiHost;
	}
	/**
	 * @return the apiURI
	 */
	public String getApiURI() {
		return apiURI;
	}
	/**
	 * @param apiURI the apiURI to set
	 */
	public void setApiURI(String apiURI) {
		this.apiURI = apiURI;
	}
}
