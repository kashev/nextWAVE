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
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import com.google.zxing.*;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MicrowaveClientActivity extends ActionBarActivity {

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

    String scannedBarcode;
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	
    	IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    	scannedBarcode = scanningResult.getContents();
    	
    	if (scanningResult != null) {
    		TextView barcodeTextView = (TextView)findViewById(R.id.scan_content);
    		barcodeTextView.setText(scannedBarcode);
          
            Firebase kitKat = new Firebase("https://nextwave.firebaseio.com/foods");
            kitKat.addListenerForSingleValueEvent(new ValueEventListener() {
            	@Override
            	public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                    	Object value = child.getValue();
                    	long barcode = (long)((Map)value).get("barcode");
                    	if (barcode == Long.parseLong(scannedBarcode))
                    	{
                    		Log.d("cookingTime", ((Map)value).get("time").toString());
                    		TextView retrievedCookingTime = (TextView)findViewById(R.id.retrieved_cooking_time);
                    		retrievedCookingTime.setText("Cooking Time from Server: " + ((Map)value).get("time").toString());
                    	}
                    		
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
  
            Log.d("kitkat", kitKat.getName());
    	}
    	
    }
    
}
