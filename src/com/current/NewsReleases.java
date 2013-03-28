package com.current;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class NewsReleases extends Activity {

	private ListAdapter listAdapter;
	private ListView lv;
	String thisLine;
    URL u;
    URLConnection feedUrl;
    
	String[] feeds = {"http://www.dol.gov/rss/news-ui.xml",
			"http://www.bls.gov/include/govdelivery/cpi.rss",
			"http://www.bls.gov/include/govdelivery/empsit.rss",
			"http://www.bls.gov/include/govdelivery/ppi.rss",
			"http://www.bls.gov/include/govdelivery/prod2.rss",
			"http://www.bls.gov/include/govdelivery/ximpim.rss",
			};
	
	public String[] sTitles = {"Unemployment Insurance Initial Claims Report",
			"CPI - Consumer Price Index",
			"EMPSIT - Employment Situation",
			"PPI - Producer Price Index",
			"PROD2 - Productivity and Costs",
			"XIMPIM - U.S. Import and Export Price Indexes"
			};
	
	public static HashMap<String, String> hSubTitles = new HashMap<String, String>();
	public static HashMap<String, String> hDate = new HashMap<String, String>();

	public static HashMap<String, String> hSecondTitles = new HashMap<String, String>();
	public static HashMap<String, String> hSecondSubTitles = new HashMap<String, String>();


	public static HashMap<String, String> hPdfUrls = new HashMap<String, String>();
	public static HashMap<String, String> hBrowserUrls = new HashMap<String, String>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newsreleases);        
        lv = (ListView)findViewById(R.id.news_listview);		
		listAdapter = new ListAdapter(this);
		lv.setAdapter(listAdapter);
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				
				if(position != 0 && position != 2){
					int index = -1;
					String sName;
					String sDescription = "Data not available.";
					String sDate = "";
					String sSecondTitle = "";
					String sSecondSubTitle = "";
					String sPdfUrl = "";
					String sBrowserUrl = "";
					
					if(position == 1)
						index = position -1;
					else if (position > 2)
						index = position -2;
					if(index == -1)
						return;

					sName = sTitles[index];
					if(hSubTitles.get(sTitles[index]) != null)
						sDescription = hSubTitles.get(sTitles[index]);				
					if(hPdfUrls.get(sTitles[index]) != null)
						sPdfUrl = hPdfUrls.get(sTitles[index]);
					if(hSecondSubTitles.get(sTitles[index]) != null)
						sSecondSubTitle = hSecondSubTitles.get(sTitles[index]);
					if(hDate.get(sTitles[index]) != null)
						sDate = hDate.get(sTitles[index]);
					if(hSecondTitles.get(sTitles[index]) != null)
						sSecondTitle = hSecondTitles.get(sTitles[index]);
					if(hBrowserUrls.get(sTitles[index]) != null)
						sBrowserUrl = hBrowserUrls.get(sTitles[index]);

					Intent intent = new Intent(getApplicationContext(),NewsDetails.class);
					intent.putExtra("title", sName);
					intent.putExtra("description", sDescription);
					intent.putExtra("secondTitle", sSecondTitle);
					intent.putExtra("secondSubTitle", sSecondSubTitle);
					intent.putExtra("Date", sDate);

					intent.putExtra("pdfUrl", sPdfUrl);
					intent.putExtra("browserUrl", sBrowserUrl);
					startActivity(intent);
					
					for(int i =0; i < sTitles.length; i++)
						hSubTitles.put(sTitles[i], "Loading ....");
				}

			}
		});
		
		if(isConnected())
   	  {
			Thread thread = new Thread(null,run_GD,"runGetData");
	        thread.start();   	  }
     	else
   	  {

   	  showAlert();

   	  }
		
		
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);
		menu.add(0,0,0,"Refresh").setIcon(R.drawable.ic_menu_refresh);
		return true;
	}
	  	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case 0:
        	
        	 if(isConnected())
         	  {
      			Thread thread = new Thread(null,run_GD,"runGetData");
      	        thread.start();   	  }
           	else
         	  {

         	  showAlert();

         	  }
           break;
        }
        return super.onOptionsItemSelected(item);

    }
	
	private Runnable run_GD = new Runnable()
    {
    	public void run(){
    		Looper.prepare();
    		handler.sendEmptyMessage(101);//show progress dialog .. Loading please wait
    		getData();
    		handler.sendEmptyMessageDelayed(102, 2000);//dismiss progress dialog
    		handler.sendEmptyMessage(100);// update the UI with latest values	    		
    	}			
    };
    
    private void getData() {
		
    	for(int i = 0; i < feeds.length; i++)
    	{
    		parseData(feeds[i], i);
    		
    	}
		
	}
    
    private void parseData(String feed, int position) {

    	try {
    		String title = "";
    		String subText = "";
    		String SecondTitle = "";
    		String SecondsubTitle = "";

    		String pdfUrl = "";
    		String browserUrl = "";
    		String Date = "";


    		feedUrl = new URL(feed).openConnection();

    		InputStream in = feedUrl.getInputStream();
    		String json = convertStreamToString(in);
    		Log.i("ContentParsing","json => "+json);
    		if(feed.contains(feeds[0]))
    		{
    			title=flatteningXmlTitleString(json);
    			subText=flatteningXmlSubTitleString(json);
        		browserUrl = flatteningXmlLinkString(json);
    		}
    		else
    		{
    			title=flatteningHtmlTitleString(json);
    			subText=flatteningHtmlSubTitleString(json);
    			SecondTitle=flatteningHtmlSecondTitleString(json);
    			Date=flatteningHtmlDateTitleString(json);

    			SecondsubTitle=flatteningHtmlSecondSubTitleString(json);
    			pdfUrl = flatteningHtmlPdfLinkTitleString(json);
        		browserUrl = flatteningHtmlBrowserLinkTitleString(json);
    		}
    		
    		if(title.length() > 1)
    			sTitles[position] = title;
    		if(subText.length() > 1)
    			hSubTitles.put(sTitles[position], subText);
    		
    		if(SecondTitle.length() > 1 )
    			hSecondTitles.put(sTitles[position], ""+SecondTitle);
    		if(Date.length() > 1 )
    			hDate.put(sTitles[position], ""+Date);
    		if(SecondsubTitle.length() > 1 )
    			hSecondSubTitles.put(sTitles[position], ""+SecondsubTitle);
    		if(pdfUrl.length() > 1 )
    			hPdfUrls.put(sTitles[position], "PDF : "+pdfUrl);
    		if(browserUrl.length() > 1 )
    			hBrowserUrls.put(sTitles[position], "URL : "+browserUrl);

    	}
    	catch (MalformedURLException e) {
    		Log.v("ERROR","MALFORMED URL EXCEPTION");
    	} catch (IOException e) {
    		e.printStackTrace();
    	}

    }
    
   

	Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg){
			switch(msg.what)
			{
			case 100:
				if(listAdapter != null)listAdapter.notifyDataSetChanged();// update the UI with latest values
				break;
			case 101:
				showProgressDlg();
				break;
			case 102:
				dismissDlg();
				break;
			}

		}
	};
	
	ProgressDialog pDialog = null;
	private void showProgressDlg() {		
		
		pDialog = new ProgressDialog(this);		
		pDialog.setMessage("Loading. Please wait...");
		pDialog.setCancelable(false);
		pDialog.show();
	}
	private void dismissDlg() {		
		
		if(pDialog != null)pDialog.dismiss();
	}
	
	private class ListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context cxt;
		
		public ListAdapter(Context context) {
			//super(context, c, true);
			cxt = context;			
			// Cache the LayoutInflate to avoid asking for a new one each time.
			mInflater = LayoutInflater.from(cxt);
		}

		public int getCount() {
			return (sTitles.length+2);
		}

		public int getViewTypeCount() {
			return 3;
		}
		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;			
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.newsrelease_list_item, parent, false);
				holder.title = (TextView) convertView.findViewById(R.id.news_title);
				holder.subText = (TextView) convertView.findViewById(R.id.news_subtext);
				holder.imageView = (ImageView) convertView.findViewById(R.id.arrow);
				holder.headingDol = (TextView) convertView.findViewById(R.id.nr_dol_heading);
				holder.headingBls = (TextView) convertView.findViewById(R.id.nr_bls_heading);
				

			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if(position == 0)
			{
				holder.headingDol.setVisibility(View.VISIBLE);
				holder.title.setVisibility(View.GONE);
				holder.subText.setVisibility(View.GONE);
				holder.headingBls.setVisibility(View.GONE);
				holder.imageView.setVisibility(View.GONE);
			}
			else if(position == 1)
			{
				holder.headingBls.setVisibility(View.GONE);
				holder.headingDol.setVisibility(View.GONE);
				holder.title.setVisibility(View.VISIBLE);
				holder.subText.setVisibility(View.VISIBLE);
				holder.imageView.setVisibility(View.VISIBLE);
				holder.title.setText(sTitles[position-1]);				
				if(hSubTitles.get(sTitles[position-1]) != null)
					holder.subText.setText(hSubTitles.get(sTitles[position-1]));				
			}
			else if(position == 2)
			{
				holder.headingBls.setVisibility(View.VISIBLE);
				holder.title.setVisibility(View.GONE);
				holder.subText.setVisibility(View.GONE);
				holder.headingDol.setVisibility(View.GONE);
				holder.imageView.setVisibility(View.GONE);
			}
			else
			{
				holder.headingBls.setVisibility(View.GONE);
				holder.headingDol.setVisibility(View.GONE);
				holder.title.setVisibility(View.VISIBLE);
				holder.subText.setVisibility(View.VISIBLE);
				holder.imageView.setVisibility(View.VISIBLE);
				holder.title.setText(sTitles[position-2]);
				if(hSubTitles.get(sTitles[position-2]) != null)
					holder.subText.setText(hSubTitles.get(sTitles[position-2]));
			}									
			convertView.setTag(holder);
			return convertView;
		}			

	}

	class ViewHolder {
		TextView headingDol;
		TextView headingBls;
		TextView title;
		TextView subText;
		ImageView imageView;
		
	}
	
	 public String flatteningHtmlTitleString(String incomingString)
	    {
	    	String[] separated = incomingString.split("title");
	    	String   tmpString = separated[1].replace( ">", "");
	    	String   tmpString1 = tmpString.replace( "<", "");
	    	String   tmpString2 = tmpString1.replace( "/", "");

			return tmpString2;
	    	
	    }
	    
		public String flatteningHtmlSubTitleString(String incomingString)
	    {
	    	String[] separated = incomingString.split("subtitle");
	    	String   tmpString = separated[1].replace( ">", "");
	    	String   tmpString1 = tmpString.replace( "<", "");
	    	String   tmpString2 = tmpString1.replace( "/", "");
	    	//String   tmpString3 = tmpString2.replace( '-', );

			return tmpString2;
	    	
	    }
	    public String flatteningHtmlSecondTitleString(String incomingString)
	    {
	    	String[] separated = incomingString.split("h4");
	    	String[] separated3 = separated[1].split("/a");
	         String[] separated1 = separated3[0].split("underline;");
	    	String   tmpString = separated1[1].replace( ">", "");
	    	String   tmpString1 = tmpString.replace( "<", "");
	    	String   tmpString2 = tmpString1.replace( "/", "");
	    	String   tmpString3 = tmpString2.replace( '"', ' ' );

	        Log.i("table title", ""+tmpString3);
			return tmpString3;
	    	
	    }
	    public String flatteningHtmlSecondSubTitleString(String incomingString)
	    {
	    	String[] separated = incomingString.split("p style");
	    	String[] separated3 = separated[1].split("0px;");
	         String[] separated1 = separated3[1].split("/p");
	    	String   tmpString = separated1[0].replace( ">", "");
	    	String   tmpString1 = tmpString.replace( "<", "");
	    	String   tmpString2 = tmpString1.replace( "/", "");
	    	String   tmpString3 = tmpString2.replace( '"', ' ' );

	        Log.i("table title", ""+tmpString3);
			return tmpString3;
	    	
	    }
	    public String flatteningHtmlPdfLinkTitleString(String incomingString)
	    {
	    	String[] separated = incomingString.split("link type=");
	    	String[] separated3 = separated[1].split("href=");
	    	String[] separated1 = separated3[1].split("/>");
	    	String   tmpString = separated1[0].replace( ">", "");
	    	String   tmpString1 = tmpString.replace( "<", "");
	    	//String   tmpString2 = tmpString1.replace( '/', ' ' );
	    	String   tmpString3 = tmpString1.replace( '"', ' ' );

	    	Log.i("table title", ""+tmpString3);
	    	return tmpString3;

	    }
	    public String flatteningHtmlBrowserLinkTitleString(String incomingString)
	    {
	    	String[] separated = incomingString.split("h4");
	    	String[] separated3 = separated[1].split("href=");
	    	String[] separated1 = separated3[1].split("style");
	    	String   tmpString = separated1[0].replace( ">", "");
	    	String   tmpString1 = tmpString.replace( "<", "");
	    	//String   tmpString2 = tmpString1.replace( '/', ' ' );
	    	String   tmpString3 = tmpString1.replace( '"', ' ' );

	    	Log.i("table title", ""+tmpString3);
	    	return tmpString3;

	    }
	    public String flatteningHtmlDateTitleString(String incomingString)
	    {
	    	String[] separated = incomingString.split("<updated>");
	    	String[] separated3 = separated[1].split("</updated>");
	    	String[] separated1 = separated3[0].split("T");
	    	String   tmpString = separated1[0].replace( '>', ' ' );
	    	String   tmpString1 = tmpString.replace( '<', ' ' );
	    	//String   tmpString2 = tmpString1.replace( '/', ' ' );
	    	String   tmpString3 = tmpString1.replace( '"', ' ' );

	    	Log.i("table title", ""+tmpString3);
	    	return separated1[0];

	    }
	    
	    public String flatteningXmlTitleString(String incomingString)
	    {
	    	String[] separated = incomingString.split("<item>");
	    	String[] separated3 = separated[2].split("<title>");
	    	String[] separated1 = separated3[1].split("</title>");
	    	//String[] separated2 = separated1[1].split("</title>");	    	

	    	Log.i("table title", ""+separated1[0]);
	    	return separated1[0];

	    }	
	    
	    public String flatteningXmlSubTitleString(String incomingString)
	    {
	    	String[] separated = incomingString.split("<item>");
	    	String[] separated3 = separated[2].split("<description>");
	    	String[] separated1 = separated3[1].split("</description>");
	    	//String[] separated2 = separated1[1].split("</title>");	    	

	    	Log.i("table title", ""+separated1[0]);
	    	return separated1[0];

	    }	
	    
	    public String flatteningXmlLinkString(String incomingString)
	    {
	    	String[] separated = incomingString.split("<item>");
	    	String[] separated3 = separated[2].split("<link>");
	    	String[] separated1 = separated3[1].split("</link>");
	    	//String[] separated2 = separated1[1].split("</title>");	    	

	    	Log.i("table title", ""+separated1[0]);
	    	return separated1[0];

	    }	
	    
	    private static String convertStreamToString(InputStream is) throws UnsupportedEncodingException {
	    	  BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
	    	  StringBuilder sb = new StringBuilder();
	    	  String line = null;
	    	  try {
	    	    while ((line = reader.readLine()) != null) {
	    	      sb.append(line + "\n");
	    	    }
	    	  } catch (IOException e) {
	    	    e.printStackTrace();
	    	  } finally {
	    	    try {
	    	      is.close();
	    	  } catch (IOException e) {
	    	    e.printStackTrace();
	    	  }
	    	}
	    	  String str = new String(sb.toString().getBytes("ISO-8859-1"), "UTF-8");

	    	  return str;
	    	}
	    
	    
	    private void showAlert() {
	  	  AlertDialog.Builder builder =

	  	  new AlertDialog.Builder(this);
	  	 builder.setTitle(

		  "NO Internet Connection ")
		  .setMessage(

		  "An Internet connection is required to access DOL News Releases. Please try again later.");
		  
	  	  

	  	  AlertDialog alert = builder.create();

	  	  alert.show();
	  	for(int i =0; i < sTitles.length; i++)
			hSubTitles.put(sTitles[i], "Unable to load the data");

	  	  }

	  	  private boolean isConnected() {
	  	  NetworkInfo info = ((ConnectivityManager)getApplicationContext().getSystemService(Context.

	  	  CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
	  	  if (info == null) {
	  	  return false;
	  	  }

	  	  return info.isConnected();
	  	  }
	    public class ExampleHandler extends DefaultHandler { 
	        StringBuffer buff = null;
	        boolean buffering = false; 
	        
	        @Override
	        public void startDocument() throws SAXException {
	            // Some sort of setting up work
	        } 
	        
	        @Override
	        public void endDocument() throws SAXException {
	            // Some sort of finishing up work
	        } 
	        
	        @Override
	        public void startElement(String namespaceURI, String localName, String qName, 
	                Attributes atts) throws SAXException {
	            if (localName.equals("qwerasdf")) {
	                /*buff = new StringBuffer("");
	                buffering = true;*/
	            }   
	        } 
	        
	        @Override
	        public void characters(char ch[], int start, int length) {
	            if(buffering) {
	                buff.append(ch, start, length);
	            }
	        } 
	        
	        @Override
	        public void endElement(String namespaceURI, String localName, String qName) 
	        throws SAXException {
	            if (localName.equals("title")) {
	               /* buffering = false; 
	                String content = buff.toString();
	                
	                Log.v("parsed one", ""+content);*/
	                
	                // Do something with the full text content that we've just parsed
	            }
	        }
	    }
    
}
