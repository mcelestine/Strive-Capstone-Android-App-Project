package com.capstone.striveapp2;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.TextView;

/**
 * This class is used to handle anything that needs to be done within the
 * app when a chronometer updates it's value.
 */
public class OnTick implements Chronometer.OnChronometerTickListener {


	TextView clockFace;			// The textview where the current time will be shown


	Date time;					//The time since this clock was started
	SimpleDateFormat timeFormat;//The format that will be applied when converting the time to a string
	String toSeconds="";
	// The amount of time between the time this counter is counting up from
	// and the Unix Epoch Time
	long epochTime;

	/**
	 * @param clockFace
	 * The TextView that will be updated every Tick of the clock.
	 * @param format
	 * The format to print the current time in. Supports the same formats that SimpleDateFormat supports.
	 */
	public OnTick(TextView clockFace,String format) {
		this.clockFace = clockFace;
		time = new Date();
		timeFormat = new SimpleDateFormat(format);
		TimeZone tz = new SimpleTimeZone(0,""); //Make sure the time is centered around 0
		timeFormat.setTimeZone(tz);
	}

	public void onChronometerTick(Chronometer chronometer) {
		long systemTime = System.currentTimeMillis();
		long realtime = SystemClock.elapsedRealtime();
		long base = chronometer.getBase();
		epochTime = systemTime - (realtime - base);
		time.setTime(systemTime - epochTime);
		clockFace.setText(this.toString());
	}

	/**
	 * Get the time this clock is counting up from relative to the Unix Epoch Time.
	 */
	public long getBase() {
		return epochTime;
	}

	/**
	 * Get the amount of time (in milliseconds) that has passed since this clock was
	 * started
	 */
	public long getTime() {
		return System.currentTimeMillis() - epochTime;
	}

	/**
	 * Get a string representation of the current time based off of the format
	 * supplied to this class.
	 */
	public String toString() {
		return timeFormat.format(time);
	}
	
	/*
	public String toSeconds() {
		
	}*/

}
