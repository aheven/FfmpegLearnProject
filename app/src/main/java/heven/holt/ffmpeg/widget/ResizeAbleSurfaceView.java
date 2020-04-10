package heven.holt.ffmpeg.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Time:2020/4/10
 * Author:HevenHolt
 * Description:
 */
public class ResizeAbleSurfaceView extends SurfaceView {
    private int mRatioWidth = -1;
    private int mRatioHeight = -1;

    public ResizeAbleSurfaceView(Context context) {
        super(context);
    }

    public ResizeAbleSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizeAbleSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width > height * mRatioWidth / mRatioHeight) {    //注意这里骚操作，替换"小于号"为"大于号"
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

    public void resize(int width, int height) {
        mRatioWidth = width;
        mRatioHeight = height;
        getHolder().setFixedSize(width, height);
        requestLayout();
        invalidate();
    }
}
