package com.nextwave;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.HttpResponse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import com.google.zxing.integration.android.*;

import com.getpebble.*;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;


public class MicrowaveClientFragment extends Fragment {

	View mainView;
	private TextView formatText, contentText;
	Activity mainActivity = getActivity();
	
	String sparkURL = "https://api.spark.io/v1/devices/48ff70065067555028111587/";
	String sparkToken = "4348526a1c0932c678d6e971ce456b9d2ea4a1f5";
	
	private final static UUID PEBBLE_APP_UUID = UUID.fromString("f798b9e5-d4e9-4b8b-b88d-30d2707d5dc7");
	private final static int STATE_KEY = 0;
	private final static int TIME_KEY = 1;
	private final static int PEBBLE_READY = 0;
	private final static int PEBBLE_COOKING = 1;
	private final static int PEBBLE_DONE = 2;
	
    public MicrowaveClientFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = activity;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
    	View rootView = inflater.inflate(R.layout.fragment_microwave_client, container, false);
        mainView = rootView;
        
        Button scanButton = (Button) rootView.findViewById(R.id.button_scan);
        scanButton.setOnClickListener(barcodeScan);
        
        Button noBarcodeButton = (Button) rootView.findViewById(R.id.button_no_barcode);
        noBarcodeButton.setOnClickListener(noBarcodeLaunch);
        
        Button customButton = (Button) rootView.findViewById(R.id.button_custom_time);
        customButton.setOnClickListener(customTimeLaunch);
       
        return rootView;
    }
    
	
	View.OnClickListener barcodeScan = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			IntentIntegrator scanIntegrator = new IntentIntegrator(getActivity());
			scanIntegrator.initiateScan();
		}
	};
	
	View.OnClickListener noBarcodeLaunch = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent searchDbIntent = new Intent(mainActivity, SearchDbActivity.class);
			startActivity(searchDbIntent);
		}
	};
	
	View.OnClickListener customTimeLaunch = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent startFromDbIntent = new Intent(mainActivity, StartFromDbActivity.class);
        	Bundle extras = new Bundle();
        	extras.putString("NW_PRODUCT_NAME", "Custom Item");
        	extras.putLong("NW_BARCODE", -1);
        	extras.putLong("NW_COOKING_TIME", 0);
        	startFromDbIntent.putExtras(extras);
        	startActivity(startFromDbIntent);
		}
	};
	
	
}