package com.nextwave;

import java.util.Map;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

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
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.Formatter;

public class FoodDoneActivity extends Activity {

	static String productName;
	static long barcode;
	long cookingTime;
	static int adjustFlag = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_food_done);

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
		getMenuInflater().inflate(R.menu.food_done, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml
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
			rootView = inflater.inflate(R.layout.fragment_food_done,
					container, false);
			
			secondSpinner = (NumberPicker) rootView.findViewById(R.id.numberpicker_adjust_seconds);
			secondSpinner.setMinValue(0);
			secondSpinner.setMaxValue(240);
			secondSpinner.setOnLongPressUpdateInterval(100);
			secondSpinner.setFormatter(TWO_DIGIT_FORMATTER);
			secondSpinner.setValue(0);
			
			Button doneButton = (Button) rootView.findViewById(R.id.button_complete);
			doneButton.setOnClickListener(writeAndExit);
			
			Button plusButton = (Button) rootView.findViewById(R.id.button_plus);
			plusButton.setOnClickListener(plusFlag);
			
			Button minusButton = (Button) rootView.findViewById(R.id.button_minus);
			minusButton.setOnClickListener(minusFlag);
			
			return rootView;
		}
		
		public static final NumberPicker.Formatter TWO_DIGIT_FORMATTER =
	    		new Formatter() {

				@Override
				public String format(int value) {
				// TODO Auto-generated method stub
					return String.format("%d", value*adjustFlag);
			}
		};
		
		View.OnClickListener plusFlag = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				adjustFlag = 1;
				
			}
		};
		
		View.OnClickListener minusFlag = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				adjustFlag = -1;
				
			}
		};
		
		View.OnClickListener writeAndExit = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (productName.compareTo("Custom Item") == 0)
				{
					Intent microwaveClientIntent = new Intent(mainActivity, MicrowaveClientActivity.class);
					startActivity(microwaveClientIntent);
				}
				else
				{
				Firebase kitKat = new Firebase("https://nextwave.firebaseio.com/foods");		
			    kitKat.addListenerForSingleValueEvent(new ValueEventListener() {
			    	@Override
			    	public void onDataChange(DataSnapshot snapshot) {
			    		int spinnerDiff = secondSpinner.getValue();
			            for (DataSnapshot child : snapshot.getChildren()) {
			            	Object value = child.getValue();
			            	String productNameDB = ((Map)value).get("name").toString();
			            	long barcodeDB = (long)((Map)value).get("barcode");
			            	long cookingTimeDB = (long)((Map)value).get("time");
			            	if (barcode < 0)
			            	{
				            	if (productNameDB.toLowerCase().contains(productName.toLowerCase()))
				            	{
				            		child.getRef().child("time").setValue(cookingTimeDB + adjustFlag*spinnerDiff);
		                    		break;
				            	}
			            	} 
			            	else
			            	{
			            		if (barcode == barcodeDB)
			            		{
			            			child.getRef().child("time").setValue(cookingTimeDB + adjustFlag*spinnerDiff);
			            			break;
			            		}
			            	}
			            }
			            Intent microwaveClientIntent = new Intent(mainActivity, MicrowaveClientActivity.class);
						startActivity(microwaveClientIntent);
			    	}
			    	
			    	public void onCancelled() {
			    		System.err.println("Listener was cancelled");
			    	}
					@Override
					public void onCancelled(FirebaseError arg0) {
						// TODO Auto-generated method stub
						
					}
			    });
			}
			}
		};
	}

}
