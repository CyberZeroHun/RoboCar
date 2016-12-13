package hu.uniobuda.nik.joystick;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * Created by Forisz on 13/12/16.
 */

public class ArrowKeys extends LinearLayout {

	public static final byte DIRECTION_UP = 0;
	public static final byte DIRECTION_DOWN = 1;
	public static final byte DIRECTION_LEFT = 2;
	public static final byte DIRECTION_RIGHT = 3;

	protected ImageButton arrowUp;
	protected ImageButton arrowDown;
	protected ImageButton arrowLeft;
	protected ImageButton arrowRight;

	private OnArrowKeyClickListener listener;

	public void setOnArrowButtonClickListener(OnArrowKeyClickListener listener) {
		this.listener = listener;
	}


	public ArrowKeys(Context context) {
		super(context);
		init(context, null);
	}

	public ArrowKeys(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public ArrowKeys(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	public ArrowKeys(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		addView(inflater.inflate(R.layout.layout_arrow_keys, null));
		listener = null;

		ClickListener clickListener = new ClickListener();

		arrowUp = (ImageButton) findViewById(R.id.arrow_up);
		arrowDown = (ImageButton) findViewById(R.id.arrow_down);
		arrowLeft = (ImageButton) findViewById(R.id.arrow_left);
		arrowRight = (ImageButton) findViewById(R.id.arrow_right);

		arrowUp.setOnClickListener(clickListener);
		arrowDown.setOnClickListener(clickListener);
		arrowLeft.setOnClickListener(clickListener);
		arrowRight.setOnClickListener(clickListener);

	}

	class ClickListener implements ImageButton.OnClickListener {

		@Override
		public void onClick(View v) {
			if (listener == null)
				return;

			byte direction = 0;

			int i = v.getId();

			if (i == R.id.arrow_up) {
				direction = DIRECTION_UP;
			} else if (i == R.id.arrow_down) {
				direction = DIRECTION_DOWN;
			} else if (i == R.id.arrow_left) {
				direction = DIRECTION_LEFT;
			} else if (i == R.id.arrow_right) {
				direction = DIRECTION_RIGHT;
			}

			listener.onClick(direction);
		}
	}
}


