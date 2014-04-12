package com.nextwave;

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
	
    public MicrowaveClientFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_microwave_client, container, false);
        mainView = rootView;
        
//        Firebase kitKat = new Firebase("https://nextwave.firebaseio.com/foods/KitKat");
//        kitKat.child("barcode").setValue("NULL");
//        kitKat.child("time").setValue(1234);
        
//        Firebase kitKat = new Firebase("https://nextwave.firebaseio.com/foods");
//        kitKat.addListenerForSingleValueEvent(new ValueEventListener() {
//        	@Override
//        	public void onDataChange(DataSnapshot snapshot) {
//                for (DataSnapshot child : snapshot.getChildren()) {
//                	Object value = child.getValue();
//                	long barcode = (long)((Map)value).get("barcode");
//                	if (barcode == 123456789)
//                	{
//                		Log.d("cookingTime", ((Map)value).get("time").toString());
//                	}
//                		
//                }
//        	}
//        	
//        	public void onCancelled() {
//        		System.err.println("Listener was cancelled");
//        	}
//
//			@Override
//			public void onCancelled(FirebaseError arg0) {
//				// TODO Auto-generated method stub
//				
//			}
//        });
//
//        Log.d("kitkat", kitKat.getName());
        
        Button sendButton = (Button) rootView.findViewById(R.id.button_send);
        sendButton.setOnClickListener(dbSend);
        
        Button scanButton = (Button) rootView.findViewById(R.id.button_scan);
        scanButton.setOnClickListener(barcodeScan);
        
       
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
	
	View.OnClickListener barcodeScan = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			IntentIntegrator scanIntegrator = new IntentIntegrator(getActivity());
			scanIntegrator.initiateScan();
		}
	};
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//    	super.onActivityResult(requestCode, resultCode, intent);

		
		IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    	
    	if (scanningResult != null) {
    		Toast toast = Toast.makeText(getActivity().getApplicationContext(), /*scanningResult.getContents()*/ "Fragment", Toast.LENGTH_SHORT);
    		toast.show();
    		Log.d("barcode", "scanned from fragment");
    	}
    	
    }
}