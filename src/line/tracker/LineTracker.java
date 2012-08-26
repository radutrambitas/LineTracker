package line.tracker;

import line.tracker.ArduinoReceiver;
import line.tracker.DrawOnTop;
import line.tracker.Preview;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

public class LineTracker extends Activity implements SensorEventListener {
	static final String DEVICE_ADDRESS = "00:06:66:04:9E:39";
	
    private Preview mPreview;
    private DrawOnTop mDrawOnTop;
    private ArduinoReceiver arduinoReceiver;
    
    // Orientation sensor and data
    SensorManager sensorMgr;
    Sensor orSensor;
    static Float azimuth=null;
    static Float pitch=null;
    static Float roll=null;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        arduinoReceiver = new ArduinoReceiver();
        // in order to receive broadcasted intents we need to register our receiver
		registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));
		
		// connect to Arduino
        Amarino.connect(this, DEVICE_ADDRESS);
        
        // Hide the window title.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Create our Preview view and set it as the content of our activity.
        // Create our DrawOnTop view.
        mDrawOnTop = new DrawOnTop(this);
        mPreview = new Preview(this, mDrawOnTop);
		
        setContentView(mPreview);
        addContentView(mDrawOnTop, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
        // register orientation sensor events:
        sensorMgr = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        orSensor = null;
		for (Sensor sensor : sensorMgr.getSensorList(Sensor.TYPE_ORIENTATION)) {
            if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
                orSensor = sensor;
            }
        }
 
        sensorMgr.registerListener(this, orSensor, SensorManager.SENSOR_DELAY_FASTEST);        
    }
    
	@Override
	protected void onStop() {
		super.onStop();
		
		// stop Amarino's background service, we don't need it any more 
		Amarino.disconnect(this, DEVICE_ADDRESS);
		
		// unregister with the orientation sensor
        sensorMgr.unregisterListener(this);
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// not interested in this event	
	}

	public void onSensorChanged(SensorEvent event) {
		azimuth=event.values[0];
		pitch=event.values[1];
		roll=event.values[2];
	}
}