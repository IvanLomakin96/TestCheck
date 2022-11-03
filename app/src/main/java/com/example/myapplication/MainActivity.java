package com.example.myapplication;

import com.honeywell.aidc.*;
public class MainActivity extends Activity implements BarcodeReader.BarcodeListener,
        BarcodeReader.TriggerListener {
    private AidcManager manager;
    private BarcodeReader reader;
    private boolean triggerState = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create the AidcManager providing a Context and an
        // CreatedCallback implementation.
        AidcManager.create(this, new AidcManager.CreatedCallback() {

            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                // use the manager to create a BarcodeReader with a session
                // associated with the internal imager.
                reader = manager.createBarcodeReader();

                try {
                    // apply settings
                    reader.setProperty(BarcodeReader.PROPERTY_CODE_39_ENABLED, false);
                    reader.setProperty(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);

                    // set the trigger mode to client control
                    reader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                            BarcodeReader.TRIGGER_CONTROL_MODE_CLIENT_CONTROL);
                } catch (UnsupportedPropertyException e) {
                    Toast.makeText(MainActivity.this, "Failed to apply properties",
                            Toast.LENGTH_SHORT).show();
                }

                // register bar code event listener
                reader.addBarcodeListener(MainActivity.this);

                // register trigger state change listener
                reader.addTriggerListener(MainActivity.this);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (reader != null) {
            try {
                reader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (reader != null) {
            // release the scanner claim so we don't get any scanner
            // notifications while paused.
            reader.release();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (reader != null) {
            // unregister barcode event listener
            reader.removeBarcodeListener(this);

            // unregister trigger state change listener
            reader.removeTriggerListener(this);

            // close BarcodeReader to clean up resources.
            // once closed, the object can no longer be used.
            reader.close();
        }
        if (manager != null) {
            // close AidcManager to disconnect from the scanner service.
            // once closed, the object can no longer be used.
            manager.close();
        }
    }

    @Override
    public void onBarcodeEvent(final BarcodeReadEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String barcodeData = event.getBarcodeData();
                String timestamp = event.getTimestamp();

                // update UI to reflect the data
            }
        });

        // reset the trigger state
        triggerState = false;
    }

    @Override
    public void onFailureEvent(final BarcodeFailureEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Barcode read failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // these events can be used to implement custom trigger modes if the automatic
    // behavior provided by the scanner service is insufficient for your application.
    // the following code demonstrates a "toggle" mode implementation, where the state
    // of the scanner changes each time the scan trigger is pressed.
    @Override
    public void onTriggerEvent(TriggerStateChangeEvent event) {
        try {
            // only handle trigger presses
            if (event.getState()) {
                // turn on/off aimer, illumination and decoding
                reader.aim(!triggerState);
                reader.light(!triggerState);
                reader.decode(!triggerState);

                triggerState = !triggerState;
            }
        } catch (ScannerNotClaimedException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Scanner is not claimed",
                    Toast.LENGTH_SHORT).show();
        } catch (ScannerUnavailableException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Scanner unavailable",
                    Toast.LENGTH_SHORT).show();
        }
    }
}