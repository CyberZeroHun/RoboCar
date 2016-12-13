package hu.uniobuda.nik.robocar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import hu.uniobuda.nik.joystick.ArrowKeys;
import hu.uniobuda.nik.joystick.OnArrowKeyClickListener;

public class MapTestActivity extends AppCompatActivity {
	private static final String TAG = MapTestActivity.class.getSimpleName();

	protected ArrowKeys controlArrowKeys;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_test);


		controlArrowKeys = (ArrowKeys) findViewById(R.id.arrow_keys);

		controlArrowKeys.setOnArrowButtonClickListener(new OnArrowKeyClickListener() {
			@Override
			public void onClick(byte direction) {
				if (direction == ArrowKeys.DIRECTION_UP) {
					// TODO: Implement method stub
					Log.i(TAG, "előre");
				} else if (direction == ArrowKeys.DIRECTION_DOWN) {
					// TODO: Implement method stub
					Log.i(TAG, "hátra");
				} else if (direction == ArrowKeys.DIRECTION_LEFT) {
					// TODO: Implement method stub
					Log.i(TAG, "balra");
				} else if (direction == ArrowKeys.DIRECTION_RIGHT) {
					Log.i(TAG, "jobbra");
					// TODO: Implement method stub
				}
			}
		});

//		MapView mapView;
	}
}
