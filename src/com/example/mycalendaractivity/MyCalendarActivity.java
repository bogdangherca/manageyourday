package com.example.mycalendaractivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(3)
public class MyCalendarActivity extends Activity implements OnClickListener {
	private static final String tag = "MyCalendarActivity";
	static final String YEAR = "com.MyCalendar.YEAR";
	static final String MONTH = "com.MyCalendar.MONTH";
	static final String DAY = "com.MyCalendar.DAY";
	static final String TITLE = "com.MyCalendar.TITLE";
	static final String DESCRIPTION = "com.MyCalendar.DESCRIPTION";
	static final String HOUR = "com.MyCalendar.HOUR";
	static final String MIN = "com.MyCalendar.MIN";
	static final String IMP = "com.MyCalendar.IMP";
	private TextView currentMonth;
	private Button selectedDayMonthYearButton;
	private ImageView prevMonth;
	private ImageView nextMonth;
	private GridView calendarView;
	private GridCellAdapter adapter;
	private Calendar _calendar;
	@SuppressLint("NewApi")
	private int month, year;
	@SuppressWarnings("unused")
	@SuppressLint({ "NewApi", "NewApi", "NewApi", "NewApi" })
	private final DateFormat dateFormatter = new DateFormat();
	private static final String dateTemplate = "MMMM yyyy";
	private DatabaseManager dm;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_calendar);

		_calendar = Calendar.getInstance(Locale.getDefault());
		month = _calendar.get(Calendar.MONTH) + 1;
		year = _calendar.get(Calendar.YEAR);
		Log.d(tag, "Calendar Instance:= " + "Month: " + month + " " + "Year: "
				+ year);

		selectedDayMonthYearButton = (Button) this
				.findViewById(R.id.selectedDayMonthYear);
		selectedDayMonthYearButton.setText("Selected: ");

		prevMonth = (ImageView) this.findViewById(R.id.prevMonth);
		prevMonth.setOnClickListener(this);

		currentMonth = (TextView) this.findViewById(R.id.currentMonth);
		currentMonth.setText(DateFormat.format(dateTemplate,
				_calendar.getTime()));

		nextMonth = (ImageView) this.findViewById(R.id.nextMonth);
		nextMonth.setOnClickListener(this);

		calendarView = (GridView) this.findViewById(R.id.calendar);

		// Initialised
		adapter = new GridCellAdapter(getApplicationContext(),
				R.id.calendar_day_gridcell, month, year);
		adapter.notifyDataSetChanged();
		calendarView.setAdapter(adapter);
		dm = DatabaseCreator.getHelper(getApplicationContext());
	}

	/**
	 * 
	 * @param month
	 * @param year
	 */
	private void setGridCellAdapterToDate(int month, int year) {
		adapter = new GridCellAdapter(getApplicationContext(),
				R.id.calendar_day_gridcell, month, year);
		_calendar.set(year, month - 1, _calendar.get(Calendar.DAY_OF_MONTH));
		currentMonth.setText(DateFormat.format(dateTemplate,
				_calendar.getTime()));
		adapter.notifyDataSetChanged();
		calendarView.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		if (v == prevMonth) {
			if (month <= 1) {
				month = 12;
				year--;
			} else {
				month--;
			}
			Log.d(tag, "Setting Prev Month in GridCellAdapter: " + "Month: "
					+ month + " Year: " + year);
			setGridCellAdapterToDate(month, year);
		}
		if (v == nextMonth) {
			if (month > 11) {
				month = 1;
				year++;
			} else {
				month++;
			}
			Log.d(tag, "Setting Next Month in GridCellAdapter: " + "Month: "
					+ month + " Year: " + year);
			setGridCellAdapterToDate(month, year);
		}

	}

	@Override
	public void onDestroy() {
		Log.d(tag, "Destroying View ...");
		super.onDestroy();
	}
	
	public void triggerSearch(View v) {
		Intent intent = new Intent(this, SearchActivity.class);
		startActivity(intent);
	}

	// Inner Class
	@SuppressLint("NewApi")
	public class GridCellAdapter extends BaseAdapter implements OnClickListener {
		private static final String tag = "GridCellAdapter";
		private final Context _context;

		private final List<String> list;
		private static final int DAY_OFFSET = 1;
		private final String[] weekdays = new String[] { "Sun", "Mon", "Tue",
				"Wed", "Thu", "Fri", "Sat" };
		private final String[] months = { "January", "February", "March",
				"April", "May", "June", "July", "August", "September",
				"October", "November", "December" };
		private final int[] daysOfMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30,
				31, 30, 31 };
		private int daysInMonth;
		private int currentDayOfMonth;
		private int currentWeekDay;
		private Button gridcell;
		private TextView num_events_per_day;
		private final HashMap<String, Integer> eventsPerMonthMap;
		private final SimpleDateFormat dateFormatter = new SimpleDateFormat(
				"dd-MMM-yyyy");

		// Days in Current Month
		public GridCellAdapter(Context context, int textViewResourceId,
				int month, int year) {
			super();
			this._context = context;
			this.list = new ArrayList<String>();
			Log.d(tag, "==> Passed in Date FOR Month: " + month + " "
					+ "Year: " + year);
			Calendar calendar = Calendar.getInstance();
			setCurrentDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
			setCurrentWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
			Log.d(tag, "New Calendar:= " + calendar.getTime().toString());
			Log.d(tag, "CurrentDayOfWeek :" + getCurrentWeekDay());
			Log.d(tag, "CurrentDayOfMonth :" + getCurrentDayOfMonth());

			// Print Month
			printMonth(month, year);

			// Find Number of Events
			eventsPerMonthMap = findNumberOfEventsPerMonth(year, month);
		}

		private String getMonthAsString(int i) {
			return months[i];
		}

		private String getWeekDayAsString(int i) {
			return weekdays[i];
		}

		private int getNumberOfDaysOfMonth(int i) {
			return daysOfMonth[i];
		}

		public String getItem(int position) {
			return list.get(position);
		}

		@Override
		public int getCount() {
			return list.size();
		}
		
		private int getMonthAsInt (String month)
		{
			int n = -1;
			
			for (int i = 0 ; i < months.length ; i++) {
				if (months[i].equals(month)) {
					n = i;
					break;
				}
			}
			
			return n;
		}

		/**
		 * Prints Month
		 * 
		 * @param mm
		 * @param yy
		 */
		private void printMonth(int mm, int yy) {
			Log.d(tag, "==> printMonth: mm: " + mm + " " + "yy: " + yy);
			int trailingSpaces = 0;
			int daysInPrevMonth = 0;
			int prevMonth = 0;
			int prevYear = 0;
			int nextMonth = 0;
			int nextYear = 0;

			int currentMonth = mm - 1;
			String currentMonthName = getMonthAsString(currentMonth);
			daysInMonth = getNumberOfDaysOfMonth(currentMonth);

			Log.d(tag, "Current Month: " + " " + currentMonthName + " having "
					+ daysInMonth + " days.");

			GregorianCalendar cal = new GregorianCalendar(yy, currentMonth, 1);
			Log.d(tag, "Gregorian Calendar:= " + cal.getTime().toString());

			if (currentMonth == 11) {
				prevMonth = currentMonth - 1;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
				nextMonth = 0;
				prevYear = yy;
				nextYear = yy + 1;
				Log.d(tag, "*->PrevYear: " + prevYear + " PrevMonth:"
						+ prevMonth + " NextMonth: " + nextMonth
						+ " NextYear: " + nextYear);
			} else if (currentMonth == 0) {
				prevMonth = 11;
				prevYear = yy - 1;
				nextYear = yy;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
				nextMonth = 1;
				Log.d(tag, "**--> PrevYear: " + prevYear + " PrevMonth:"
						+ prevMonth + " NextMonth: " + nextMonth
						+ " NextYear: " + nextYear);
			} else {
				prevMonth = currentMonth - 1;
				nextMonth = currentMonth + 1;
				nextYear = yy;
				prevYear = yy;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
				Log.d(tag, "***---> PrevYear: " + prevYear + " PrevMonth:"
						+ prevMonth + " NextMonth: " + nextMonth
						+ " NextYear: " + nextYear);
			}

			int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
			trailingSpaces = currentWeekDay;

			Log.d(tag, "Week Day:" + currentWeekDay + " is "
					+ getWeekDayAsString(currentWeekDay));
			Log.d(tag, "No. Trailing space to Add: " + trailingSpaces);
			Log.d(tag, "No. of Days in Previous Month: " + daysInPrevMonth);

			if (cal.isLeapYear(cal.get(Calendar.YEAR)))
				if (mm == 2)
					++daysInMonth;
				else if (mm == 3)
					++daysInPrevMonth;

			// Trailing Month days
			for (int i = 0; i < trailingSpaces; i++) {
				Log.d(tag,
						"PREV MONTH:= "
								+ prevMonth
								+ " => "
								+ getMonthAsString(prevMonth)
								+ " "
								+ String.valueOf((daysInPrevMonth
										- trailingSpaces + DAY_OFFSET)
										+ i));
				list.add(String
						.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET)
								+ i)
						+ "-GREY"
						+ "-"
						+ getMonthAsString(prevMonth)
						+ "-"
						+ prevYear);
			}

			// Current Month Days
			for (int i = 1; i <= daysInMonth; i++) {
				Log.d(currentMonthName, String.valueOf(i) + " "
						+ getMonthAsString(currentMonth) + " " + yy);
				if (i == getCurrentDayOfMonth()) {
					list.add(String.valueOf(i) + "-BLUE" + "-"
							+ getMonthAsString(currentMonth) + "-" + yy);
				} else {
					list.add(String.valueOf(i) + "-WHITE" + "-"
							+ getMonthAsString(currentMonth) + "-" + yy);
				}
			}

			// Leading Month days
			for (int i = 0; i < list.size() % 7; i++) {
				Log.d(tag, "NEXT MONTH:= " + getMonthAsString(nextMonth));
				list.add(String.valueOf(i + 1) + "-GREY" + "-"
						+ getMonthAsString(nextMonth) + "-" + nextYear);
			}
		}

		/**
		 * NOTE: YOU NEED TO IMPLEMENT THIS PART Given the YEAR, MONTH, retrieve
		 * ALL entries from a SQLite database for that month. Iterate over the
		 * List of All entries, and get the dateCreated, which is converted into
		 * day.
		 * 
		 * @param year
		 * @param month
		 * @return
		 */
		private HashMap<String, Integer> findNumberOfEventsPerMonth(int year,
				int month) {
			HashMap<String, Integer> map = new HashMap<String, Integer>();

			return map;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = (LayoutInflater) _context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.screen_gridcell, parent, false);
			}

			// Get a reference to the Day gridcell
			gridcell = (Button) row.findViewById(R.id.calendar_day_gridcell);
			gridcell.setOnClickListener(this);

			// ACCOUNT FOR SPACING

			Log.d(tag, "Current Day: " + getCurrentDayOfMonth());
			String[] day_color = list.get(position).split("-");
			String theday = day_color[0];
			String themonth = day_color[2];
			String theyear = day_color[3];
			if ((!eventsPerMonthMap.isEmpty()) && (eventsPerMonthMap != null)) {
				if (eventsPerMonthMap.containsKey(theday)) {
					num_events_per_day = (TextView) row
							.findViewById(R.id.num_events_per_day);
					Integer numEvents = (Integer) eventsPerMonthMap.get(theday);
					num_events_per_day.setText(numEvents.toString());
				}
			}

			// Set the Day GridCell
			gridcell.setText(theday);
			gridcell.setTag(theday + "-" + themonth + "-" + theyear);
			Log.d(tag, "Setting GridCell " + theday + "-" + themonth + "-"
					+ theyear);

			if (day_color[1].equals("GREY")) {
				gridcell.setTextColor(getResources()
						.getColor(R.color.lightgray));
			}
			if (day_color[1].equals("WHITE")) {
				gridcell.setTextColor(getResources().getColor(
						R.color.lightgray02));
			}
			if (day_color[1].equals("BLUE")) {
				gridcell.setTextColor(getResources().getColor(R.color.orrange));
			}
			return row;
		}

		private TableLayout tl;
		private String date_month_year;
		private int directionHourSort = 1;
		private int directionPrioritySort = 0;
		
		private void bubbleSortPriority (TableRow[] vectTR) {
			int j = 0, n = vectTR.length;
			TableRow tmp;
			boolean swapped = true;
			
			while (swapped) {
				swapped = false;
				j++;
				for (int i = 0 ; i < n - j ; i++) {
					TextView pr1 = (TextView) vectTR[i].getChildAt(3);
					String prC1 = pr1.getText().toString();
					
					TextView pr2 = (TextView) vectTR[i + 1].getChildAt(3);
					String prC2 = pr2.getText().toString();
					
					if ((directionPrioritySort == 0 && (((prC1.equals("High") || prC1.equals("Medium")) && prC2.equals("Low")) ||
							(prC1.equals("High") && prC2.equals("Medium")))) ||
							(directionPrioritySort == 1 && (((prC2.equals("High") || prC2.equals("Medium")) && prC1.equals("Low")) ||
							(prC2.equals("High") && prC1.equals("Medium"))))) {
						tmp = vectTR[i];
						vectTR[i] = vectTR[i + 1];
						vectTR[i + 1] = tmp;
						swapped = true;
					}
				}
			}
		}
		
		private void bubbleSortHour (TableRow[] vectTR) {
			int j = 0, n = vectTR.length;
			TableRow tmp;
			boolean swapped = true;
			
			while (swapped) {
				swapped = false;
				j++;
				for (int i = 0 ; i < n - j ; i++) {
					TextView hr1 = (TextView) vectTR[i].getChildAt(2);
					String hrS1 = hr1.getText().toString();
					String[] hrSplit1 = hrS1.split(":");
					Date dt1 = new Date(0, 0, 0, Integer.valueOf(hrSplit1[0]), Integer.valueOf(hrSplit1[1]));
					
					TextView hr2 = (TextView) vectTR[i + 1].getChildAt(2);
					String hrS2 = hr2.getText().toString();
					String[] hrSplit2 = hrS2.split(":");
					Date dt2 = new Date(0, 0, 0, Integer.valueOf(hrSplit2[0]), Integer.valueOf(hrSplit2[1]));
					
					if (((directionHourSort == 0) && (dt1.compareTo(dt2) > 0)) ||
							((directionHourSort == 1) && (dt1.compareTo(dt2) < 0))) {
						tmp = vectTR[i];
						vectTR[i] = vectTR[i + 1];
						vectTR[i + 1] = tmp;
						swapped = true;
					}
				}
			}
		}
		
		private OnClickListener sortOnClickListener = new OnClickListener() {
	        public void onClick(View v) {
	        	TextView tv = (TextView)v;
	        	String comparator = tv.getText().toString();

	        	int nr = tl.getChildCount();
        		TableRow[] vectTR = new TableRow[nr - 1];
        		for (int k = 1 ; k < nr ; k++) {
        			vectTR[k - 1] = (TableRow)tl.getChildAt(k);
        		}
	        	
	        	if (comparator.equals("End time"))
	        	{
	        		bubbleSortHour(vectTR);
	        		if (directionHourSort == 0)
	        			directionHourSort = 1;
	        		else
	        			directionHourSort = 0;
	        	} else if (comparator.equals("Priority")) {
	        		bubbleSortPriority(vectTR);
	        		if (directionPrioritySort == 0)
	        			directionPrioritySort = 1;
	        		else
	        			directionPrioritySort = 0;
	        	}
	        	
	        	tl.removeViews(1, vectTR.length);
	        	for (int k = 0 ; k < vectTR.length ; k++) {
	        		tl.addView(vectTR[k]);
	        	}
	        }
		};
		
		private OnClickListener rowOnClickListener = new OnClickListener() {
	        public void onClick(View v) {
	        	TableRow tr = (TableRow)v;
	        	
	        	TextView th = (TextView)tr.getChildAt(2);
	        	String hour = th.getText().toString();
	        	
	        	CheckBox cb = (CheckBox) tr.getChildAt(4);
	        	if (cb.isChecked()) {
	        		tl.removeView(tr);
	        		if (tl.getChildCount() > 1) {
	        			tl.refreshDrawableState();
	        		} else {
	        			tl.removeAllViews();
	        			Toast.makeText(getApplicationContext(), "There are no tasks for today!", Toast.LENGTH_LONG).show();
	        		}
	        		
	        		SQLiteDatabase db = dm.getReadableDatabase();
	        		String selection = "date='" +  date_month_year + "' AND hour='" + hour + "'";
	        		db.delete("Tasks", selection, null);
	        	}
	        	else {
	        		SQLiteDatabase db = dm.getReadableDatabase();
	        		String selection = "date='" +  date_month_year + "' AND hour='" + hour + "'";	
	        		TextView subj = (TextView)tr.getChildAt(1);
		        	String subiect = subj.getText().toString();
		        	
		        	TextView imp = (TextView)tr.getChildAt(3);
		        	String priority = imp.getText().toString();
		        	String[] projection = {"description"};
					Cursor c = db.query("Tasks", 
							projection,
							selection,
							null,
							null,
							null,
							null);
					StringTokenizer stk = new StringTokenizer(hour, " :;/\\");
					c.moveToFirst();
					String description = c.getString(c.getColumnIndexOrThrow("description"));
					db.delete("Tasks", selection, null);
					String[] dateSplit = date_month_year.split("-");
		        	openUpdateTaskActivity(Integer.parseInt(dateSplit[2]), 
		        			getMonthAsInt(dateSplit[1]), Integer.parseInt(dateSplit[0]), 
		        			Integer.parseInt(stk.nextToken()),
		        			Integer.parseInt(stk.nextToken()), subiect, 
		        			description, imp.getText().toString());
		        }
	        	
	        }
	    };  
		
	    private OnCheckedChangeListener checkboxOnListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SQLiteDatabase db = dm.getReadableDatabase();
				ContentValues values = new ContentValues();
				
				if (isChecked == true) {
					values.put("checkTask", 1);
				} else {
					values.put("checkTask", 0);
				}
				
				TableRow tr = (TableRow)buttonView.getParent();
				TextView th = (TextView)tr.getChildAt(2);
		        String hour = th.getText().toString();
		        	
				String selection = "date='" +  date_month_year + "' AND hour='" + hour + "'";
					
				int count = db.update(
						"Tasks",
					    values,
					    selection,
					    null);
			}
	    };
	    
		@Override
		public void onClick(View view) {
			date_month_year = (String) view.getTag();
			selectedDayMonthYearButton.setText("Selected: " + date_month_year);
			
			SQLiteDatabase db = dm.getReadableDatabase();
			
			String[] projection = {"checkTask", "subject", "hour", "importance"};
			String selection = "date = '" + date_month_year + "'";
			
			String sortOrder = "hour" + " ASC";
			
			try {
				tl = (TableLayout) findViewById(R.id.displayLinear);
				tl.removeAllViews();
				
				Cursor c = db.query("Tasks", 
						projection,
						selection,
						null,
						null,
						null,
						sortOrder);
				
				if (c.getCount() == 0)
				{
					Toast.makeText(getApplicationContext(), "There are no tasks for today!", Toast.LENGTH_LONG).show();
					return;
				}
				
				TextView iID = new TextView(getApplicationContext());
				iID.setText("Task done");
				iID.setTextColor(Color.rgb(204, 102, 0));
				iID.setGravity(Gravity.CENTER);
				iID.setPadding(5, 0, 5, 0);
				
				TextView sbID = new TextView(getApplicationContext());
				sbID.setText("Task subject");
				sbID.setTextColor(Color.rgb(204, 102, 0));
				sbID.setGravity(Gravity.CENTER);
				sbID.setPadding(5, 0, 5, 0);
				
				TextView hID = new TextView(getApplicationContext());
				hID.setText("End time");
				hID.setTextColor(Color.rgb(204, 102, 0));
				hID.setGravity(Gravity.CENTER);
				hID.setPadding(5, 0, 5, 0);
				hID.setClickable(true);
				hID.setOnClickListener(sortOnClickListener);
				
				TextView imID = new TextView(getApplicationContext());
				imID.setText("Priority");
				imID.setTextColor(Color.rgb(204, 102, 0));
				imID.setGravity(Gravity.CENTER);
				imID.setPadding(5, 0, 5, 0);
				imID.setClickable(true);
				imID.setOnClickListener(sortOnClickListener);
				
				TextView cbID = new TextView(getApplicationContext());
				cbID.setText("Delete task");
				cbID.setTextColor(Color.rgb(204, 102, 0));
				cbID.setGravity(Gravity.CENTER);
				cbID.setPadding(5, 0, 5, 0);
				
				TableRow rowH = new TableRow(getApplicationContext());
				rowH.addView(iID);
				rowH.addView(sbID);
				rowH.addView(hID);
				rowH.addView(imID);
				rowH.addView(cbID);
				
				tl.addView(rowH);
				
				int lines = c.getCount();
				c.moveToFirst();
				for (int j = 0 ; j < lines ; j++) {
					int checked = c.getInt(c.getColumnIndexOrThrow("checkTask"));
					String sb = c.getString(c.getColumnIndexOrThrow("subject"));
					String h = c.getString(c.getColumnIndexOrThrow("hour"));
					String im = c.getString(c.getColumnIndexOrThrow("importance"));
									
					int col = 0;
					
					String[] dateSplit = date_month_year.split("-");
					String[] hourSplit = h.split(":");
					Date dt1 = new Date(new Integer(dateSplit[2]).intValue(), 
									getMonthAsInt(dateSplit[1]),
									new Integer(dateSplit[0]).intValue(), 
									new Integer(hourSplit[0]).intValue(), 
									new Integer(hourSplit[1]).intValue());
					
					Calendar calendarInfo = Calendar.getInstance();
					int day = calendarInfo.get(Calendar.DAY_OF_MONTH);
					int month = calendarInfo.get(Calendar.MONTH);
					int year = calendarInfo.get(Calendar.YEAR);
					int hourCal = calendarInfo.get(Calendar.HOUR_OF_DAY);
					int minutes = calendarInfo.get(Calendar.MINUTE);
					
					Date dt2 = new Date(year, month, day, hourCal, minutes);
					
					if (dt1.compareTo(dt2) < 0)
						col = R.color.note_text_disabled;
					else if (im.equals("High"))
						col = Color.RED;
					else if (im.equals("Medium"))
						col = Color.BLACK;
					else
						col = Color.BLUE;
						
					CheckBox taskCheck = new CheckBox(getApplicationContext());
					if (checked == 0)
						taskCheck.setChecked(false);
					else
						taskCheck.setChecked(true);
					taskCheck.setGravity(Gravity.CENTER);
					taskCheck.setPadding(5, 0, 5, 0);
					taskCheck.setOnCheckedChangeListener(checkboxOnListener);
					
					TextView sbV = new TextView(getApplicationContext());
					sbV.setText(sb);
					sbV.setTextColor(col);
					sbV.setGravity(Gravity.CENTER);
					sbV.setPadding(5, 0, 5, 0);
					
					TextView hV = new TextView(getApplicationContext());
					hV.setText(h);
					hV.setTextColor(col);
					hV.setGravity(Gravity.CENTER);
					hV.setPadding(5, 0, 5, 0);
					
					TextView imV = new TextView(getApplicationContext());
					imV.setText(im);
					imV.setTextColor(col);
					imV.setGravity(Gravity.CENTER);
					imV.setPadding(5, 0, 5, 0);
					
					CheckBox cb = new CheckBox(getApplicationContext());
					cb.setChecked(false);
					cb.setGravity(Gravity.CENTER);
					cb.setPadding(5, 0, 5, 0);
					
					TableRow row = new TableRow(getApplicationContext());

					row.addView(taskCheck);
			        row.addView(sbV);
			        row.addView(hV);
			        row.addView(imV);
			        row.addView(cb);
			        
			        row.setClickable(true);
			        row.setOnClickListener(rowOnClickListener);
			        
			        tl.addView(row);
					
					if (j < lines - 1)
						c.moveToNext();
				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "There are no tasks for today!", Toast.LENGTH_LONG).show();
				return;
			}
			
			Log.e("Selected date", date_month_year);
			try {
				Date parsedDate = dateFormatter.parse(date_month_year);
				Log.d(tag, "Parsed Date: " + parsedDate.toString());

			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		public int getCurrentDayOfMonth() {
			return currentDayOfMonth;
		}

		private void setCurrentDayOfMonth(int currentDayOfMonth) {
			this.currentDayOfMonth = currentDayOfMonth;
		}

		public void setCurrentWeekDay(int currentWeekDay) {
			this.currentWeekDay = currentWeekDay;
		}

		public int getCurrentWeekDay() {
			return currentWeekDay;
		}
	}
	public void openNewTaskActivity(View view) {
		Intent intent = new Intent(this, NewTaskActivity.class);
		intent.putExtra(YEAR, year);
		intent.putExtra(MONTH, month);
		StringTokenizer stk = new StringTokenizer(selectedDayMonthYearButton.getText().toString(), "Selected: -;\" ");
		if (stk.hasMoreTokens()) {
			String day = stk.nextToken();
			intent.putExtra(DAY, Integer.parseInt(day));
		}
		startActivity(intent);
	}
	
	public void openUpdateTaskActivity(int year, int month, int day, int hour, int min, String title, String description, String imp) {
		Intent intent = new Intent(this, NewTaskActivity.class);
		intent.putExtra(YEAR, year);
		intent.putExtra(MONTH, month + 1);
		intent.putExtra(DAY, day);
		intent.putExtra(HOUR, hour);
		intent.putExtra(MIN, min);
		intent.putExtra(TITLE, title);
		intent.putExtra(DESCRIPTION, description);
		intent.putExtra(IMP, imp);
		startActivity(intent);
	}
}
