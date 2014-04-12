package com.nextwave;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.HttpResponse;

import android.app.Activity;
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


public class MicrowaveClientFragment extends Fragment {

	View mainView;
	private TextView formatText, contentText;
	Activity mainActivity = getActivity();
	
	String sparkURL = "https://api.spark.io/v1/devices/48ff70065067555028111587/";
	String sparkToken = "4348526a1c0932c678d6e971ce456b9d2ea4a1f5";
	
    public MicrowaveClientFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_microwave_client, container, false);
        mainView = rootView;
        
        Button sendButton = (Button) rootView.findViewById(R.id.button_send);
        sendButton.setOnClickListener(dbSend);
        
        Button scanButton = (Button) rootView.findViewById(R.id.button_scan);
        scanButton.setOnClickListener(barcodeScan);
        
        Button sparkOnButton = (Button) rootView.findViewById(R.id.button_spark_on);
        sparkOnButton.setOnClickListener(sparkTurnOn);
        
       
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
	
	View.OnClickListener barcodeScan = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			IntentIntegrator scanIntegrator = new IntentIntegrator(getActivity());
			scanIntegrator.initiateScan();
		}
	};
	
	View.OnClickListener sparkTurnOn = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			HttpClient httpclient = new DefaultHttpClient(); 
			HttpPost httppost = new HttpPost(sparkURL + "cook");
			
			try {
				// Add data
				List<NameValuePair> toSpark = new ArrayList<NameValuePair>(2);
				toSpark.add(new BasicNameValuePair("access_token", sparkToken));
				toSpark.add(new BasicNameValuePair("time", "1"));
				httppost.setEntity(new UrlEncodedFormEntity(toSpark));
				
				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
				
			} catch (ClientProtocolException e) {
				// TODO
			} catch (IOException e) {
				// TODO
			}
		}
	};
}