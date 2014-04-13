package com.nextwave;

import java.util.Map;

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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class SearchDbActivity extends Activity {

	String productName;
	long barcode;
	long cookingTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_db);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_db, menu);
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
			rootView = inflater.inflate(R.layout.fragment_search_db,
					container, false);
			
			SearchDbActivity parent = (SearchDbActivity) mainActivity;
			
			Button searchButton = (Button) rootView.findViewById(R.id.button_search_db);
			searchButton.setOnClickListener(submitSearchButton);
			
			return rootView;
		}
		
		boolean itemFound;
		View.OnClickListener submitSearchButton = new View.OnClickListener() {
			public void onClick(View v) {
				Firebase kitKat = new Firebase("https://nextwave.firebaseio.com/foods");
			    kitKat.addListenerForSingleValueEvent(new ValueEventListener() {
			    	@Override
			    	public void onDataChange(DataSnapshot snapshot) {
			    		String productNameInput = ((EditText) rootView.findViewById(R.id.search_field)).getText().toString();
			    		
			    		itemFound = false;
			            for (DataSnapshot child : snapshot.getChildren()) {
			            	Object value = child.getValue();
			            	String productName = ((Map)value).get("name").toString();
			            	long barcode = (long)((Map)value).get("barcode");
			            	long cookingTime = (long)((Map)value).get("time");
			            	if (productName.toLowerCase().contains(productNameInput.toLowerCase()))
			            	{
			            		itemFound = true;
			            		Intent startFromDbIntent = new Intent(mainActivity, StartFromDbActivity.class);
	                    		Bundle extras = new Bundle();
	                    		extras.putString("NW_PRODUCT_NAME", productName);
	                    		extras.putLong("NW_BARCODE", barcode);
	                    		extras.putLong("NW_COOKING_TIME", cookingTime);
	                    		startFromDbIntent.putExtras(extras);
	                    		startActivity(startFromDbIntent);
	                    		break;
			            	}
			            }
			            if (itemFound == false)
			            {
			            	Intent inputToDbIntent = new Intent(mainActivity, InputToDbActivity.class);
			            	Bundle extras = new Bundle();
			            	extras.putString("NW_PRODUCT_NAME", productNameInput);
			            	extras.putLong("NW_BARCODE", -1);
			            	extras.putLong("NW_COOKING_TIME", 0);
			            	inputToDbIntent.putExtras(extras);
			            	startActivity(inputToDbIntent);
			            }
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
		};
	}

}
