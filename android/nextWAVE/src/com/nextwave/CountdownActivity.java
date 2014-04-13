package com.nextwave;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.os.Build;

public class CountdownActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_countdown);

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

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_countdown,
					container, false);
			
//	        Button sparkOnButton = (Button) rootView.findViewById(R.id.button_spark_on);
//	        sparkOnButton.setOnClickListener(sparkTurnOn);
//	        
//	        Button sparkOffButton = (Button) rootView.findViewById(R.id.button_spark_off);
//	        sparkOffButton.setOnClickListener(sparkTurnOff);
			
			return rootView;
		}
		
//		View.OnClickListener sparkTurnOn = new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				HttpClient httpclient = new DefaultHttpClient(); 
//				HttpPost httppost = new HttpPost(sparkURL + "cook");
//				
//				try {
//					// Add data
//					List<NameValuePair> toSpark = new ArrayList<NameValuePair>(2);
//					toSpark.add(new BasicNameValuePair("access_token", sparkToken));
//					toSpark.add(new BasicNameValuePair("time", "10"));
//					httppost.setEntity(new UrlEncodedFormEntity(toSpark));
//					
//					// Execute HTTP Post Request
//					// is this blocking?
//					HttpResponse response = httpclient.execute(httppost);
//					
//					// Talk to pebble yo
//					if (PebbleKit.isWatchConnected(mainActivity)) {
//						// Should already be started, eh. 
//						PebbleKit.startAppOnPebble(mainActivity, PEBBLE_APP_UUID);
//						
////						Thread.sleep(10000);
//						PebbleDictionary data = new PebbleDictionary();
//				        data.addUint8(STATE_KEY, (byte) PEBBLE_COOKING); // 0, 1, 2 for ready, cooking, done
//				        data.addUint32(TIME_KEY, (int)10); // time in seconds
//
//				        PebbleKit.sendDataToPebble(mainActivity, PEBBLE_APP_UUID, data);
//				        Log.d("Pebble", "Launched?");
//					}
//					
//				} catch (ClientProtocolException e) {
//					// TODO
//				} catch (IOException e) {
//					// TODO
//				} /*catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} */
//			}
//		};
//		
//		View.OnClickListener sparkTurnOff = new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				HttpClient httpclient = new DefaultHttpClient(); 
//				HttpPost httppost = new HttpPost(sparkURL + "stopcook");
//				
//				try {
//					// Add data
//					List<NameValuePair> toSpark = new ArrayList<NameValuePair>(1);
//					toSpark.add(new BasicNameValuePair("access_token", sparkToken));
////					toSpark.add(new BasicNameValuePair("time", "1"));
//					httppost.setEntity(new UrlEncodedFormEntity(toSpark));
//					
//					// Execute HTTP Post Request
//					HttpResponse response = httpclient.execute(httppost);
//					
//				} catch (ClientProtocolException e) {
//					// TODO
//				} catch (IOException e) {
//					// TODO
//				}
//			}
//		};
	}

}
