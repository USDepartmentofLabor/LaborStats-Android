package com.current;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class NewsDetails extends Activity {
	
	private Intent mIntent;
	private String sTitle;
	private String sDescription;
	private String sDate;
	private String sPdfUrl = "";
	private String sBrowserUrl = "";
	private String sSecondTitle="";
	private String sSecondSubTitle="";
	
	String[] urls;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newsdetails);
        TextView newsDetails = (TextView) findViewById(R.id.newsdetails);
        Date date = null ;
        //SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        //sDate = dateFormat.format(date);
        mIntent = getIntent();
        if(mIntent != null )
        {
        	sTitle = mIntent.getStringExtra("title");
        	
        	sDate = mIntent.getStringExtra("Date");

        	SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");  
        	try {  
        	     date = format.parse(sDate);  
        	    System.out.println(date);  
        	} catch (ParseException e) {  
        	    // TODO Auto-generated catch block  
        	    e.printStackTrace();  
        	}


        	sDescription = mIntent.getStringExtra("description");
        	sPdfUrl = mIntent.getStringExtra("pdfUrl");
        	sBrowserUrl = mIntent.getStringExtra("browserUrl");
        	sSecondTitle = mIntent.getStringExtra("secondTitle");
        	sSecondSubTitle = mIntent.getStringExtra("secondSubTitle");
        	newsDetails.setText(Html.fromHtml("<b><big>" + sTitle + "</b></big>" + "<br />" + 
                    "<small>" + sDate + "</small>" + "<br />" +  "<br />" +
                    "<big>" +sDescription+"<br />"+sSecondTitle+"<br />"+sSecondSubTitle+ "</big>"+ "<br />" +  "<br />" +
                    "<small>" + sPdfUrl + "</small>" +"<br />" + "<br />"
                    +"<small>" + sBrowserUrl + "</small>"+"<br />" + "<br />"));         	
        }
        
    }

}
