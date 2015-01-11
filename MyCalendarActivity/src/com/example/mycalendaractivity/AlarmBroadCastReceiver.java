package com.example.mycalendaractivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmBroadCastReceiver extends BroadcastReceiver {
	public AlarmBroadCastReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO: This method is called when the BroadcastReceiver is receiving
		// an Intent broadcast.
		Intent scheduledIntent = new Intent(context, MyCalendarActivity.class);
		scheduledIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(scheduledIntent);
	}
}
