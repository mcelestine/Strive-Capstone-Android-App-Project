package com.capstone.striveapp2;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.striveapp2.JoggingActivity.ErrorDialogFragment;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.plus.PlusShare;

public class BikingActivity extends FragmentActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener,
		View.OnClickListener {

	// used when sending to database
	private static final String EXERCISE_NUM = "1";
	// Location Client used to communicate with the Location Services
	private LocationClient bLocationClient;
	// Holds the current location
	private Location bCurrentLocation;

	// A request to connect to Location Services
	// Holds accuracy and frequency parameters
	private LocationRequest bLocationRequest;

	boolean bUpdatesRequested = false;
	boolean isChronometerRunning = false;

	private GoogleMap bMap;

	Chronometer chron;
	OnTick chronInterface;

	private long timeSpentExercisingMilli = 0;

	private double distanceTraveled = 0;

	// store location points to later calculate distance traveled
	ArrayList<Location> locations = new ArrayList<Location>();

	private String timeSpentExercisingSec = "";
	private String totalDistanceString = "";

	ActivityHandler bhandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jogging);

		// Get Google Play availability status
		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getBaseContext());

		if (status != ConnectionResult.SUCCESS) {
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
					requestCode);
			dialog.show();
		} else {
			setUpMapIfNeeded();
		}

		bLocationRequest = LocationRequest.create();
		// Use high accuracy
		bLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set update interval to 5 seconds
		bLocationRequest
				.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
		// Set the interval ceiling to one minute
		bLocationRequest
				.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

		bUpdatesRequested = false;

		bLocationClient = new LocationClient(this, this, this);

		setUpChronometerAndButtons();

		bhandler = new ActivityHandler();
		bhandler.Exercise = EXERCISE_NUM; // 1 for Biking
	}

	private void setUpChronometerAndButtons() {
		// Initialize Chronometer
		chron = (Chronometer) this.findViewById(R.id.chronometer);

		TextView clockFace = (TextView) this.findViewById(R.id.clockTextView);

		// Object for interfacing with the Chronometer
		chronInterface = new OnTick(clockFace, "HH:mm:ss");

		chron.setOnChronometerTickListener(chronInterface);

		findViewById(R.id.startButton).setOnClickListener(this);
		findViewById(R.id.pauseButton).setOnClickListener(this);
		findViewById(R.id.stopButton).setOnClickListener(this);
		findViewById(R.id.resetButton).setOnClickListener(this);
		findViewById(R.id.shareButton).setOnClickListener(this);
		findViewById(R.id.saveStatsButton).setOnClickListener(this);
	}

	/*
	 * Called when the Activity is restarted, even before it becomes visible.
	 */
	@Override
	public void onStart() {
		super.onStart();
		bLocationClient.connect();
		// send request for location updates
	}

	/*
	 * Called when the Activity is no longer visible at all. Stop updates and
	 * disconnect.
	 */
	@Override
	public void onStop() {
		// If the client is connected
		if (bLocationClient.isConnected()) {
			stopPeriodicUpdates();
		}
		bLocationClient.disconnect();
		chron.setBase(SystemClock.elapsedRealtime());
		super.onStop();
	}

	/*
	 * Called when the Activity is going into the background. Parts of the UI
	 * may be visible, but the Activity is inactive.
	 */
	@Override
	public void onPause() {
		super.onPause();
		if (isChronometerRunning) {
			chron.stop();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/*
	 * Called by Location Services when the request to connect finishes
	 * successfully. At this point you can request the current location or start
	 * periodic updates.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		// Display the connection status
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		setUpMapIfNeeded();
		bCurrentLocation = bLocationClient.getLastLocation();
		// Add initial location to arraylist
		locations.add(bCurrentLocation);
		updateMap(bCurrentLocation);
	}

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			// If no resolution is available, display a dialog to the user
			// with the error.
			showErrorDialog(connectionResult.getErrorCode());
		}

	}

	/*
	 * Called by Location Services if the connection drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();

	}

	private void setUpMapIfNeeded() {
		if (bMap == null) {
			// Getting reference to the SupportMapFragment of
			// activity_fragment_jogging.xml
			// and then get the google map object from the fragment
			bMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			if (bMap != null) {
				bMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				bMap.setMyLocationEnabled(true);
			}
		}
	}

	private void updateMap(Location mCurrentLocation2) {
		// Get initial starting latitude and longitude
		LatLng latLng = new LatLng(mCurrentLocation2.getLatitude(),
				mCurrentLocation2.getLongitude());
		bMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		// Zoom in the Google Map
		bMap.animateCamera(CameraUpdateFactory.zoomTo(20));
	}

	private void startPeriodicUpdates() {
		bLocationClient.requestLocationUpdates(bLocationRequest, this);

	}

	private void stopPeriodicUpdates() {
		bLocationClient.removeLocationUpdates(this);
		// mConnectionState.setText("Stop updates");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.biking, menu);
		return true;
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.startButton:
			Log.d("STRIVE", "Tapped start");
			startPeriodicUpdates();
			chron.start();
			isChronometerRunning = true;
			break;
		case R.id.stopButton:
			stopPeriodicUpdates();
			timeSpentExercisingMilli += chronInterface.getTime();
			// convert to seconds
			timeSpentExercisingSec = convertToSecondsString();
			// update textview
			TextView displayText = (TextView) findViewById(R.id.elapsedTimeDisplayTextView);
			displayText.setText(timeSpentExercisingSec + "seconds");
			
			// pass data to activity handler
			bhandler.timeSpentExercisingSec = timeSpentExercisingSec;
			
			double totDistance = getDistanceTraveled();
			// convert to miles
			double miles = totDistance * 0.00062137119;
			totalDistanceString = String.valueOf(miles);
			TextView displayDistance = (TextView) findViewById(R.id.totalDistanceDisplayTextView);
			displayDistance.setText(String.valueOf(distanceTraveled));
			//pass data to activity handler
			bhandler.totalDistanceString = totalDistanceString;
			chron.stop();
			break;
		case R.id.pauseButton:
			chron.stop();
			timeSpentExercisingMilli += chronInterface.getTime();
			break;
		case R.id.resetButton:
			chron.setBase(SystemClock.elapsedRealtime());
			break;
		case R.id.shareButton:
			Intent shareIntent = new PlusShare.Builder(this)
					.setType("text/plain")
					.setText(
							"Hey! I just completed a jogging run with Strive!!!")
					.setContentUrl(Uri.parse("http://54.214.48.20/strive"))
					.getIntent();
			startActivityForResult(shareIntent, 0);
		case R.id.saveStatsButton:
			bhandler.sendToDatabase();
		default:
		}

	}

	private String convertToSecondsString() {
		float time = timeSpentExercisingMilli / 1000F;
		return String.valueOf(time);
	}

	private double getDistanceTraveled() {
		float[] dist = new float[1];

		for (int i = 0; locations.size() - 2 >= i; i++) {
			double lat1 = locations.get(i).getLatitude();
			double lng1 = locations.get(i).getLongitude();
			double lat2 = locations.get(i + 1).getLatitude();
			double lng2 = locations.get(i + 1).getLongitude();

			Location.distanceBetween(lat1, lng1, lat2, lng2, dist);
			distanceTraveled += dist[0];
		}
		return distanceTraveled;
	}

	// Callback method that receives location updates
	@Override
	public void onLocationChanged(Location location) {

		// Add Location to array
		locations.add(location);
	}

	/**
	 * Show a dialog returned by Google Play services for the connection error
	 * code
	 * 
	 * @param errorCode
	 *            An error code returned from onConnectionFailed
	 */
	private void showErrorDialog(int errorCode) {
		// Get the error dialog from Google Play services
		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
				this, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

		// If Google Play services can provide an error dialog
		if (errorDialog != null) {

			// Create a new DialogFragment in which to show the error dialog
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();

			// Set the dialog in the DialogFragment
			errorFragment.setDialog(errorDialog);

			// Show the error dialog in the DialogFragment
			errorFragment.show(getSupportFragmentManager(),
					LocationUtils.APPTAG);
		}

	}

	/**
	 * Define a DialogFragment to display the error dialog generated in
	 * showErrorDialog.
	 */
	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		/**
		 * Default constructor. Sets the dialog field to null
		 */
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		/**
		 * Set the dialog to display
		 * 
		 * @param dialog
		 *            An error dialog
		 */
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		/*
		 * This method must return a Dialog to the DialogFragment.
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

}
