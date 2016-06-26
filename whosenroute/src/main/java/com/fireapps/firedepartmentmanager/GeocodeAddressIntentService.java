package com.fireapps.firedepartmentmanager;

/**
 * Created by austinhodak on 6/19/16.
 */
import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeocodeAddressIntentService extends IntentService {

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;

    public static final int USE_ADDRESS_NAME = 1;
    public static final int USE_ADDRESS_LOCATION = 2;

    public static final String PACKAGE_NAME =
            "com.fireapps.firedepartmentmanager";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String RESULT_ADDRESS = PACKAGE_NAME + ".RESULT_ADDRESS";
    public static final String LOCATION_LATITUDE_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_LATITUDE_DATA_EXTRA";
    public static final String LOCATION_LONGITUDE_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_LONGITUDE_DATA_EXTRA";
    public static final String LOCATION_NAME_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_NAME_DATA_EXTRA";
    public static final String FETCH_TYPE_EXTRA = PACKAGE_NAME + ".FETCH_TYPE_EXTRA";

    protected ResultReceiver resultReceiver;
    private static final String TAG = "GEO_ADDY_SERVICE";

    public GeocodeAddressIntentService() {
        super("GeocodeAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(TAG, "onHandleIntent");
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String errorMessage = "";
        List<Address> addresses = null;

        int fetchType = intent.getIntExtra(FETCH_TYPE_EXTRA, 0);
        Log.e(TAG, "fetchType == " + fetchType);

        if(fetchType == USE_ADDRESS_NAME) {
            String name = intent.getStringExtra(LOCATION_NAME_DATA_EXTRA);
            try {
                addresses = geocoder.getFromLocationName(name, 1);
            } catch (IOException e) {
                errorMessage = "Service not available";
                Log.e(TAG, errorMessage, e);
            }
        }
        else if(fetchType == USE_ADDRESS_LOCATION) {
            double latitude = intent.getDoubleExtra(LOCATION_LATITUDE_DATA_EXTRA, 0);
            double longitude = intent.getDoubleExtra(LOCATION_LONGITUDE_DATA_EXTRA, 0);

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException ioException) {
                errorMessage = "Service Not Available";
                Log.e(TAG, errorMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                errorMessage = "Invalid Latitude or Longitude Used";
                Log.e(TAG, errorMessage + ". " +
                        "Latitude = " + latitude + ", Longitude = " +
                        longitude, illegalArgumentException);
            }
        }
        else {
            errorMessage = "Unknown Type";
            Log.e(TAG, errorMessage);
        }

        resultReceiver = intent.getParcelableExtra(RECEIVER);
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "Not Found";
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(FAILURE_RESULT, errorMessage, null);
        } else {
            for(Address address : addresses) {
                String outputAddress = "";
                for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    outputAddress += " --- " + address.getAddressLine(i);
                }
                Log.e(TAG, outputAddress);
            }
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, "Address Found");
            deliverResultToReceiver(SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments), address);
        }
    }

    private void deliverResultToReceiver(int resultCode, String message, Address address) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(RESULT_ADDRESS, address);
        bundle.putString(RESULT_DATA_KEY, message);
        resultReceiver.send(resultCode, bundle);
    }

}
