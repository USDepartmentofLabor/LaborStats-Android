package com.current;


import gov.dol.doldata.api.DOLDataContext;
import gov.dol.doldata.api.DOLDataRequest;
import gov.dol.doldata.api.DOLDataRequestCallback;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Numbers extends Activity implements DOLDataRequestCallback {


	private ListAdapter listAdapter;
	private ListView lv;
	int flag = 0;
	int currentDay=0;
	
	int AHEmonth;
	int CPImonth;
	int PEmonth;
	int PPImonth;
	int USEmonth;
	int URmonth;
	int USImonth;
	int ECIqtr;
	int PROqtr;
	int refreshFlag=0;
	
	int month;
	int day;
	int qtr;
	int year;
	DOLDataRequest request = null;
	DOLDataContext dolContext = null;
	// You need to provide your own API key and secret.  Get yours at http://developer.dol.gov
	public final String API_KEY = "";
	public final String SHARED_SECRET = "";
	public final String API_HOST = "http://api.dol.gov";
	public final String API_URI = "/V1";
	
	public static String[] namesList = {
		"Unemployment Insurance Initial Claims:",
		"Consumer Price Index (CPI):",
		"Unemployment Rate:",
		"Payroll Employment:",		
        "Average Hourly Earnings:",
		"Producer Price Index (PPI):",		
        "Employment Cost Index (ECI):",		
		"Productivity:",
		"U.S. Import Price Index:",
		"U.S. Export Price Index:"
		};
		
	
	public List<String> finalTitles = new ArrayList<String>();
	public static HashMap<String, String> hSubTitles = new HashMap<String, String>();
	
	public SharedPreferences shp;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.currentnumbers);
        shp = PreferenceManager.getDefaultSharedPreferences(this);
        lv = (ListView)findViewById(R.id.numbers_listview);		
		listAdapter = new ListAdapter(this);
		lv.setAdapter(listAdapter);
		for(int i =0; i < namesList.length; i++)
			hSubTitles.put(namesList[i], "Loading ....");
		/*dolContext = new DOLDataContext(API_KEY, SHARED_SECRET, API_HOST, API_URI);
    	request = new DOLDataRequest(this, dolContext);*/
    	updateValues();   	
    	
    	
    	
		
    }	
	
	 @Override
	    protected void onResume() {
			super.onResume();
			getSavedItems();

		}
	    
	  private void getSavedItems() {
		 boolean bDolBlsSelected = shp.getBoolean("dol_bls_selected", false);
		 finalTitles.clear();
		 if(!bDolBlsSelected)
		 {			 
			 for(int i =0; i < namesList.length; i++)
				{
				 finalTitles.add(i,namesList[i]);
				}			 	 
		 }
		 else
		 {
			 
				for(int j =0; j < namesList.length; j++)
				{
					if(shp.getBoolean(namesList[j], false))
					{
						finalTitles.add(namesList[j]);
					}				
				}
			 
		 }	    	
	    listAdapter.notifyDataSetChanged();
			
		}
	  
	  public void updateValues()
	  {
		  if(isConnected())
		  {
			  //handler.sendEmptyMessage(101);//show progress dialog .. Loading please wait
		        showProgressDlg();

			  getLatestValues();	    		
			  handler.sendEmptyMessageDelayed(102, 5000);//dismiss progress dialog
			  handler.sendEmptyMessage(100);// update the UI with latest values
		  }
		  else
		  {
			  showAlert();
		  }
	  }
	  
	  private void showAlert() {
		  AlertDialog.Builder builder = new AlertDialog.Builder(this);
		  builder.setTitle("No Internet Connection")
		  .setMessage("An Internet connection is required to access DOL Current Numbers. Please try again later");
		 
		  

		  AlertDialog alert = builder.create();
		  alert.show();
		  for(int i =0; i < namesList.length; i++)
				hSubTitles.put(namesList[i], "Unable to load the data");

	  }

	private boolean isConnected() {
		  NetworkInfo info = ((ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		  if (info == null) {
			  return false;
		  }

		  return info.isConnected();
	  }

	@Override
		public boolean onCreateOptionsMenu(Menu menu) {
		  
		  super.onCreateOptionsMenu(menu);
		  menu.add(0,0,0,"Settings").setIcon(android.R.drawable.ic_menu_preferences);
		  menu.add(0,1,1,"Refresh").setIcon(R.drawable.ic_menu_refresh);
		  return true;
	    }
		  	
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        // Handle item selection
	        switch (item.getItemId()) {
	        case 0:
	        	Intent intent = new Intent(getApplicationContext(),EditNumbers.class);
	        	startActivity(intent);
	        	break;
	        case 1:
	        	updateValues();
	           break;
	        }
	        return super.onOptionsItemSelected(item);

	    }
	    
	   /* private Runnable run_DP = new Runnable()
	    {
	    	public void run(){
	    		Looper.prepare();
	    		handler.sendEmptyMessage(101);//show progress dialog .. Loading please wait
	    		getLatestValues();	    		
	    		handler.sendEmptyMessageDelayed(102, 5000);//dismiss progress dialog
	    		handler.sendEmptyMessage(100);// update the UI with latest values	    		
	    	}			
	    };*/
	    
	    private void getLatestValues() {
	    	
	    	Calendar cal = Calendar.getInstance();
	    	day = cal.get(Calendar.DATE);
	    	month= cal.get(Calendar.MONTH) + 1;
	    	year= cal.get(Calendar.YEAR);
	    	qtr=4;
	    	flag = 0;
	    	
	    	if(dolContext == null) dolContext = new DOLDataContext(API_KEY, SHARED_SECRET, API_HOST, API_URI);
	    	if(request == null)request = new DOLDataRequest(this, dolContext);
	    	
	    	//ifNoValuesInETAArray();
	    	ifNoValuesInCPIArray();
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
			if(shp.getBoolean(namesList[0], false) && shp.getBoolean("bls_selected", false))
			{
				return (finalTitles.size()+2);
			}
			else if(!shp.getBoolean(namesList[0], false) && !shp.getBoolean("bls_selected", false))
			{
				return (finalTitles.size()+2);
			}
			else
			{
				return (finalTitles.size()+1);
			}
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
				convertView = mInflater.inflate(R.layout.list_numbers_item, parent, false);
				holder.title = (TextView) convertView.findViewById(R.id.maintitle);
				holder.subText = (TextView) convertView.findViewById(R.id.subtitle);				
				//holder.chBox = (CheckBox) convertView.findViewById(R.id.checkbox_id);
				holder.headingDol = (TextView) convertView.findViewById(R.id.dol_heading);
				holder.headingBls = (TextView) convertView.findViewById(R.id.bls_heading);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			boolean bDolBlsSelected = shp.getBoolean("dol_bls_selected", false);
			//holder.chBox.setVisibility(View.GONE);
			if(!bDolBlsSelected || shp.getBoolean(namesList[0], false))
			{
				if(position == 0)
				{
					holder.headingDol.setVisibility(View.VISIBLE);
					holder.title.setVisibility(View.GONE);
					holder.subText.setVisibility(View.GONE);
					holder.headingBls.setVisibility(View.GONE);
				}
				else if(position == 1)
				{
					holder.headingBls.setVisibility(View.GONE);
					holder.headingDol.setVisibility(View.GONE);
					holder.title.setVisibility(View.VISIBLE);
					holder.subText.setVisibility(View.VISIBLE);
					holder.title.setText(finalTitles.get(position-1));
					if(hSubTitles.get(finalTitles.get(position-1)) != null)
						holder.subText.setText(hSubTitles.get(finalTitles.get(position-1)));	
				}
				else if(position == 2)
				{
					holder.headingBls.setVisibility(View.VISIBLE);
					holder.title.setVisibility(View.GONE);
					holder.subText.setVisibility(View.GONE);
					holder.headingDol.setVisibility(View.GONE);
				}
				else
				{
					holder.headingBls.setVisibility(View.GONE);
					holder.headingDol.setVisibility(View.GONE);
					holder.title.setVisibility(View.VISIBLE);
					holder.subText.setVisibility(View.VISIBLE);
					holder.title.setText(finalTitles.get(position-2));
					if(hSubTitles.get(finalTitles.get(position-2)) != null)
						holder.subText.setText(hSubTitles.get(finalTitles.get(position-2)));
				}
			}
			else
			{
				if (position == 0)
				{
					holder.headingBls.setVisibility(View.VISIBLE);
					holder.title.setVisibility(View.GONE);
					holder.subText.setVisibility(View.GONE);
					holder.headingDol.setVisibility(View.GONE);
				}
				else
				{
					holder.headingBls.setVisibility(View.GONE);
					holder.headingDol.setVisibility(View.GONE);
					holder.title.setVisibility(View.VISIBLE);
					holder.subText.setVisibility(View.VISIBLE);
					holder.title.setText(finalTitles.get(position-1));
					if(hSubTitles.get(finalTitles.get(position-1)) != null)
						holder.subText.setText(hSubTitles.get(finalTitles.get(position-1)));
				}
				
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
		CheckBox chBox;
	}
	
	
	@Override
	public void DOLDataResultsCallback(List<Map<String, String>> results, String numbersKey) {
		
		if(flag==9 && numbersKey.equals("Unemployment Insurance Initial Claims:"))
		{
			if(results.size()!=0)
			{

			for (Map<String, String> m : results) {
			if(m.get("seasonallyAdjustedInitialClaims")!=null && !m.get("seasonallyAdjustedInitialClaims").contains("null"))
			{
			Calendar cal1 = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");

			cal1.add(Calendar.DATE, currentDay);	
			
//			double parsed = Double.parseDouble(m.get("seasonallyAdjustedInitialClaims"));
//		       String formato = NumberFormat.getCurrencyInstance().format((parsed));
//		       String cleanString = formato.toString().replaceAll("[$]", "");
//		       //float aFloat=Float.valueOf(cleanString.toString());
			
			
			String cleanString = m.get("seasonallyAdjustedInitialClaims").toString().replaceAll("[.]", "");

			int parsed = Integer.parseInt(cleanString);
			
		       String formato = NumberFormat.getCurrencyInstance().format((parsed));
		       
		      
		       String   tmpString = formato.replace( ".00", "");
		       String   tmpString1 = tmpString.replace( "$", "");
		       
			flag=10;
			hSubTitles.put("Unemployment Insurance Initial Claims:",tmpString1+" in the week ending "+dateFormat.format(cal1.getTime()));//Unemployment Insurance Initial Claims:



			//ifNoValuesInCPIArray();

			handler.sendEmptyMessageDelayed(100,100);//Update the UI
			}
			else
			{
			currentDay--;
			ifNoValuesInETAArray();

			}

			}


			}
			else
			{
			currentDay--;
			ifNoValuesInETAArray();
			}
		}	

		else if(flag==0 && numbersKey.equals("Consumer Price Index (CPI):"))
		{

			if(results.size()!=0)
			{
			flag=1;
			String sMonth;
			Calendar cal = Calendar.getInstance();
			CPImonth=month;
			month = cal.get(Calendar.MONTH) + 1;
			year = cal.get(Calendar.YEAR);

			for (Map<String, String> m : results) {

			int aInt = Integer.valueOf(m.get("period").toString());
			sMonth=getMonthForInt(aInt);
			//	
			float aFloat=Float.valueOf(m.get("value").toString());
//			    Float fValue = Float.valueOf(m.get("value"));
//			    String.ValueOf( fValue );

			if(m.get("type").contains("F"))
			{
				if(aFloat>=0)
				{
			hSubTitles.put("Consumer Price Index (CPI):","+"+aFloat+"% in "+sMonth+" "+m.get("year"));//Consumer Price Index (CPI):
				}
				else
				{
					hSubTitles.put("Consumer Price Index (CPI):",aFloat+"% in "+sMonth+" "+m.get("year"));//Consumer Price Index (CPI):

				}
			}
			else
			{
				if(aFloat>=0)
				{
			   hSubTitles.put("Consumer Price Index (CPI):", "+"+aFloat+"% ("+ m.get("type")+")  in "+sMonth+" "+m.get("year"));//Consumer Price Index (CPI):
				}
				else
				{
					hSubTitles.put("Consumer Price Index (CPI):",aFloat+"% ("+ m.get("type")+")  in "+sMonth+" "+m.get("year"));//Consumer Price Index (CPI):

				}

			}
			}
			handler.sendEmptyMessageDelayed(100,100);//Update the UI
			ifNoValuesInURArray();
			}
			else
			{
			month--;
			ifNoValuesInCPIArray();
			}

			// Iterate thru List of results. Add each field to the display List
		}

		else if(flag==1 && numbersKey.equals("Unemployment Rate:"))
		{
			// Create List of strings to populate the listview items

			if(results.size()!=0)
			{
			flag=2;
			URmonth=month;
			Calendar cal = Calendar.getInstance();
			month = cal.get(Calendar.MONTH) + 1;
			year = cal.get(Calendar.YEAR);

			for (Map<String, String> m : results) {
			int aInt = Integer.valueOf(m.get("period").toString());
			String sMonth=getMonthForInt(aInt);
			float aFloat=Float.valueOf(m.get("value").toString());

			if(m.get("type").contains("F"))
			{
			hSubTitles.put("Unemployment Rate:",aFloat+"% in "+sMonth+" "+m.get("year"));//Unemployment Rate:
			}
			else
			{
			hSubTitles.put("Unemployment Rate:", aFloat+"% ("+ m.get("type")+")  in "+sMonth+" "+m.get("year"));

			}
			}
			handler.sendEmptyMessageDelayed(100,100);//Update the UI
			ifNoValuesInPEArray();
			}
			else
			{
			if(month>0)
			{
			month--;
			}
			else
			{
			month=12;
			year--;
			}
			ifNoValuesInURArray();
			}

			// Iterate thru List of results. Add each field to the display List		

		}
		else if(flag==2 && numbersKey.equals("Payroll Employment:"))
		{
			// Create List of strings to populate the listview items

			if(results.size()!=0)
			{
			flag=3;
			PEmonth=month;
			Calendar cal = Calendar.getInstance();
			month = cal.get(Calendar.MONTH) + 1;
			year = cal.get(Calendar.YEAR);

			for (Map<String, String> m : results) {
			int aInt = Integer.valueOf(m.get("period").toString());
			String sMonth=getMonthForInt(aInt);
			float aFloat=Float.valueOf(m.get("value").toString());
			int aaa=(int)aFloat;


			if(m.get("type").contains("F"))
			{
				if(aaa>0)
				{
					hSubTitles.put("Payroll Employment:","+"+aaa+",000"+" in "+sMonth+" "+m.get("year"));//Payroll Employment:

				}
				else if(aaa==0)
				{
					hSubTitles.put("Payroll Employment:",+aaa+" in "+sMonth+" "+m.get("year"));//Payroll Employment:

				}
				else
				{
			hSubTitles.put("Payroll Employment:",aaa+",000"+" in "+sMonth+" "+m.get("year"));//Payroll Employment:
				}
			}
			else
			{
				if(aaa>0)
				{
					hSubTitles.put("Payroll Employment:","+"+ aaa+",000"+" ("+ m.get("type")+")  in "+sMonth+" "+m.get("year"));

				}
				else if(aaa==0)
				{
					hSubTitles.put("Payroll Employment:", aaa+" ("+ m.get("type")+")  in "+sMonth+" "+m.get("year"));

				}
				
				else
				{
			hSubTitles.put("Payroll Employment:", aaa+",000"+" ("+ m.get("type")+")  in "+sMonth+" "+m.get("year"));
				}

			}

			}
			handler.sendEmptyMessageDelayed(100,100);//Update the UI
			ifNoValuesInAHEArray();
			}
			else
			{
			if(month>0)
			{
			month--;
			}
			else
			{
			month=12;
			year--;
			}
			ifNoValuesInPEArray();
			}

			// Iterate thru List of results. Add each field to the display List
		}
		else if(flag==3 && numbersKey.equals("Average Hourly Earnings:"))
		{
			// Create List of strings to populate the listview items

			if(results.size()!=0)
			{
			flag=4;
			AHEmonth=month;
			Calendar cal = Calendar.getInstance();
			month = cal.get(Calendar.MONTH) + 1;
			year = cal.get(Calendar.YEAR);

			for (Map<String, String> m : results) {
			int aInt = Integer.valueOf(m.get("period").toString());
			String sMonth=getMonthForInt(aInt);
			float aFloat=Float.valueOf(m.get("value").toString());

			
			if(m.get("type").contains("F"))
			{
				if(aFloat>=0)
				{
					hSubTitles.put("Average Hourly Earnings:","+$"+String.format("%.2f", aFloat)+" in "+sMonth+" "+m.get("year"));//Average Hourly Earnings:

				}
				else
				{
					aFloat=-(aFloat);
			hSubTitles.put("Average Hourly Earnings:","-$"+String.format("%.2f", aFloat)+" in "+sMonth+" "+m.get("year"));//Average Hourly Earnings:
				}
			}
			else
			{
				if(aFloat>=0)
				{
					hSubTitles.put("Average Hourly Earnings:", "+$"+String.format("%.2g", aFloat)+" ("+ m.get("type")+")  in "+sMonth+" "+m.get("year"));

				}
				else
				{
					aFloat=-(aFloat);
			hSubTitles.put("Average Hourly Earnings:", "-$"+String.format("%.2g", aFloat)+" ("+ m.get("type")+")  in "+sMonth+" "+m.get("year"));
				}

			}

			}	
			handler.sendEmptyMessageDelayed(100,100);//Update the UI
			ifNoValuesInPPIArray();

			}
			else
			{
			if(month>0)
			{
			month--;
			}
			else
			{
			month=12;
			year--;
			}
			ifNoValuesInAHEArray();
			}
			// Iterate thru List of results. Add each field to the display List		

		}
		else if(flag==4 && numbersKey.equals("Producer Price Index (PPI):"))
		{
			// Create List of strings to populate the listview items

			if(results.size()!=0)
			{
			flag=5;
			PPImonth=month;
			Calendar cal = Calendar.getInstance();
			month = cal.get(Calendar.MONTH) + 1;
			year = cal.get(Calendar.YEAR);

			for (Map<String, String> m : results) {
			int aInt = Integer.valueOf(m.get("period").toString());
			String sMonth=getMonthForInt(aInt);
			float aFloat=Float.valueOf(m.get("value").toString());


			if(m.get("type").contains("F"))
			{
				if(aFloat>=0)
				{
					hSubTitles.put("Producer Price Index (PPI):","+"+aFloat+"% in "+sMonth+" "+m.get("year"));//Producer Price Index (PPI):

				}
				else
				{
			hSubTitles.put("Producer Price Index (PPI):",aFloat+"% in "+sMonth+" "+m.get("year"));//Producer Price Index (PPI):
				}
			}
			else
			{
				if(aFloat>=0)
				{
					hSubTitles.put("Producer Price Index (PPI):","+"+aFloat+"% ("+ m.get("type")+")  in "+sMonth+" "+m.get("year"));

				}
				else
				{
			hSubTitles.put("Producer Price Index (PPI):", aFloat+"% ("+ m.get("type")+")  in "+sMonth+" "+m.get("year"));
				}
			}
			}
			handler.sendEmptyMessageDelayed(100,100);//Update the UI
			ifNoValuesInUSIArray();
			}
			else
			{
			if(month>0)
			{
			month--;
			}
			else
			{
			month=12;
			year--;
			}
			ifNoValuesInPPIArray();
			}

			// Iterate thru List of results. Add each field to the display List		

		}
		else if(flag==5 && numbersKey.equals("U.S. Import Price Index:"))
		{
			// Create List of strings to populate the listview items

			if(results.size()!=0)
			{
			flag=6;
			USImonth=month;
			Calendar cal = Calendar.getInstance();
			month = cal.get(Calendar.MONTH) + 1;
			year = cal.get(Calendar.YEAR);

			for (Map<String, String> m : results) {
			int aInt = Integer.valueOf(m.get("period").toString());
			String sMonth=getMonthForInt(aInt);
			float aFloat=Float.valueOf(m.get("value").toString());


			if(m.get("type").contains("F"))
			{
				if(aFloat>=0)
				{
					hSubTitles.put("U.S. Import Price Index:","+"+aFloat+"% in "+sMonth+" "+m.get("year"));//U.S. Import Price Index:

				}
				else
				{
			hSubTitles.put("U.S. Import Price Index:",aFloat+"% in "+sMonth+" "+m.get("year"));//U.S. Import Price Index:
				}
			}
			else
			{
				if(aFloat>=0)
				{
					hSubTitles.put("U.S. Import Price Index:","+"+aFloat+"% ("+ m.get("type")+")  in "+sMonth+" "+m.get("year"));

				}
				else
				{
			hSubTitles.put("U.S. Import Price Index:", aFloat+"% ("+ m.get("type")+")  in "+sMonth+" "+m.get("year"));
				}

			}

			}
			handler.sendEmptyMessageDelayed(100,100);//Update the UI
			ifNoValuesInUSEArray();
			}
			else
			{
			if(month>0)
			{
			month--;
			}
			else
			{
			month=12;
			year--;
			}
			ifNoValuesInUSIArray();
			}
			// Iterate thru List of results. Add each field to the display List	
		}

		else if(flag==6 && numbersKey.equals("U.S. Export Price Index:"))
		{
			// Create List of strings to populate the listview items

			if(results.size()!=0)
			{
			flag=7;
			USEmonth=month;
			Calendar cal = Calendar.getInstance();
			month = cal.get(Calendar.MONTH) + 1;
			year = cal.get(Calendar.YEAR);

			for (Map<String, String> m : results) {
			int aInt = Integer.valueOf(m.get("period").toString());
			String sMonth=getMonthForInt(aInt);
			float aFloat=Float.valueOf(m.get("value").toString());


			if(m.get("type").contains("F"))
			{
				if(aFloat>=0)
				{
					hSubTitles.put("U.S. Export Price Index:","+"+aFloat+"% in "+sMonth+" "+m.get("year"));//U.S. Export Price Index:
	
				}
				else
				{
			hSubTitles.put("U.S. Export Price Index:",aFloat+"% in "+sMonth+" "+m.get("year"));//U.S. Export Price Index:
				}
			
			}
			else
			{
				if(aFloat>=0)
				{
					hSubTitles.put("U.S. Export Price Index:", "+"+aFloat+"% ("+ m.get("type")+")  in "+sMonth+" "+m.get("year"));
	
				}
				else
				{
			hSubTitles.put("U.S. Export Price Index:", aFloat+"% ("+ m.get("type")+")  in "+sMonth+" "+m.get("year"));
				}

			}
			}
			handler.sendEmptyMessageDelayed(100,100);//Update the UI
			ifNoValuesInECIArray();
			Log.i("DisplayArray",""+hSubTitles.toString());
			}
			else
			{
			if(month>0)
			{
			month--;
			}
			else
			{
			month=12;
			year--;
			}
			ifNoValuesInUSEArray();
			}
			// Iterate thru List of results. Add each field to the display List
		}
		else if(flag==7 && numbersKey.equals("Employment Cost Index (ECI):"))
		{
			// Create List of strings to populate the listview items

			if(results.size()!=0)
			{
			flag=8;
			ECIqtr=qtr;
			Calendar cal = Calendar.getInstance();
			qtr=4;                            
			year = cal.get(Calendar.YEAR);

			for (Map<String, String> m : results) {

			int aInt = Integer.valueOf(m.get("period").toString());
			String sQtr=getQtrForInt(aInt);
			float aFloat=Float.valueOf(m.get("value").toString());


			if(m.get("type").contains("F"))
			{
				if(aFloat>=0)
				{
					hSubTitles.put("Employment Cost Index (ECI):","+"+aFloat+"% in "+sQtr+" "+m.get("year"));//Employment Cost Index (ECI):

				}
				else
				{
			hSubTitles.put("Employment Cost Index (ECI):",aFloat+"% in "+sQtr+" "+m.get("year"));//Employment Cost Index (ECI):
				}
			}
			else
			{
				if(aFloat>=0)
				{
					hSubTitles.put("Employment Cost Index (ECI):", "+"+aFloat+"% ("+ m.get("type")+")  in "+sQtr+" "+m.get("year"));

				}
				else
				{
			hSubTitles.put("Employment Cost Index (ECI):", aFloat+"% ("+ m.get("type")+")  in "+sQtr+" "+m.get("year"));
				}
			

			}

			}

			Log.i("DisplayArray",""+hSubTitles.toString());
			handler.sendEmptyMessageDelayed(100,100);//Update the UI
			ifNoValuesInPROArray();
			}
			else
			{
			if(qtr>0)
			{
			qtr--;
			}
			else
			{
			qtr=4;
			year--;
			}
			ifNoValuesInECIArray();
			}

			// Iterate thru List of results. Add each field to the display List		

		}
		else if(flag==8 && numbersKey.equals("Productivity:"))
		{
			// Create List of strings to populate the listview items

			if(results.size()!=0)
			{
			flag=9;
			PROqtr=qtr;
			Calendar cal = Calendar.getInstance();
			qtr=4;
			year = cal.get(Calendar.YEAR);

			for (Map<String, String> m : results) {

//									
//							    	String   tmpString = (m.get("period").replace( 'Q', ' ' ));
//							    	String   tmpString1 = (tmpString.replace( '0', ' ' ));
			//
			//int aInt =
			String sQtr="";



			if(m.get("period").contains("Q01"))
			{

			sQtr="1st Qtr of";
			}
			else if(m.get("period").contains("Q02"))
			{

			sQtr="2nd Qtr of";
			}
			else if(m.get("period").contains("Q03"))
			{

			sQtr="3rd Qtr of";
			}
			else if(m.get("period").contains("Q04"))
			{

			sQtr="4th Qtr of";
			}
			else
			{
			sQtr="invalid";
			}
			float aFloat=Float.valueOf(m.get("value").toString());


			//display.add(m.get("year")+" - "+m.get("value")); 

			if(m.get("type").contains("F"))
			{
				if(aFloat>=0)
				{
					hSubTitles.put("Productivity:","+"+aFloat+"% in "+sQtr+" "+m.get("year"));//Productivity:
	
				}
				else
				{
			hSubTitles.put("Productivity:",aFloat+"% in "+sQtr+" "+m.get("year"));//Productivity:
				}
			
			}
			else
			{
				if(aFloat>=0)
				{
					hSubTitles.put("Productivity:","+"+aFloat+"% ("+ m.get("type")+")  in "+sQtr+" "+m.get("year"));
	
				}
				else
				{
			hSubTitles.put("Productivity:", aFloat+"% ("+ m.get("type")+")  in "+sQtr+" "+m.get("year"));
				}

			}
			}
			handler.sendEmptyMessageDelayed(100,100);//Update the UI
			ifNoValuesInETAArray();
			}
			else
			{
			if(qtr>0)
			{
			qtr--;
			}
			else
			{
			qtr=4;
			year--;
			}	ifNoValuesInPROArray();
			}
			// Iterate thru List of results. Add each field to the display List		

		}



	}
	
	

	String getQtrForInt(int m) {
		String month = "invalid";
		String[] months ={"1st Qtr of","2nd Qtr of","3rd Qtr of","4th Qtr of"};

		if (m >0 && m <= 4 ) {
		month = months[m-1];
		}
		return month;
		}


	@Override
	public void DOLDataErrorCallback(String error) {

	
	AlertDialog alertDialog;
	alertDialog = new AlertDialog.Builder(this).create();
	alertDialog.setTitle("Error");
	if(error.contains("api.dol.gov"))
	{
	alertDialog.setTitle("No Internet Connection");
	error="An Internet connection is required to access DOL Current Numbers. Please try again later";

	}
	else if(error.contains("Unauthorized"))
	{
	error="Sorry we are unable to access DOL Numbers at this moment. Please try again later";

	}
	alertDialog.setMessage(error);
	alertDialog.show();
	for(int i =0; i < namesList.length; i++)
	hSubTitles.put(namesList[i], "Unable to load the data");
	}

	
	public void ifNoValuesInCPIArray()
    {
		/*String method = "statistics/BLS_Numbers/importPriceIndex1MonthChange";
        HashMap < String, String > args = new HashMap< String, String >(3);
		args.put("filter", "(year eq 2011) and (period eq 7)");
		request.callAPIMethod(method, args);*/
		// API method to call
		String method = "statistics/BLS_Numbers/consumerPriceIndex1MonthChange";
        HashMap < String, String > args = new HashMap< String, String >(3);
		args.put("filter", "(year eq "+year+") and (period eq "+month+")");
		request.callAPIMethod(method, args, "Consumer Price Index (CPI):");
    	
    }
	public void ifNoValuesInURArray()
    {
    	
		// API method to call
		String method = "statistics/BLS_Numbers/unemploymentRate";
        HashMap < String, String > args = new HashMap< String, String >(3);
		args.put("filter", "(year eq "+year+") and (period eq "+month+")");
		request.callAPIMethod(method, args, "Unemployment Rate:");
    	
    }
	public void ifNoValuesInPEArray()
    {
    	
		// API method to call
		String method = "statistics/BLS_Numbers/payrollEmployment1MonthNetChange";
        HashMap < String, String > args = new HashMap< String, String >(3);
		args.put("filter", "(year eq "+year+") and (period eq "+month+")");
		request.callAPIMethod(method, args, "Payroll Employment:");
    	
    }
	public void ifNoValuesInAHEArray()
    {
    	
		// API method to call
		String method = "statistics/BLS_Numbers/averageHourlyEarnings1MonthNetChange";
        HashMap < String, String > args = new HashMap< String, String >(3);
		args.put("filter", "(year eq "+year+") and (period eq "+month+")");
		request.callAPIMethod(method, args, "Average Hourly Earnings:");
    	
    }
	public void ifNoValuesInPPIArray()
    {
    	
		// API method to call
		String method = "statistics/BLS_Numbers/producerPriceIndex1MonthChange";
        HashMap < String, String > args = new HashMap< String, String >(3);
		args.put("filter", "(year eq "+year+") and (period eq "+month+")");
		request.callAPIMethod(method, args, "Producer Price Index (PPI):");
    	
    }
	public void ifNoValuesInUSIArray()
    {
    	
		// API method to call
		String method = "statistics/BLS_Numbers/importPriceIndex1MonthChange";
        HashMap < String, String > args = new HashMap< String, String >(3);
		args.put("filter", "(year eq "+year+") and (period eq "+month+")");
		request.callAPIMethod(method, args, "U.S. Import Price Index:");
    	
    }
	public void ifNoValuesInUSEArray()
    {
    	
		// API method to call
		String method = "statistics/BLS_Numbers/exportPriceIndex1MonthChange";
        HashMap < String, String > args = new HashMap< String, String >(3);
		args.put("filter", "(year eq "+year+") and (period eq "+month+")");
		request.callAPIMethod(method, args, "U.S. Export Price Index:");
    	
    }
	public void ifNoValuesInECIArray()
    {
    	
		// API method to call
		String method = "statistics/BLS_Numbers/employmentCostIndex";
        HashMap < String, String > args = new HashMap< String, String >(3);
		args.put("filter", "(year eq "+year+") and (period eq "+qtr+")");
		request.callAPIMethod(method, args, "Employment Cost Index (ECI):");
    	
    }
	public void ifNoValuesInPROArray()
    {
    	
		// API method to call
		String method = "statistics/BLS_Numbers/productivity";
        HashMap < String, String > args = new HashMap< String, String >(3);
		args.put("filter", "(year eq "+year+") and (period eq 'Q0"+qtr+"')");
		request.callAPIMethod(method, args, "Productivity:");
    	
    }
	public void ifNoValuesInETAArray()
    {
		Calendar cal1 = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        cal1.add(Calendar.DATE, currentDay);	      
	      Log.i("dateformat", ""+dateFormat.format(cal1.getTime()));
		String method = "Statistics/OUI_InitialClaims/unemploymentInsuranceInitialClaims";
        HashMap < String, String > args = new HashMap< String, String >(3);
		args.put("filter", "(week eq datetime'"+dateFormat.format(cal1.getTime())+"T00:00:00')");
     	request.callAPIMethod(method, args, "Unemployment Insurance Initial Claims:");
		
    	
    }  
	
	String getMonthForInt(int m) {
	    String month = "invalid";
	    String[] months ={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

	    if (m >0 && m <= 12 ) {
	        month = months[m-1];
	    }
	    return month;
	}
	
	
	
	

}
