package org.leeinseo108.paintboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class PaintBoard extends View {

    public boolean changed = false;
    Paint mPaint;
    Canvas mCanvas;
    Bitmap mBitmap;

    float lastX;
    float lastY;

    Path mPath = new Path();

    float mCurveEndX;
    float mCurveEndY;

    int mInvalidateExtraBorder = 10;

    static final float TOUCH_TOLERANCE = 8;

    public PaintBoard(Context context) {
        super(context);

        init(context);
    }

    public PaintBoard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(3.0F);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        this.lastX = -1;
        this.lastY = -1;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Bitmap img = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(img);
        canvas.drawColor(Color.WHITE);

        mBitmap = img;
        mCanvas = canvas;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                changed = true;

                Rect rect = touchUp(event, false); // 뗄 경우 touchUp 함수 실행
                if (rect != null) {
                    invalidate();
                }

                mPath.rewind();

                return true;

            case MotionEvent.ACTION_DOWN:
                rect = touchDown(event); // 누를 경우 touchDown 함수 실행
                if (rect != null) {
                    invalidate();
                }

                return true;
            case MotionEvent.ACTION_MOVE:
                rect = touchMove(event); // 움직일 경우 touchMove 함수 실행
                if (rect != null) {
                    invalidate();
                }

                return true;
        }

        return false;
    }

    private Rect touchDown(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        lastX = x;
        lastY = y;

        Rect mInvalidRect = new Rect();
        mPath.moveTo(x, y);

        final int border = mInvalidateExtraBorder; // 일단 10으로 설정.
        mInvalidRect.set((int) x - border, (int) y - border, (int) x + border, (int) y + border);
        mCurveEndX = x;
        mCurveEndY = y;

        mCanvas.drawPath(mPath, mPaint);

        return mInvalidRect;
    }

    private Rect touchMove(MotionEvent event) {
        Rect rect = processMove(event);

        return rect;
    }

    private Rect touchUp(MotionEvent event, boolean cancel) {
        Rect rect = processMove(event);

        return rect;
    }

    private Rect processMove(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        final float dx = Math.abs(x - lastX); // x의 변화값
        final float dy = Math.abs(y - lastY); // y의 변화값

        Rect mInvalidRect = new Rect();
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            final int border = mInvalidateExtraBorder;
            mInvalidRect.set((int) mCurveEndX - border, (int) mCurveEndY - border,
                    (int) mCurveEndX + border, (int) mCurveEndY + border);

            float cX = mCurveEndX = (x + lastX) / 2; // quadratic function으로 그리기 위해서..
            float cY = mCurveEndY = (y + lastY) / 2;

            mPath.quadTo(lastX, lastY, cX, cY);

            mInvalidRect.union((int) lastX - border, (int) lastY - border, (int) lastX + border, (int) lastY + border);
            mInvalidRect.union((int) cX - border, (int) cY - border, (int) cX + border, (int) cY + border);

            lastX = x;
            lastY = y;

            mCanvas.drawPath(mPath, mPaint);
        }

        return mInvalidRect; //invalidate를 통해 View에 갱신.
    }
}
