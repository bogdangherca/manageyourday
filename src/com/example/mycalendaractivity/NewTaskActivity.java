package com.example.mycalendaractivity;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TimePicker;

public class NewTaskActivity extends Activity {
	TimePicker timePicker;
	DatePicker datePicker;
	EditText titleWidget;
	EditText descriptionWidget;
	RadioButton highImp;
	RadioButton lowImp;
	RadioButton medImp;
	private DatabaseManager dm;
	int year;
	int month;
	int day;
	int hour;
	int min;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_task);
		timePicker = (TimePicker)this.findViewById(R.id.timeickerId);
		timePicker.setIs24HourView(true);
		datePicker = (DatePicker)this.findViewById(R.id.datePickerId);
		titleWidget = (EditText)this.findViewById(R.id.taskSubjectId);
		descriptionWidget = (EditText)this.findViewById(R.id.taskDescriptionId);
		highImp = (RadioButton)findViewById(R.id.highPrioId);
		medImp  = (RadioButton)findViewById(R.id.medPrioId);
		lowImp  = (RadioButton)findViewById(R.id.lowPrioId);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			int year = extras.getInt(MyCalendarActivity.YEAR);
			int day = extras.getInt(MyCalendarActivity.DAY);
			int month = extras.getInt(MyCalendarActivity.MONTH);
			int min = 0;
			int hour = 12;
			if (extras.containsKey(MyCalendarActivity.MIN))
				min = extras.getInt(MyCalendarActivity.MIN);
			if (extras.containsKey(MyCalendarActivity.HOUR))
					hour = extras.getInt(MyCalendarActivity.HOUR);
			if (extras.containsKey(MyCalendarActivity.TITLE))
				titleWidget.setText(extras.getString(MyCalendarActivity.TITLE));
			if (extras.containsKey(MyCalendarActivity.DESCRIPTION))
				descriptionWidget.setText(extras.getString(MyCalendarActivity.DESCRIPTION));
			if (extras.containsKey(extras.getString(MyCalendarActivity.IMP)))
			{
				String imp = extras.getString(MyCalendarActivity.IMP);
				if (imp.equals("High"))
					highImp.setChecked(true);
				else if (imp.equals("Medium"))
					medImp.setChecked(true);
				else
					lowImp.setChecked(true);
			}
			datePicker.updateDate(year, month - 1, day);
			timePicker.setCurrentHour(hour);
			timePicker.setCurrentMinute(min);
		}
		//setTitle("Add new task");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_task, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.actionDone) {
			saveData();
			scheduleAlarm();
			openMainActivity();
		}
		return super.onOptionsItemSelected(item);
	}

	private void scheduleAlarm() {
		// TODO Auto-generated method stub
		Intent myIntent = new Intent(getBaseContext(),
				AlarmBroadCastReceiver.class);

		PendingIntent pendingIntent
		= PendingIntent.getBroadcast(getBaseContext(),
				0, myIntent, 0);

		AlarmManager alarmManager
		= (AlarmManager)getSystemService(ALARM_SERVICE);
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day, hour, min);
		calendar.add(Calendar.SECOND, 10);
		alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
	}		

	private void openMainActivity() {
		Intent intent = new Intent(this, MyCalendarActivity.class);
		startActivity(intent);
	}

	private String getMonthForInt(int num) {
		String month = "wrong";
		DateFormatSymbols dfs = new DateFormatSymbols();
		String[] months = dfs.getMonths();
		if (num >= 0 && num <= 11 ) {
			month = months[num];
		}
		return month;
	}

	private void saveData() {
		hour = timePicker.getCurrentHour();
		min = timePicker.getCurrentMinute();
		day = datePicker.getDayOfMonth();
		year = datePicker.getYear();
		month = datePicker.getMonth();
		String date = day + "-" + getMonthForInt(month)+ "-" + year;
		String time = "";
		if (hour < 10)
			time = time + "0"+hour;
		else 
			time += hour;
		time +=":";
		if (min < 10)
			time = time + "0"+min;
		else
			time = time + min;
		dm = DatabaseCreator.getHelper(getApplicationContext());
		SQLiteDatabase db = dm.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("checkTask", 0);
		values.put("subject", ((EditText)findViewById(R.id.taskSubjectId)).getText().toString());
		values.put("description", ((EditText)findViewById(R.id.taskDescriptionId)).getText().toString());	
		values.put("date", date);
		values.put("hour", time);
		String imp = null;
		if (highImp.isChecked())
			imp = "High";
		if (medImp.isChecked())
			imp = "Medium";
		if (lowImp.isChecked())
			imp = "Low";
		values.put("importance", imp);
		db.insert("Tasks", null, values);
		db.close();
	}
}
