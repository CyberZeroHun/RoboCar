package hu.uniobuda.nik.robocar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

import hu.uniobuda.nik.joystick.ArrowKeys;
import hu.uniobuda.nik.map.MapData;
import hu.uniobuda.nik.map.MapView;
import hu.uniobuda.nik.map.Node;
import hu.uniobuda.nik.map.PathFinder;

public class MapTestActivity extends AppCompatActivity {
	private static final String TAG = MapTestActivity.class.getSimpleName();

	protected ArrowKeys controlArrowKeys;
	protected MapView mapView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_test);

		MapData testData = new MapData();
		mapView = (MapView) findViewById(R.id.map);

		mapView.setData(testData);


		PathFinder pathFinder = new PathFinder(testData);


		List<Node> path = pathFinder.aStar();

		Log.i(TAG, String.format("Destination node (x: %d, y: %d)", pathFinder.getDestination().getX(), pathFinder.getDestination().getY()));
		for (Node node : path) {
			testData.setData(node.getX(), node.getY(), MapData.PATH);
			Log.i(TAG, String.format("x: %d, y: %d", node.getX(), node.getY()));
		}

		testData.setData(pathFinder.getDestination().getX(), pathFinder.getDestination().getY(), MapData.DESTINATION);
		testData.setData(pathFinder.getStart().getX(), pathFinder.getStart().getY(), MapData.ROBOT);
		mapView.invalidate();
		Log.i(TAG, String.format("Start node (x: %d, y: %d)", pathFinder.getStart().getX(), pathFinder.getStart().getY()));








//		controlArrowKeys = (ArrowKeys) findViewById(R.id.arrow_keys);
/*
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
*/
//		MapView mapView;
	}
}
