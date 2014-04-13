package com.nextwave;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.NumberPicker.Formatter;
import android.widget.TextView;

public class StartFromDbActivity extends Activity {

	String productName;
	long barcode;
	long cookingTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_from_db);

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
		getMenuInflater().inflate(R.menu.start_from_db, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_start_from_db,
					container, false);
			
			StartFromDbActivity parent = (StartFromDbActivity)mainActivity;
			final String productName = parent.productName;
			final long barcode = parent.barcode;
			final long cookingTime = parent.cookingTime;
			
			TextView textView =(TextView) rootView.findViewById(R.id.product_name);
			textView.setText(productName);
			
			minuteSpinner = (NumberPicker) rootView.findViewById(R.id.cooking_time_minutes);
			minuteSpinner.setMinValue(0);
			minuteSpinner.setMaxValue(99);
			minuteSpinner.setOnLongPressUpdateInterval(100);
			minuteSpinner.setFormatter(TWO_DIGIT_FORMATTER);
			minuteSpinner.setValue((int)cookingTime/60);
//			minuteSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
//				public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
////					onTimeChanged();
//					spinner.setValue(newVal);
//				}
//			});
			
			secondSpinner = (NumberPicker) rootView.findViewById(R.id.cooking_time_seconds);
			secondSpinner.setMinValue(0);
			secondSpinner.setMaxValue(59);
			secondSpinner.setOnLongPressUpdateInterval(100);
			secondSpinner.setFormatter(TWO_DIGIT_FORMATTER);
			secondSpinner.setValue((int)cookingTime%60);
						
	        Button startCookButton = (Button) rootView.findViewById(R.id.start_cook);
	        startCookButton.setOnClickListener(new View.OnClickListener() {
	        	@Override
	        	public void onClick(View v) {
	        		// Send to countdown
	        		
	        		Intent countdownIntent = new Intent(mainActivity, CountdownActivity.class);
            		Bundle extras = new Bundle();
            		extras.putString("NW_PRODUCT_NAME", productName);
            		extras.putLong("NW_BARCODE", barcode);
            		extras.putLong("NW_COOKING_TIME", minuteSpinner.getValue()*60 + secondSpinner.getValue());
            		countdownIntent.putExtras(extras);
            		startActivity(countdownIntent);
	        	}
	        });
			
					
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
	}

}
