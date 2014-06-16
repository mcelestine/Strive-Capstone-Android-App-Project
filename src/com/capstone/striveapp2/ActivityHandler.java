package com.capstone.striveapp2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.os.Environment;
import android.util.Log;

public class ActivityHandler {
	
	public boolean isSDAvailable = false;
	public boolean isSDWritable = false;
	
	public String timeSpentExercisingSec = "";
	public String totalDistanceString="";
	public String Exercise="";
	AsyncHttpClient client;
	
	public ActivityHandler() {
		client = new AsyncHttpClient();
	}
	
	public void checkSd() {
		String state = Environment.getExternalStorageState();
		if(Environment.MEDIA_MOUNTED.equals(state)) {
			// write
			isSDAvailable = true;
			isSDWritable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// read only
			isSDAvailable = true;
			isSDWritable = false;
		} else {
			isSDAvailable = false;
			isSDWritable = false;
		}
		
	}

	public void writeToFile() {
		File path = Environment.getExternalStorageDirectory();
		File dir = new File(path.getAbsolutePath() + "/download");
		dir.mkdirs();
		File file = new File(dir, "UsrStats.txt");
		try {
			FileOutputStream f = new FileOutputStream(file);
			PrintWriter pw = new PrintWriter(f);
			pw.println("Line 1");
			pw.println("Line 2");
			pw.flush();
			pw.close();
			f.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("HELPER", "File write failed: " + e.toString());
		}
	}
	
	public void sendToDatabase() {
		Map<String, String> source = new HashMap<String, String>();
		source.put("GoogleID", "57");
		source.put("DisplayName", "Makina C");
		source.put("Exercise", Exercise);
		source.put("ExerciseTime", timeSpentExercisingSec);
		source.put("ExerciseDistance", totalDistanceString);
		
		RequestParams params = new RequestParams(source);
		client.post("http://54.214.48.20/strive/datacatcher.php", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String result) {
				Log.d("TestHttp", result);
			}
			
			@Override
			public void onFailure(Throwable error) {
				Log.e("Error", "error occurred");
				
			}
		});
	}
}
