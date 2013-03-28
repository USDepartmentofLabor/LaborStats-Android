package com.current;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class EditNumbers extends Activity {

	private ListAdapter listAdapter;
	private ListView lv;		
	
	HashMap<String, Boolean> hMap = new HashMap<String, Boolean>();
	
	public SharedPreferences shp;
	public Button btnDone;
	public Button btnCancel;
	public LinearLayout linearDoneCancel;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.currentnumbers);
        linearDoneCancel = (LinearLayout) findViewById(R.id.linear_done_cancel);
        linearDoneCancel.setVisibility(View.VISIBLE);
        btnDone = (Button)findViewById(R.id.done);
        btnCancel = (Button) findViewById(R.id.cancel);
        btnDone.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
        btnDone.setOnClickListener(done);
        btnCancel.setOnClickListener(cancel); 

        shp = PreferenceManager.getDefaultSharedPreferences(this);
        lv = (ListView)findViewById(R.id.numbers_listview);		
		listAdapter = new ListAdapter(this);
		lv.setAdapter(listAdapter);
		for(int i =0; i < Numbers.namesList.length; i++)
			hMap.put(Numbers.namesList[i], false);
		
		lv.setClickable(true);		

		lv.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position,
                    long id) {
			Log.i("EditNumbers","OnItemSelected");

                if(position !=0 && position !=2){
                    String sKey = "";
                     if(position == 1)
                     {
                         sKey = Numbers.namesList[position-1];
                     }
                     else
                     {
                         sKey = Numbers.namesList[position-2];
                     }
                    CheckBox chBox = (CheckBox) v.findViewById(R.id.checkbox_id);
                    if (chBox.isChecked())
                    {
                        chBox.setChecked(false);
                        hMap.put(sKey, false);
                    }
                    else
                    {
                        chBox.setChecked(true);
                        hMap.put(sKey, true);
                    }
                }
                
            }
        });		

    }
	


	private OnClickListener done = new OnClickListener() {
		public void onClick(View v) {

			saveCheckItems();
			finish();

		}
		
	};
	
	private void saveCheckItems() {	
		boolean bSelected = false;
		boolean bBlsSelected = false;
		for(int i =0; i < Numbers.namesList.length; i++)
		{
			if(hMap.get(Numbers.namesList[i]))
			{
				bSelected = true;
				if(i > 0)
					bBlsSelected = true;
				shp.edit().putBoolean(Numbers.namesList[i], true).commit();				
			}
			
		}		
		if(bSelected)
		{
			shp.edit().putBoolean("dol_bls_selected", true).commit();
			if(bBlsSelected) 
				shp.edit().putBoolean("bls_selected", true).commit();
			else
				shp.edit().putBoolean("bls_selected", false).commit();
			for(int i =0; i < Numbers.namesList.length; i++)
			{
				if(!hMap.get(Numbers.namesList[i]))
				{
					shp.edit().putBoolean(Numbers.namesList[i], false).commit();
				}
			}
			
		}		
		
	}
	
	private OnClickListener cancel = new OnClickListener() {
		public void onClick(View v) {

			finish();

		}
	};
	
	 @Override
	    protected void onResume() {
			super.onResume();			

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
			return (Numbers.namesList.length + 2);
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
			final NameHolder nameHolder = new NameHolder();
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.list_item, parent, false);
				holder.title = (TextView) convertView.findViewById(R.id.maintitle);
				holder.subText = (TextView) convertView.findViewById(R.id.subtitle);				
				holder.chBox = (CheckBox) convertView.findViewById(R.id.checkbox_id);
				holder.headingDol = (TextView) convertView.findViewById(R.id.dol_heading);
				holder.headingBls = (TextView) convertView.findViewById(R.id.bls_heading);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}	

			if(position == 0)
			{
				holder.headingDol.setVisibility(View.VISIBLE);
				holder.title.setVisibility(View.GONE);
				holder.subText.setVisibility(View.GONE);
				holder.headingBls.setVisibility(View.GONE);
				holder.chBox.setVisibility(View.GONE);
			}
			else if(position == 1)
			{
				nameHolder.sName = Numbers.namesList[position-1];
				holder.headingBls.setVisibility(View.GONE);
				holder.headingDol.setVisibility(View.GONE);
				holder.title.setVisibility(View.VISIBLE);
				holder.subText.setVisibility(View.VISIBLE);
				holder.chBox.setVisibility(View.VISIBLE);
				holder.title.setText(Numbers.namesList[position-1]);
				if(Numbers.hSubTitles.get(Numbers.namesList[position-1]) != null)
					holder.subText.setText(Numbers.hSubTitles.get(Numbers.namesList[position-1]));
				if(hMap.get(Numbers.namesList[position-1]))
					holder.chBox.setChecked(true);
				else
					holder.chBox.setChecked(false);
			}
			else if(position == 2)
			{
				holder.headingBls.setVisibility(View.VISIBLE);
				holder.title.setVisibility(View.GONE);
				holder.subText.setVisibility(View.GONE);
				holder.headingDol.setVisibility(View.GONE);
				holder.chBox.setVisibility(View.GONE);
			}
			else
			{
				nameHolder.sName = Numbers.namesList[position-2];
				holder.headingBls.setVisibility(View.GONE);
				holder.headingDol.setVisibility(View.GONE);
				holder.title.setVisibility(View.VISIBLE);
				holder.subText.setVisibility(View.VISIBLE);
				holder.chBox.setVisibility(View.VISIBLE);
				holder.title.setText(Numbers.namesList[position-2]);
				if(Numbers.hSubTitles.get(Numbers.namesList[position-2]) != null)
					holder.subText.setText(Numbers.hSubTitles.get(Numbers.namesList[position-2]));
				if(hMap.get(Numbers.namesList[position-2]))
					holder.chBox.setChecked(true);
				else
					holder.chBox.setChecked(false);
			}
			
			holder.chBox.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					String key = nameHolder.sName;
					CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkbox_id);
	                   Log.i("EditNumbers","CheckBox on click 2 clicked");

					hMap.put(key, checkBox.isChecked());					

				}
			});
			

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
	class NameHolder {
		String sName;		
	}
	
    



}
;