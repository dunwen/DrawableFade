package com.dundunwen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;

/**
 * Created by dun on 2016/2/19.
 */
public class MyDrawable extends LayerDrawable implements Drawable.Callback{
    Paint mPaint;
    Context mContext;
    final static String TAG = "MyDrawable";

    /**
     * Creates a new layer drawable with the list of specified layers.
     *
     * @param layers a list of drawables to use as layers in this new drawable,
     *               must be non-null
     */
    public MyDrawable(Drawable[] layers,Context mContext) {
        super(layers);
        this.mContext = mContext;
        initPaint();

    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        invalidateSelf();
    }

    int RectWidth = 50;
    int RectHeight = 25;

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Drawable drawable = getDrawable(getNumberOfLayers() - 1);

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Log.i(TAG, "draw: width = " + width + " height =" + height);

        drawable.draw(canvas);
        canvas.drawRect(width - dip2px(mContext,RectWidth), 0, width, dip2px(mContext,RectHeight), mPaint);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


}
