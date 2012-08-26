package line.tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import at.abraxas.amarino.AmarinoIntent;
import line.tracker.DrawOnTop;

/**
 * ArduinoReceiver is responsible for catching broadcasted Amarino
 * events.
 *
 * This simple example extracts data from the intent and turns tracking on or off.
 */
 public class ArduinoReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String data = null;

		// the type of data which is added to the intent
		final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
		
		// we only expect String data though, but it is better to check if really string was sent
		// later Amarino will support different data types, so far data comes always as string and
		// you have to parse the data to the type you have sent from Arduino, like it is shown below
		if (dataType == AmarinoIntent.STRING_EXTRA){
			data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
			
			if (data != null){
				DrawOnTop.tracking = Integer.parseInt(data);
			}
		}
	}
}