package hu.uniobuda.nik.joystick;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;


/**
 * Joystick view implementation.
 * Created by Christoforos Zisis on 07/05/16.
 *
 * @author Christoforos Zisis
 * @version 1.0.1
 *          <p>
 *          TODO: Since android has a nice implementation for points --> Replace {hu.uniobuda.nik.joystick.Point} with {android.graphics.PointF}
 */
public class Joystick extends View {

    //<editor-fold desc="Constants">
    private static final int DEFAULT_OUTER_CIRCLE_WIDTH = 5;
    private static final int DEFAULT_INNER_CIRCLE_RADIUS = 50;
    private static final int DEFAULT_INNER_CIRCLE_COLOR = Color.RED;
    private static final int DEFAULT_OUTER_CIRCLE_COLOR = Color.BLACK;
    private static final int DEFAULT_LINE_COLOR = Color.BLUE;
    private static final int DEFAULT_LINE_WIDTH = 2;
    private static final boolean RESET_POSITION_AFTER_RELEASE = true;
    private static final boolean DRAW_CROSSHAIR = false;
    private static final boolean ANIMATE = true;
    private static final float[] angles = new float[]{
            (float) Math.toRadians(0),
            (float) Math.toRadians(22.5),
            (float) Math.toRadians(45),
            (float) Math.toRadians(67.5),
            (float) Math.toRadians(90),
            (float) Math.toRadians(112.5),
            (float) Math.toRadians(135),
            (float) Math.toRadians(157.5),
            (float) Math.toRadians(180),
            (float) Math.toRadians(202.5),
            (float) Math.toRadians(225),
            (float) Math.toRadians(247.5),
            (float) Math.toRadians(270),
            (float) Math.toRadians(292.5),
            (float) Math.toRadians(315),
            (float) Math.toRadians(337.5)
    };
    private static final Paint.Style[] STYLES = new Paint.Style[]{
            Paint.Style.FILL,
            Paint.Style.STROKE
    };
    //</editor-fold>

    //<editor-fold desc="Fields">
    private final float[] crosshairEndX = new float[angles.length];
    private final float[] crosshairEndY = new float[angles.length];

    private JoystickEventListener listener;
    private Paint outerCirclePaint, innerCirclePaint, linePaint, crosshairPaint;
    private float outerCircleRadius, centerX, centerY, posX, posY;
    private int innerCircleRadius, outerCircleOffset; // Offset is the half of the stroke width of the outer circle. (Prevents drawing the circle outside of the view)
    private Point joy;
    private boolean resetPositionAfterRelease, drawCrosshair, animate;
    private ValueAnimator animator;
    private Paint.Style innerCircleStyle, outerCircleStyle;
    //</editor-fold>

    //<editor-fold desc="Getters & Setters">
    public boolean isResetPositionAfterRelease() {
        return resetPositionAfterRelease;
    }

    public void setResetPositionAfterRelease(boolean resetPositionAfterRelease) {
        this.resetPositionAfterRelease = resetPositionAfterRelease;
    }

    public float getPosY() {
        return posY;
    }

    public float getPosX() {
        return posX;
    }

    public boolean isAnimate() {
        return animate;
    }

    public void setAnimate(boolean animate) {
        this.animate = animate;
    }
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public Joystick(Context context) {
        this(context, null);
    }

    public Joystick(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Joystick(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }
    //</editor-fold>

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        listener = null;
        joy = new Point(0f, 0f);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.Joystick, defStyleAttr, 0);

        innerCircleRadius = attributes.getDimensionPixelSize(R.styleable.Joystick_innerCircleRadius, DEFAULT_INNER_CIRCLE_RADIUS);
        int innerCircleColor = attributes.getColor(R.styleable.Joystick_innerCircleColor, DEFAULT_INNER_CIRCLE_COLOR);
        int outerCircleColor = attributes.getColor(R.styleable.Joystick_outerCircleColor, DEFAULT_OUTER_CIRCLE_COLOR);
        int outerCircleWidth = attributes.getDimensionPixelSize(R.styleable.Joystick_outerCircleWidth, DEFAULT_OUTER_CIRCLE_WIDTH);
        int lineColor = attributes.getColor(R.styleable.Joystick_lineColor, DEFAULT_LINE_COLOR);
        int lineWidth = attributes.getDimensionPixelSize(R.styleable.Joystick_lineWidth, DEFAULT_LINE_WIDTH);
        resetPositionAfterRelease = attributes.getBoolean(R.styleable.Joystick_resetPositionOnRelease, RESET_POSITION_AFTER_RELEASE);
        drawCrosshair = attributes.getBoolean(R.styleable.Joystick_drawCrosshair, DRAW_CROSSHAIR);
        animate = attributes.getBoolean(R.styleable.Joystick_animate, ANIMATE);
        outerCircleStyle = STYLES[attributes.getInt(R.styleable.Joystick_outerCircleStyle, 1)];
        innerCircleStyle = STYLES[attributes.getInt(R.styleable.Joystick_innerCircleStyle, 0)];

        attributes.recycle();

        outerCircleOffset = outerCircleWidth >> 1;

        // Set up Paint instances based on attributes.
        outerCirclePaint = new Paint();
        outerCirclePaint.setAntiAlias(true);
        outerCirclePaint.setStyle(outerCircleStyle);
        outerCirclePaint.setColor(outerCircleColor);
        outerCirclePaint.setStrokeWidth(outerCircleWidth);

        innerCirclePaint = new Paint();
        innerCirclePaint.setAntiAlias(true);
        innerCirclePaint.setStyle(innerCircleStyle);
        innerCirclePaint.setColor(innerCircleColor);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(lineColor);
        linePaint.setColor(lineColor);
        linePaint.setStrokeWidth(lineWidth);

        crosshairPaint = new Paint();
        crosshairPaint.setAntiAlias(true);
        crosshairPaint.setColor(Color.BLACK);
        crosshairPaint.setAlpha(30);
        crosshairPaint.setStrokeWidth(1);
        crosshairPaint.setStyle(Paint.Style.STROKE);

        if (animate) {
            animator = ValueAnimator.ofFloat(1, 0);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float value = (Float) animation.getAnimatedValue();
                    float x = translate(joy.x, centerX);
                    float y = -translate(joy.y, centerY);
                    Joystick.this.setPosition(value * x, value * y);

                }
            });
            animator.setDuration(150);
        } else {
            animator = null;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        centerX = w >> 1;
        centerY = h >> 1;
        // Set the joystick position to the middle
        joy.x = centerX;
        joy.y = centerY;
        // Set the radius of the outer circle
        outerCircleRadius = centerX - innerCircleRadius;
        for (int i = 0; i < angles.length; ++i) {
            this.crosshairEndX[i] = centerX + (float) Math.cos(angles[i]) * outerCircleRadius;
            this.crosshairEndY[i] = centerY - (float) Math.sin(angles[i]) * outerCircleRadius;
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the outer circle
        canvas.drawCircle(centerX, centerY, outerCircleRadius - outerCircleOffset, outerCirclePaint);
        // Draw crosshair if needed
        if (drawCrosshair) {
            for (int i = angles.length - 1; i >= 0; i--) {
                canvas.drawLine(centerX, centerY, crosshairEndX[i], crosshairEndY[i], crosshairPaint);
            }
        }
        // Draw the line that connects the inner and outer circles
        canvas.drawLine(joy.x, joy.y, centerX, centerY, linePaint);
        // Draw the inner circle
        canvas.drawCircle(joy.x, joy.y, innerCircleRadius, innerCirclePaint);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (listener != null) {
                    listener.onJoystickTouched();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                final Point pointer = new Point(event.getX(0), event.getY(0));
                final Point center = new Point(centerX, centerY);
                // Prevent the inner circle from leaving the outer circle's boundaries.
                if (getDistance(pointer, center) > outerCircleRadius) {
                    joy = getCircleLineIntersectionPoint(pointer, center, outerCircleRadius);
                } else {
                    joy.x = pointer.x;
                    joy.y = pointer.y;
                }
                // Let's translate the x and y position into the [-1, 1] range and call the callback with it;
                if (listener != null) {
                    posX = translate(joy.x, centerX);
                    posY = -translate(joy.y, centerY);

                    double rad = Math.atan2(posY, posX);
                    double deg = rad * (180 / Math.PI);

                    if (posX < 0) {
                        if (posY < 0) {
                            deg *= -1;
                            deg = -1 * (270 - deg);
                        } else {
                            deg = 90 - deg;
                        }
                    } else {
                        deg = 90 - deg;
                    }

                    listener.onPositionChange(posX, posY, (float) deg);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                // Move the joystick back to the center if needed.
                if (resetPositionAfterRelease) {
                    resetPositionToCenter();
                }
                // Fire the onJoystickReleasedEvent
                if (listener != null) {
                    listener.onJoystickReleased();
                }

                break;
        }
        return true;
    }

    public void setJoystickEventListener(JoystickEventListener listener) {
        this.listener = listener;
    }

    /**
     * Calculates the distance between two points in the coordinate system.
     *
     * @param a {Point}
     * @param b {Point}
     * @return distance between {Point} a and {Point} b
     */
    private float getDistance(final Point a, final Point b) {
        return (float) Math.sqrt((b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y));
    }

    /**
     * @param point
     * @param center
     * @param radius
     * @return
     */
    private static Point getCircleLineIntersectionPoint(final Point point, final Point center, float radius) {
        float caX = center.x - point.x;
        float caY = center.y - point.y;

        float a = caX * caX + caY * caY;
        float bBy2 = caX * caX + caY * caY;
        float c = caX * caX + caY * caY - radius * radius;

        float pBy2 = bBy2 / a;
        float q = c / a;

        float disc = pBy2 * pBy2 - q;

        float abScalingFactor1 = -pBy2 + (float) Math.sqrt(disc);

        return new Point(point.x - caX * abScalingFactor1, point.y - caY * abScalingFactor1);
    }

    /**
     * Translates the joystick's position into the [-1, 1] range (needs * -1 for the y axis)
     *
     * @param number The joystick's position on a given axis
     * @param offset The center coordinate of that axis
     */
    private float translate(float number, float offset) {
        return offset < number ? Math.abs(offset - number) / outerCircleRadius : -(Math.abs(offset - number) / outerCircleRadius);
    }

    /**
     * Resets the joystick position to the center of the view
     */
    public void resetPositionToCenter() {
        // If the animator is defined, then use that for centering the joystick.
        if (animator != null) {
            animator.start();
        } else {
            joy.x = centerX;
            joy.y = centerY;
            invalidate();
        }
    }

    /**
     * Sets the joystick's position based on an x and y location
     *
     * @param x float between [-1, 1]
     * @param y float between [-1, 1]
     */
    public void setPosition(float x, float y) {
        joy.x = centerX + (x * outerCircleRadius);
        joy.y = centerY - (y * outerCircleRadius);
        invalidate();

        if (listener != null) {
            listener.onPositionChange(x, y, 0f);
        }
    }
}