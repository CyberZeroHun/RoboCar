package hu.uniobuda.nik.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by Forisz on 16/11/16.
 */

public class MapView extends View {
	private static final String TAG = MapView.class.getSimpleName();

	private MapData data;
	private int scaleX;
	private int scaleY;

	private Paint destinationPaint;
	private Paint backgroundPaint;
	private Paint obstaclePaint;
	private Paint robotPaint;
	private Paint pathPaint;

	//<editor-fold desc="Constructors">
	public MapView(Context context) {
		super(context);
		init(context, null);
	}

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	public MapView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		destinationPaint = new Paint();
		destinationPaint.setColor(Color.GREEN);

		obstaclePaint = new Paint();
		obstaclePaint.setColor(Color.RED);

		robotPaint = new Paint();
		robotPaint.setColor(Color.BLUE);

		pathPaint = new Paint();
		pathPaint.setColor(Color.BLACK);
		pathPaint.setAntiAlias(true);

		data = new MapData();
	}

	public MapData getData() {
		return data;
	}

	public MapView setData(MapData data) {
		this.data = data;
		return this;
	}
	//</editor-fold>

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		Paint p = new Paint();
		p.setColor(Color.RED);
		p.setStyle(Paint.Style.FILL_AND_STROKE);

		//	canvas.save();

		float scaleX = (float) getWidth() / 60f;
		float scaleY = (float) getHeight() / 60f;
		Log.i(TAG, String.format("#onDraw; scaleX: %d, scaleY: %d", canvas.getWidth(), canvas.getHeight()));
		Log.i(TAG, String.format("#onDraw; scaleX: %f, scaleY: %f", scaleX, scaleY));

		//canvas.drawRect(0 * scaleX, 0 * scaleY, 10 * scaleX, 10 * scaleY, p);
		for (int i = 0; i < data.getData().length; i++) {
			for (int j = 0; j < data.getData()[i].length; j++) {
				byte point = data.getData(i, j);

				switch (point) {
					case MapData.BACKGROUND:
						// We don't have to draw shit here. Just leave this here. just in case.
						p.setColor(Color.LTGRAY);
						canvas.drawRect(i * 10 * scaleX, j * 10 * scaleY, (i * 10 + 10) * scaleX, (j * 10 + 10) * scaleY, p);
						break;
					case MapData.DESTINATION:
						p.setColor(Color.GREEN);
						canvas.drawRect(i * 10 * scaleX, j * 10 * scaleY, (i * 10 + 10) * scaleX, (j * 10 + 10) * scaleY, p);
						break;
					case MapData.OBSTACLE:
						p.setColor(Color.RED);
						canvas.drawRect(i * 10 * scaleX, j * 10 * scaleY, (i * 10 + 10) * scaleX, (j * 10 + 10) * scaleY, p);
						break;
					case MapData.ROBOT:
						p.setColor(Color.BLACK);
						canvas.drawRect(i * 10 * scaleX, j * 10 * scaleY, (i * 10 + 10) * scaleX, (j * 10 + 10) * scaleY, robotPaint);
						break;
					case MapData.PATH:
						p.setColor(Color.MAGENTA);
						canvas.drawRect(i * 10 * scaleX, j * 10 * scaleY, (i * 10 + 10) * scaleX, (j * 10 + 10) * scaleY, p);
						break;
					default:
						Log.i(TAG, "Type unrecognised");
						// Point type unrecognised. draw nothing;
						break;
				}


			}
		}

		//	canvas.restore();
		/*

		byte[][] mapData = data.getData();





			}
		}
		// restore the canvas after drawing, to apply scaling properly
		canvas.restore();
*/
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		invalidate();
	}
}
