package com.nextwave;

import com.firebase.client.Firebase;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.os.Build;

public class InputToDbActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_input_to_db);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
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

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_input_to_db,
					container, false);
			return rootView;
		}
		
		View.OnClickListener dbSend = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText foodNameInput = (EditText) mainView.findViewById(R.id.food_name);
				EditText barcodeInput = (EditText) mainView.findViewById(R.id.barcode);
				EditText cookingTimeInput = (EditText) mainView.findViewById(R.id.cooking_time);
				
				String foodName = foodNameInput.getText().toString();
				Long barcode = Long.parseLong(barcodeInput.getText().toString());
				Long cookingTime = Long.parseLong(cookingTimeInput.getText().toString());
				
				Firebase kitKat = new Firebase("https://nextwave.firebaseio.com/foods");
				kitKat.child(foodName).child("barcode").setValue(barcode);
				kitKat.child(foodName).child("time").setValue(cookingTime);
			}
		};
		
	}

}
