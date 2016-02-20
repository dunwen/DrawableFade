package com.dundunwen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import edu.cqut.cn.drawablefade.R;

public class MyDrawable2 extends View {
    /**
     * 绘制线条的Paint,即用户手指绘制Path
     */
    private Paint mOutterPaint = new Paint();
    /**
     * 记录用户绘制的Path
     */
    private Path mPath = new Path();
    /**
     * 内存中创建的Canvas
     */
    private Canvas mCanvas;
    /**
     * mCanvas绘制内容在其上
     */
    private Bitmap mBitmap;

    private int mLastX;
    private int mLastY;

    public MyDrawable2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MyDrawable2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyDrawable2(Context context) {
        super(context);
        init();
    }

    private void init() {
        mPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mOutterPaint.setColor(Color.RED);
        mOutterPaint.setAntiAlias(true);
        mOutterPaint.setDither(true);
        mOutterPaint.setStyle(Paint.Style.STROKE);
        mOutterPaint.setStrokeJoin(Paint.Join.ROUND); // 圆角
        mOutterPaint.setStrokeCap(Paint.Cap.ROUND); // 圆角
        mOutterPaint.setStrokeWidth(20);

        mCanvas.drawColor(Color.parseColor("#c0c0c0"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap mback = BitmapFactory.decodeResource(getResources(),
                R.drawable.image);
        canvas.drawBitmap(mback, 0, 0, null);
        drawPath();
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    /**
     * 绘制线条
     */
    private void drawPath() {
        mOutterPaint
                .setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(mPath, mOutterPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mPath.moveTo(mLastX, mLastY);
                break;
            case MotionEvent.ACTION_MOVE:

                mPath.lineTo(x, y);

                mLastX = x;
                mLastY = y;
                break;
        }

        invalidate();
        return true;
    }

}