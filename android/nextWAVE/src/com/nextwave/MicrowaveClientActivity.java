package com.nextwave;

import java.util.Map;
import java.util.UUID;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.getpebble.android.kit.PebbleKit;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MicrowaveClientActivity extends ActionBarActivity {

	private final static UUID PEBBLE_APP_UUID = UUID.fromString("f798b9e5-d4e9-4b8b-b88d-30d2707d5dc7");
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microwave_client);
        
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MicrowaveClientFragment())
                    .commit();
        }
        
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        
        PebbleKit.startAppOnPebble(this, PEBBLE_APP_UUID);
    }
    
    @Override
    protected void onPostResume() {
    	super.onPostResume();
    	
    	// Is this good? Forces the pebble app open when the android app is open
    	PebbleKit.startAppOnPebble(this, PEBBLE_APP_UUID);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.microwave_client, menu);
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

    long scannedBarcode;
    boolean itemFound;
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	
    	IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    	
    	itemFound = false;
    	if (scanningResult != null) {
    		
    		scannedBarcode = Long.parseLong(scanningResult.getContents());
            Firebase kitKat = new Firebase("https://nextwave.firebaseio.com/foods");
            kitKat.addListenerForSingleValueEvent(new ValueEventListener() {
            	@Override
            	public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                    	Object value = child.getValue();
                    	String productName = ((Map)value).get("name").toString();
                    	long barcode = (long)((Map)value).get("barcode");
                    	long cookingTime = (long)((Map)value).get("time");
                    	if (barcode == scannedBarcode)
                    	{
//                    		Log.d("cookingTime", ((Map)value).get("time").toString());
//                    		Log.d("Name of Scan", ((Map)value).get("name").toString());
                    		itemFound = true;
                    		
                    		Intent startFromDbIntent = new Intent(getApplicationContext(), StartFromDbActivity.class);
                    		Bundle extras = new Bundle();
                    		extras.putString("NW_PRODUCT_NAME", productName);
                    		extras.putLong("NW_BARCODE", barcode);
                    		extras.putLong("NW_COOKING_TIME", cookingTime);
                    		startFromDbIntent.putExtras(extras);
                    		startActivity(startFromDbIntent);
                    		break;
                    	}
                    		
                    }
                    if (itemFound == false) {
                    	// Item was not found in db, prompt to add new cooking time
                    	Intent inputToDbIntent = new Intent(getApplicationContext(), InputToDbActivity.class);
                    	Bundle extras = new Bundle();
                    	extras.putString("NW_PRODUCT_NAME", null);
                    	extras.putLong("NW_BARCODE", scannedBarcode);
                    	extras.putLong("NW_COOKING_TIME", -1);
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
    	else {
    		Toast toast = Toast.makeText(this, "NULL Barcode, try again?", Toast.LENGTH_LONG);
    		toast.show();
    	}
    	
    }
    
}
