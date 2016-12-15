package hu.uniobuda.nik.arrowbutton;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.forisz.arrowbutton.R;

/**
 * Created by Forisz on 13/12/16.
 */

public class ArrowButtons extends LinearLayout {

	public static final byte DIRECTION_UP = 0;
	public static final byte DIRECTION_DOWN = 1;
	public static final byte DIRECTION_LEFT = 2;
	public static final byte DIRECTION_RIGHT = 3;

	protected Button arrowUp;
	protected Button arrowDown;
	protected Button arrowLeft;
	protected Button arrowRight;

	private OnArrowButtonClickListener listener;

	public void setOnArrowButtonClickListener(OnArrowButtonClickListener listener) {
		this.listener = listener;
	}


	public ArrowButtons(Context context) {
		super(context);
		init(context, null);
	}

	public ArrowButtons(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public ArrowButtons(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	public ArrowButtons(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		addView(inflater.inflate(R.layout.arrow_button, null));
		listener = null;

		ClickListener clickListener = new ClickListener();

		arrowUp = (Button) findViewById(R.id.arrow_up);
		arrowDown = (Button) findViewById(R.id.arrow_down);
		arrowLeft = (Button) findViewById(R.id.arrow_left);
		arrowRight = (Button) findViewById(R.id.arrow_right);

		arrowUp.setOnClickListener(clickListener);
		arrowDown.setOnClickListener(clickListener);
		arrowLeft.setOnClickListener(clickListener);
		arrowRight.setOnClickListener(clickListener);

	}

	class ClickListener implements Button.OnClickListener {

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
