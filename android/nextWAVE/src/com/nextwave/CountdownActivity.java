package com.nextwave;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class CountdownActivity extends Activity {
	
	String productName;
	long barcode;
	long cookingTime;
	
	static String sparkURL = "https://api.spark.io/v1/devices/48ff70065067555028111587/";
	static String sparkToken = "4348526a1c0932c678d6e971ce456b9d2ea4a1f5";
	
	private final static UUID PEBBLE_APP_UUID = UUID.fromString("f798b9e5-d4e9-4b8b-b88d-30d2707d5dc7");
	private final static int STATE_KEY = 0;
	private final static int TIME_KEY = 1;
	private final static int PEBBLE_READY = 0;
	private final static int PEBBLE_COOKING = 1;
	private final static int PEBBLE_DONE = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_countdown);
		
		// Get the message from the intent
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		productName = extras.getString("NW_PRODUCT_NAME");
		barcode = extras.getLong("NW_BARCODE");
		cookingTime = extras.getLong("NW_COOKING_TIME");

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.countdown, menu);
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		Activity mainActivity;
		View rootView;
		CountDownTimer timer;
		
		public PlaceholderFragment() {
		}
		
		@Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        mainActivity = activity;
	    }

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			rootView = inflater.inflate(R.layout.fragment_countdown,
					container, false);
			
			CountdownActivity parent = (CountdownActivity)mainActivity;
			final String productName = parent.productName;
			final long barcode = parent.barcode;
			final long cookingTime = parent.cookingTime;
			
			HttpClient httpclient = new DefaultHttpClient(); 
			HttpPost httppost = new HttpPost(sparkURL + "cook");
			
			try {
				// Add data
				List<NameValuePair> toSpark = new ArrayList<NameValuePair>(2);
				toSpark.add(new BasicNameValuePair("access_token", sparkToken));
				toSpark.add(new BasicNameValuePair("time", "10"));
				httppost.setEntity(new UrlEncodedFormEntity(toSpark));
				
				// Execute HTTP Post Request
				// This is blocking!
				HttpResponse response = httpclient.execute(httppost);
				
				// Talk to pebble yo
				if (PebbleKit.isWatchConnected(mainActivity)) {
					// Should already be started, eh. 
					PebbleKit.startAppOnPebble(mainActivity, PEBBLE_APP_UUID);
					
					PebbleDictionary data = new PebbleDictionary();
			        data.addUint8(STATE_KEY, (byte) PEBBLE_COOKING); // 0, 1, 2 for ready, cooking, done
			        data.addUint32(TIME_KEY, (int)cookingTime); // time in seconds

			        PebbleKit.sendDataToPebble(mainActivity, PEBBLE_APP_UUID, data);
			        Log.d("Pebble", "Launched?");
				}
				
				timer = new CountDownTimer(1000*cookingTime, 1000) {
					
					TextView countdownTextView = (TextView) rootView.findViewById(R.id.countdown_timer);
					
				     public void onTick(long millisUntilFinished) {
				    	 countdownTextView.setText("seconds remaining: " + millisUntilFinished / 1000);
				     }

				     public void onFinish() {
				         countdownTextView.setText("done!");
				     }
				  }.start();
				
			} catch (ClientProtocolException e) {
				// TODO
			} catch (IOException e) {
				// TODO
			} /*catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} */
			  
			  Button killButton = (Button) rootView.findViewById(R.id.button_kill);
			  killButton.setOnClickListener(new View.OnClickListener() {
				 public void onClick(View v){
						HttpClient httpclient = new DefaultHttpClient(); 
						HttpPost httppost = new HttpPost(sparkURL + "stopcook");
						
						try {
							// Add data
							List<NameValuePair> toSpark = new ArrayList<NameValuePair>(2);
							toSpark.add(new BasicNameValuePair("access_token", sparkToken));
							httppost.setEntity(new UrlEncodedFormEntity(toSpark));
							
							// Execute HTTP Post Request
							// This is blocking!
							HttpResponse response = httpclient.execute(httppost);
							
							// Talk to pebble yo
							if (PebbleKit.isWatchConnected(mainActivity)) {
								// Should already be started, eh. 
								PebbleKit.startAppOnPebble(mainActivity, PEBBLE_APP_UUID);
								
								PebbleDictionary data = new PebbleDictionary();
						        data.addUint8(STATE_KEY, (byte) PEBBLE_DONE); // 0, 1, 2 for ready, cooking, done
						        data.addUint32(TIME_KEY, (int)0); // time in seconds

						        PebbleKit.sendDataToPebble(mainActivity, PEBBLE_APP_UUID, data);
						        Log.d("Pebble", "Launched?");
							}
							
						} catch (ClientProtocolException e) {
							// TODO
						} catch (IOException e) {
							// TODO
						} /*catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} */
						timer.cancel();
				 }
			  });
			
			return rootView;
		}
	}

}
