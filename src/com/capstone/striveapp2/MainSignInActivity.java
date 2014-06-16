package com.capstone.striveapp2;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnAccessRevokedListener;

public class MainSignInActivity extends Activity implements
		View.OnClickListener, ConnectionCallbacks, OnConnectionFailedListener,
		OnAccessRevokedListener {

	// Tag of activity name used for debugging.
	private static final String TAG = MainSignInActivity.class.getSimpleName();
	private static final int REQUEST_CODE_RESOLVE_ERR = 9000;

	private PlusClient mPlusClient; // Core Google+ client

	// stored from a failed connect()
	private ConnectionResult mConnectionResult;

	// flag used to trigger the resolution of a failed connection
	private boolean mResolveOnFail;

	private ProgressDialog mConnectionProgressDialog; // display when user is
														// connecting
	String userName = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_sign_in);

		// Initialize plus client
		mPlusClient = new PlusClient.Builder(this, this, this)
				.setVisibleActivities("http://schemas.google.com/AddActivity",
						"http://schemas.google.com/BuyActivity").build();

		mResolveOnFail = false;

		findViewById(R.id.sign_in_button).setOnClickListener(this);
		findViewById(R.id.signOutButton).setOnClickListener(this);
		findViewById(R.id.revokeAccessButton).setOnClickListener(this);
		findViewById(R.id.mainHubButton).setOnClickListener(this);

		// Set visibilities of our buttons
		findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
		findViewById(R.id.signOutButton).setVisibility(View.INVISIBLE);
		findViewById(R.id.revokeAccessButton).setVisibility(View.INVISIBLE);
		findViewById(R.id.mainHubButton).setVisibility(View.INVISIBLE);

		// Configure the ProgressDialog to be displayed if there is a delay
		// in presenting the user with the nest sign in step.
		mConnectionProgressDialog = new ProgressDialog(this);
		mConnectionProgressDialog.setMessage("Signing in...");

		Log.d(TAG, "onCreate called");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_sign_in, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_legalnotice:
			String LicenseInfo = GooglePlayServicesUtil
					.getOpenSourceSoftwareLicenseInfo(getApplicationContext());
			AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(
					MainSignInActivity.this);
			LicenseDialog.setTitle("Legal Notices");
			LicenseDialog.setMessage(LicenseInfo);
			LicenseDialog.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart called");
		// Every time we start we want to try to connect.
		// If it succeeds we'll get an onConnected() callback.
		// If it fails, we'll get onConnectionFailed() with a result.
		mPlusClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop called");

		mPlusClient.disconnect();
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(TAG, "onConnected called");
		// callback method
		// called if connect() was successful
		// Can retrieve the user's account name or make authenticated requests

		mResolveOnFail = false;
		mConnectionProgressDialog.dismiss();

		findViewById(R.id.sign_in_button).setVisibility(View.INVISIBLE);
		findViewById(R.id.signOutButton).setVisibility(View.VISIBLE);
		findViewById(R.id.revokeAccessButton).setVisibility(View.VISIBLE);
		findViewById(R.id.mainHubButton).setVisibility(View.VISIBLE);

		onSignedIn();
	}

	private void onSignedIn() {
		if (mPlusClient.isConnected()) {

			// Retrieve the account name of the user
			// final String name = mPlusClient.getAccountName();

			// Perform authentification
			final Context context = this.getApplicationContext();
			AsyncTask task = new AsyncTask() {
				@Override
				protected Object doInBackground(Object... params) {
					String scope = "oauth2:" + Scopes.PLUS_LOGIN;
					try {
						// We can retrieve the token to check via
						// tokeninfo or to pass to a service-side
						// application.
						String token = GoogleAuthUtil.getToken(context,
								mPlusClient.getAccountName(), scope);
					} catch (UserRecoverableAuthException e) {
						// This error is recoverable, so we could fix this
						// by displaying the intent to the user.
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (GoogleAuthException e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			task.execute((Void) null);
		}

	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.d(TAG, "onConnectionFailed called.");

		if (mConnectionProgressDialog.isShowing()) {
			if (result.hasResolution()) {
				mConnectionResult = result;
				if (mResolveOnFail) {
					startResolution();
				}
			}
		}
	}

	/**
	 * A helper method to flip the mResolveOnFail flag and start the resolution
	 * of the ConnenctionResult from the failed connect() call.
	 */
	private void startResolution() {
		Log.d(TAG, "startResolution called.");
		try {
			// Don't start another resolution now until we have a
			// result from the activity we're about to start.
			mResolveOnFail = false;
			// If we can resolve the error, then call start resolution
			// and pass it an integer tag we can use to track. This means
			// that when we get the onActivityResult callback we'll know
			// its from being started here.
			mConnectionResult.startResolutionForResult(this,
					REQUEST_CODE_RESOLVE_ERR);
		} catch (SendIntentException e) {
			// Any problems, just try to connect() again so we get a new
			// ConnectionResult.
			mConnectionResult = null;
			mPlusClient.connect();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		Log.d(TAG, "ActivityResult: " + requestCode);
		if (requestCode == REQUEST_CODE_RESOLVE_ERR
				&& responseCode == RESULT_OK) {
			// If we have a successful result, we will want to be able to
			// resolve any further errors, so turn on resolution with our flag.
			mResolveOnFail = true;
			// If we have a successful result, call connect() again.
			mPlusClient.connect();

		} else if (requestCode == REQUEST_CODE_RESOLVE_ERR
				&& responseCode != RESULT_OK) {
			mConnectionProgressDialog.dismiss();
			// mConnectionResult = null;
		}
		if (requestCode == 0 && responseCode == RESULT_OK) {
			Log.d(TAG, "sent");
		}
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "onDisconnected called");
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.sign_in_button:
			Log.d(TAG, "Sign in pressed");
			if (!mPlusClient.isConnected()) {
				// Show the dialog as we are now signing in.
				mConnectionProgressDialog.show();
				// Make sure that we will start the resolution
				mResolveOnFail = true;
				// We should always have a connection result ready to resolve,
				// so we can start that process.
				if (mConnectionResult != null) {
					startResolution();
				} else {
					mPlusClient.connect();
				}
			}

			break;
		case R.id.signOutButton:
			Log.d(TAG, "Sign out pressed");
			// We only want to sign out if we're connected.
			if (mPlusClient.isConnected()) {
				// Clear the default account in order to allow the user
				// to potentially choose a different account from the
				// account chooser.
				mPlusClient.clearDefaultAccount();

				// Disconnect from Google Play Services, then reconnect to
				// get a new mPlusClient.
				mPlusClient.disconnect();
				mPlusClient.connect();

				// Hide the sign out buttons, show the sign in button.
				findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
				findViewById(R.id.signOutButton).setVisibility(View.INVISIBLE);
				findViewById(R.id.revokeAccessButton).setVisibility(
						View.INVISIBLE);
				findViewById(R.id.mainHubButton).setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.revokeAccessButton:
			Log.d(TAG, "Revoke access pressed");
			if (mPlusClient.isConnected()) {
				// Clear the default account as in the Sign Out.
				mPlusClient.clearDefaultAccount();

				// Go away and revoke access to this entire application.
				// This will call back to onAccessRevoked when it is
				// complete as it needs to go away to the Google
				// authentication servers to revoke all token.
				mPlusClient.revokeAccessAndDisconnect(this);
			}
			break;
		case R.id.mainHubButton:
			Log.d(TAG, "Main Hub pressed");
			Intent intent = new Intent(this, MainHubActivity.class);
			startActivity(intent);
			break;
		default:
		}
	}

	@Override
	public void onAccessRevoked(ConnectionResult arg0) {
		Log.d(TAG, "onAccessRevoked called.");
		// mPlusClient is now disconnected and access has been revoked.
		// We should now delete any data we need to comply with the
		// developer properties. To reset ourselves to the original state,
		// we should now connect again. We don't have to disconnect as that
		// happens as part of the call.
		mPlusClient.connect();

	}

}
