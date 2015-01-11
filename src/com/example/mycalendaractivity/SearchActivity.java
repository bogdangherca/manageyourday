package com.example.mycalendaractivity;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity {
	
	private TextView searchText;
	private DatabaseManager dm;
	private DatePicker startDate;
	private DatePicker endDate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		searchText = (TextView) this.findViewById(R.id.searchText);
		startDate = (DatePicker) findViewById(R.id.datePicker1);
		endDate = (DatePicker) findViewById(R.id.datePicker2);
		
		dm = new DatabaseManager(getApplicationContext());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onSearchTask(View v) {
		// search the db, filter results and display results
		SQLiteDatabase db = dm.getReadableDatabase();
		
		String[] columns = {"date", "subject", "hour", "importance"};
		String whereClause = "subject LIKE '%" + searchText.getText().toString() + "%'";
		String sortOrder = "hour" + " ASC";
		
		try {
			Cursor c = db.query("Tasks", 
					columns,
					whereClause,
					null,
					null,
					null,
					sortOrder);
			
			if (c.getCount() == 0)
			{
				Toast.makeText(getApplicationContext(), "No tasks found", Toast.LENGTH_LONG).show();
				return;
			} else {
				// populate list with found tasks
				boolean filterFlag = filterByDateFlag();
				populateSearchResult(c, filterFlag);
				 
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getLocalizedMessage() + "There was en error with this action. Please try again.", Toast.LENGTH_LONG).show();
			return;
		}
	}
	
	public boolean filterByDateFlag() {
		int startDay = startDate.getDayOfMonth();
		int startMonth = startDate.getMonth() + 1;
		int endDay = endDate.getDayOfMonth();
		int endMonth = endDate.getMonth() + 1;
		
		// ignore if case
		if (startMonth == endMonth && startDay == endDay) {
			return false;
		}
		return true;
	}
	
	public int monthStr2Int(String month) {
		Map<String, Integer> monthMap = new HashMap<String, Integer>(){{
			put("January", new Integer(1));
			put("February", new Integer(2));
			put("March", new Integer(3));
			put("April", new Integer(4));
			put("May", new Integer(5));
			put("June", new Integer(6));
			put("July", new Integer(7));
			put("August", new Integer(8));
			put("September", new Integer(9));
			put("Octomber", new Integer(10));
			put("November", new Integer(11));
			put("December", new Integer(12));
		}};
		return monthMap.get(monthMap).intValue();
	}
	
	public void populateSearchResult(Cursor c, boolean filterByDate) {
		TableLayout tl = (TableLayout) findViewById(R.id.displayLinear);
		tl.removeAllViews();
		//ll.addView(tl);
		
		TextView iID = new TextView(getApplicationContext());
		iID.setText("Task date");
		iID.setTextColor(Color.rgb(204, 102, 0));
		iID.setGravity(Gravity.CENTER);
		iID.setPadding(10, 0, 10, 0);
		
		TextView sbID = new TextView(getApplicationContext());
		sbID.setText("Task subject");
		sbID.setTextColor(Color.rgb(204, 102, 0));
		sbID.setGravity(Gravity.CENTER);
		sbID.setPadding(10, 0, 10, 0);
		
		TextView hID = new TextView(getApplicationContext());
		hID.setText("End time");
		hID.setTextColor(Color.rgb(204, 102, 0));
		hID.setGravity(Gravity.CENTER);
		hID.setPadding(10, 0, 10, 0);
		
		TextView imID = new TextView(getApplicationContext());
		imID.setText("Priority");
		imID.setTextColor(Color.rgb(204, 102, 0));
		imID.setGravity(Gravity.CENTER);
		imID.setPadding(10, 0, 10, 0);
		
		TableRow rowH = new TableRow(getApplicationContext());
		rowH.addView(iID);
		rowH.addView(sbID);
		rowH.addView(hID);
		rowH.addView(imID);
		
		tl.addView(rowH);
		
		int lines = c.getCount();
		c.moveToFirst();
		for (int j = 0 ; j < lines ; j++) {
			String date = c.getString(c.getColumnIndexOrThrow("date"));
			String sb = c.getString(c.getColumnIndexOrThrow("subject"));
			String h = c.getString(c.getColumnIndexOrThrow("hour"));
			String im = c.getString(c.getColumnIndexOrThrow("importance"));
			
			// make date human readable
			String[] splitStr = date.split("-");
			int day = Integer.parseInt(splitStr[0]);
			int month = Integer.parseInt(splitStr[0]);
			StringBuilder dateStrBuilder = new StringBuilder();
			for (int i=0; i<splitStr.length; i++) {
				dateStrBuilder.append(splitStr[i] + " ");
			}
			String dateStr = dateStrBuilder.toString().substring(0, dateStrBuilder.toString().length());
			
			// filter by date if case
			if (filterByDate == true) {
				int startDay = startDate.getDayOfMonth();
				int startMonth = startDate.getMonth() + 1;
				int endDay = endDate.getDayOfMonth();
				int endMonth = endDate.getMonth() + 1;
				
				// ignore if not in selected date interval
				if (month < startMonth && day < startDay) {
					continue;
				}
				if (month > endMonth && day > endDay) {
					continue;
				}
			}
			
			int color = 0;
			if (im.equals("High"))
				color = Color.RED;
			else if (im.equals("Medium"))
				color = Color.BLACK;
			else
				color = Color.BLUE;
				
			
			TextView idV = new TextView(getApplicationContext());
			idV.setText(dateStr + "");
			idV.setTextColor(color);
			idV.setGravity(Gravity.CENTER);
			idV.setPadding(10, 0, 10, 0);
			
			TextView sbV = new TextView(getApplicationContext());
			sbV.setText(sb);
			sbV.setTextColor(color);
			sbV.setGravity(Gravity.CENTER);
			sbV.setPadding(10, 0, 10, 0);
			
			TextView hV = new TextView(getApplicationContext());
			hV.setText(h);
			hV.setTextColor(color);
			hV.setGravity(Gravity.CENTER);
			hV.setPadding(10, 0, 10, 0);
			
			TextView imV = new TextView(getApplicationContext());
			imV.setText(im);
			imV.setTextColor(color);
			imV.setGravity(Gravity.CENTER);
			imV.setPadding(10, 0, 10, 0);
			
			TableRow row = new TableRow(getApplicationContext());

			row.addView(idV);
	        row.addView(sbV);
	        row.addView(hV);
	        row.addView(imV);
	        
	        tl.addView(row);
			
			if (j < lines - 1)
				c.moveToNext();
		}
	}
}
