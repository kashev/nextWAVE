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

import com.firebase.client.Firebase;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.app.Activity;
import android.app.ActionBar;
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
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.NumberPicker.Formatter;
import android.os.Build;

public class InputToDbActivity extends Activity {

	String productName;
	static long barcode;
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
		setContentView(R.layout.activity_input_to_db);

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
		
		HttpClient httpclient = new DefaultHttpClient(); 
		HttpPost httppost = new HttpPost(sparkURL + "opendoor");
		
		try {
			// Add data
			List<NameValuePair> toSpark = new ArrayList<NameValuePair>(1);
			toSpark.add(new BasicNameValuePair("access_token", sparkToken));
			httppost.setEntity(new UrlEncodedFormEntity(toSpark));
			
			// Execute HTTP Post Request
			// This is blocking!
			HttpResponse response = httpclient.execute(httppost);
			
			
		} catch (ClientProtocolException e) {
			// TODO
		} catch (IOException e) {
			// TODO
		} /*catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.input_to_db, menu);
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
		NumberPicker minuteSpinner;
		NumberPicker secondSpinner;
		
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
			rootView = inflater.inflate(R.layout.fragment_input_to_db,
					container, false);
			
			InputToDbActivity parent = (InputToDbActivity) mainActivity;
			final String productName = parent.productName;
			final long barcode = parent.barcode;
			final long cookingTime = parent.cookingTime;
			
			minuteSpinner = (NumberPicker) rootView.findViewById(R.id.cooking_time_minutes);
			minuteSpinner.setMinValue(0);
			minuteSpinner.setMaxValue(99);
			minuteSpinner.setOnLongPressUpdateInterval(100);
			minuteSpinner.setFormatter(TWO_DIGIT_FORMATTER);
			minuteSpinner.setValue((int)cookingTime/60);
			
			secondSpinner = (NumberPicker) rootView.findViewById(R.id.cooking_time_seconds);
			secondSpinner.setMinValue(0);
			secondSpinner.setMaxValue(59);
			secondSpinner.setOnLongPressUpdateInterval(100);
			secondSpinner.setFormatter(TWO_DIGIT_FORMATTER);
			secondSpinner.setValue((int)cookingTime%60);
			
			Button writeAndRunButton = (Button) rootView.findViewById(R.id.button_start_cook);
			writeAndRunButton.setOnClickListener(dbSendAndLaunch);
			
			EditText productNameInput = (EditText) rootView.findViewById(R.id.foodInput);
			productNameInput.setText(productName);
			
			return rootView;
		}
		
		public static final NumberPicker.Formatter TWO_DIGIT_FORMATTER =
	    		new Formatter() {

				@Override
				public String format(int value) {
				// TODO Auto-generated method stub
				return String.format("%02d", value);
			}
		};
		
		View.OnClickListener dbSendAndLaunch = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Firebase kitKat = new Firebase("https://nextwave.firebaseio.com/foods");
				Firebase kitKatChild = kitKat.push();
				
				int cookingTimeSeconds = ((NumberPicker)rootView.findViewById(R.id.cooking_time_seconds)).getValue();
				int cookingTimeMinutes = ((NumberPicker)rootView.findViewById(R.id.cooking_time_minutes)).getValue();
				int cookingTimeTotal = cookingTimeSeconds + cookingTimeMinutes*60;
				
				String inputName = ((EditText)rootView.findViewById(R.id.foodInput)).getText().toString();
				
				kitKatChild.child("name").setValue(inputName);
				kitKatChild.child("barcode").setValue(barcode);
				kitKatChild.child("time").setValue(cookingTimeTotal);
				
				Intent countdownIntent = new Intent(mainActivity, CountdownActivity.class);
        		Bundle extras = new Bundle();
        		extras.putString("NW_PRODUCT_NAME", inputName);
        		extras.putLong("NW_BARCODE", -1);
        		extras.putLong("NW_COOKING_TIME", cookingTimeTotal);
        		countdownIntent.putExtras(extras);
        		startActivity(countdownIntent);
			}
		};
		
	}

}
