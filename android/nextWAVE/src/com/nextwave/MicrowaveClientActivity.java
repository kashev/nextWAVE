package com.nextwave;

import java.util.Map;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Build;

public class MicrowaveClientActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microwave_client);

        
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.microwave_client, menu);
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
        
        View mainView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_microwave_client, container, false);
            mainView = rootView;
            
//            Firebase kitKat = new Firebase("https://nextwave.firebaseio.com/foods/KitKat");
//            kitKat.child("barcode").setValue("NULL");
//            kitKat.child("time").setValue(1234);
            
//            Firebase kitKat = new Firebase("https://nextwave.firebaseio.com/foods");
//            kitKat.addListenerForSingleValueEvent(new ValueEventListener() {
//            	@Override
//            	public void onDataChange(DataSnapshot snapshot) {
//                    for (DataSnapshot child : snapshot.getChildren()) {
//                    	Object value = child.getValue();
//                    	long barcode = (long)((Map)value).get("barcode");
//                    	if (barcode == 123456789)
//                    	{
//                    		Log.d("cookingTime", ((Map)value).get("time").toString());
//                    	}
//                    		
//                    }
//            	}
//            	
//            	public void onCancelled() {
//            		System.err.println("Listener was cancelled");
//            	}
//
//				@Override
//				public void onCancelled(FirebaseError arg0) {
//					// TODO Auto-generated method stub
//					
//				}
//            });
//
//            Log.d("kitkat", kitKat.getName());
            
            Button sendButton = (Button) rootView.findViewById(R.id.button_send);
            sendButton.setOnClickListener(dbSend);
           
            return rootView;
        }
        
        View.OnClickListener dbSend = new View.OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			EditText foodNameInput = (EditText) mainView.findViewById(R.id.food_name);
    			EditText barcodeInput = (EditText) mainView.findViewById(R.id.barcode);
    			EditText cookingTimeInput = (EditText) mainView.findViewById(R.id.cooking_time);
    			
    			String foodName = foodNameInput.getText().toString();
    			String barcode = barcodeInput.getText().toString();
    			Long cookingTime = Long.parseLong(cookingTimeInput.getText().toString());
    			
    			Firebase kitKat = new Firebase("https://nextwave.firebaseio.com/foods");
    			kitKat.child(foodName).child("barcode").setValue(barcode);
    			kitKat.child(foodName).child("time").setValue(cookingTime);
    		}
    	};
    }

    
}
