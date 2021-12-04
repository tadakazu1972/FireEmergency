package tadakazu1972.fireemergency;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

public class CustomImageView extends androidx.appcompat.widget.AppCompatImageView {
    private Matrix matrix = new Matrix();
    private ScaleGestureDetector scaleGestureDetector;
    private final float SCALE_MAX = 3.0f;
    private final float SCALE_MIN = 0.5f;
    private final float PINCH_SENSITIVITY = 5.0f;

    public CustomImageView(Context context) {
        super(context);
        init(context);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setImageResource(R.drawable.northkorea);
        setScaleType(ScaleType.MATRIX);
        scaleGestureDetector = new ScaleGestureDetector(context, simpleOnScaleGestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        setImageMatrix(matrix);
        scaleGestureDetector.onTouchEvent(event);
        return scaleGestureDetector.onTouchEvent(event);
    }

    private ScaleGestureDetector.SimpleOnScaleGestureListener simpleOnScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        float focusX;
        float focusY;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = 1.0f;
            float previousScale = getMatrixValue(Matrix.MSCALE_Y);

            if (detector.getScaleFactor() >= 1.0f) {
                scaleFactor = 1 + (detector.getScaleFactor() - 1) / (previousScale * PINCH_SENSITIVITY);
            } else {
                scaleFactor = 1 - (1 - detector.getScaleFactor()) / (previousScale * PINCH_SENSITIVITY);
            }

            float scale = scaleFactor * previousScale;

            if (scale < SCALE_MIN) {
                return false;
            }

            if (scale > SCALE_MAX) {
                return false;
            }

            matrix.postScale(scaleFactor, scaleFactor, focusX,focusY);

            invalidate();

            return super.onScale(detector);

        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            focusX = detector.getFocusX();
            focusY = detector.getFocusY();
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
        }

    };

    private float getMatrixValue(int index) {
        if (matrix == null) {
            matrix = getImageMatrix();
        }

        float[] values = new float[9];
        matrix.getValues(values);

        float value = values[index];
        return value;
    }

}
