package com.dundunwen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

/**
 * Created by dun on 2016/2/19.
 */
public class MyImageView extends View {
    ItemClickListener listener;

    Paint mPaint;
    int currentShowIndex = 0; //当前显示的res 数组下标
    int[] resId; //res 数组，包含了bitmap的下标
    int rectNumOfCol = 5; //分割的列数
    int rectNumOfRow = 5; //分割的行数
    int gap = 2000; //切换图片的间隔
    int duration = 500; //过度动画的时间
    int[] delayTime = new int[rectNumOfCol]; //某一列的随机生成的延迟时间，最长为 duration

    Canvas mCanvas; //用于绘制显示在前面的bitmap的画布
    Bitmap currentShowingBitmap; //前面一张显示的bitmap
    Bitmap nextShowBitmap;//后一张要显示的bitmap
    int Measuredwidth;//测量后的宽度
    int Measuredheight;//测量后的高度
    int nextIndex = -1;//下一张bitmap的下标

    long mStartTimeMills = -1L; //记录动画开始的时间

    int maxDelayTime;//随机生成的最大延迟的时间，用于计算alpha值

    boolean isAnimationDone = false;//标记动画是否完成
    int State = 0;//标记动画的状态、暂时没有操作，但是保留了这个状态
    static final int STATE_PREPARE = 0;
    static final int STATE_STARTING = 1;
    static final int STATE_PAUSE = 2;
    static final int STATE_FINISH = 3;

    boolean isMove = false; //标记是否有滑动事件，如有滑动事件，则点击事件不响应
    boolean isDealMove = false;//标记是否处理过滑动事件，避免多次处理同一个滑动事件
    float downX;//标记触摸事件的落点x
    float downY;//同上

    public MyImageView(Context context) {
        super(context);
        initPaint();
    }

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    /**
     * 应用builder设置参数，避免单个参数设置引入bug
     * */
    public void setConfig(Builder b) {
        setResId(b.getResId());
        setRectNum(b.getRectNumOfCol(), b.getRectNumOfRow());
        setGap(b.getGap());
        setDuration(b.getDuration());
    }

    public ItemClickListener getListener() {
        return listener;
    }

    public void setListener(ItemClickListener listener) {
        this.listener = listener;
    }

    private void setRectNum(int col, int row) {
        this.rectNumOfRow = row;
        this.rectNumOfCol = col;
        delayTime = new int[rectNumOfCol];
    }


    private void setResId(int[] resId) {
        this.resId = resId;
        currentShowIndex = 0;
        invalidate();
    }

    private void initPaint() {
        mPaint = new Paint();

        //设置笔触类型，详情请看博客
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getGap() {
        return gap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    /**
     * 设置下一张要显示的图片，如果超过数组上下限，则自动修正为第一张或最后一张，达到无限循环的目的
     *
     * */
    public void setNextIndex(int nextIndex) {
        if(nextIndex<0){
            nextIndex = resId.length-1;
        }else if(nextIndex>=resId.length){
            nextIndex = 0;
        }

        this.nextIndex = nextIndex;
    }

    /**
     * 绘制一个矩形
     * @param alpha 笔触的alpha值，该值会影响绘制矩形的通透度
     * */
    private void drawRect(int left, int top, int right, int bottom, int alpha, Canvas canvas) {
        mPaint.setAlpha(alpha);
        canvas.drawRect(left, top, right, bottom, mPaint);
    }




    /**
     * 开始一个动画，即是播放下一张图片，因为 showNextBitmap 需要获得MesureWidth/MesureHeight 因此需要把事件post到
     * View的时间队列末尾，让onMeasure先执行
     * */
    public void startAnimation() {
        this.post(new Runnable() {
            @Override
            public void run() {
                showNextBitmap();
            }
        });
    }

    /**
     * 显示下一张bitmap
     * */
    private void showNextBitmap() {
        isAnimationDone = false;
        State = STATE_PREPARE;
        initBitmap();
        initDelayTime();
        mStartTimeMills = SystemClock.uptimeMillis();
        invalidate();
    }


    /**
     * 这里说明，当动画一开始，则就把下一张显示的bitmap设置为了当前显示的bitmap
     *
     * 构造当前显示的bitmap，和下一张要显示的bitmap，以及绘制当前显示的bitmap的Canvas，绘制的矩形在扎个Canvas上进行。
     * 可以优化的地方在这里，因为我对bitmap的操作不熟悉。。因此不会怎么去优化
     * */
    private void initBitmap() {
        currentShowingBitmap = Bitmap.createBitmap(Measuredwidth,Measuredheight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(currentShowingBitmap);
        mCanvas.drawBitmap(zoomImage(BitmapFactory.decodeResource(getResources(),resId[currentShowIndex]),Measuredwidth,Measuredheight),0,0,null);

        nextIndex = nextIndex == -1?(currentShowIndex+1>=resId.length?0:currentShowIndex+1):nextIndex;

        currentShowIndex = nextIndex;
        nextShowBitmap = zoomImage(BitmapFactory.decodeResource(getResources(),resId[nextIndex]),Measuredwidth,Measuredheight);
    }

    /***
     * 图片的缩放方法
     *
     * @param bgimage
     *            ：源图片资源
     * @param newWidth
     *            ：缩放后宽度
     * @param newHeight
     *            ：缩放后高度
     * @return
     */
    public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
                                   double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }




    /**
     * 构造每一列的随机延时时间，为什么要这么做？
     *
     * 我的想法是，每一列的总的动画时间都是一样的，为 （rectNumOfRow-1）*200 为了达到有先有后的效果，就设置了一个每一列
     * 的延时时间，达到我们想要的视觉效果
     * */
    private void initDelayTime() {
        maxDelayTime = -1;
        Random r = new Random();
        for (int i = 0; i < delayTime.length; i++) {
            delayTime[i] = r.nextInt(duration);
            maxDelayTime =  delayTime[i] > maxDelayTime ? delayTime[i] : maxDelayTime;
        }
    }




    /*
    * 这里，我是一次绘制了rectNumOfRow * rectNumOCol个矩形，每个矩形的透明度根据延时时间和开始时间以及每一列的延时时间确定，
    * 其公式为  alpha = （当前时间 - （动画开始时间 + 当前格子的延时时间）） * 255 / （最大延迟时间+200 * 行数）
    * 为什么这么算。。。。。。。我已经忘了TAT （没错 这是我昨天打的代码哈哈哈哈）
    *
    * 这里一共绘制了两张bitmap，分别为font 和 back  首先在控件的canvas上绘制back这张bitmap，然后，用我们在前面创建的
    * mCanvas先在font前绘制矩形（可以改变这个矩形位置的通透度的矩形），然后把绘制好的font这张bitmap绘制在控件上的canvas，
    *
    * 利用格子达到透明度255的个数 ，可以判断动画是否完成，
    * */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long currentTimeMill = SystemClock.uptimeMillis();

        int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int height = getMeasuredHeight() - getPaddingBottom() - getPaddingTop();

        int rectWidth = width / rectNumOfCol;
        int rectHeight = height / rectNumOfRow;

        canvas.drawBitmap(nextShowBitmap,0,0,null);
        int finishCount = 0;
        State = STATE_STARTING;
        for (int row = rectNumOfRow - 1; row >= 0; row--) {
            for (int col = 0; col <= rectNumOfCol - 1; col++) {
                int rectDelayTiem = delayTime[col] + 200 * (rectNumOfRow - 1 - row);
                if (currentTimeMill - (mStartTimeMills + rectDelayTiem) > 0) {
                    double ham = ((double)(currentTimeMill - (mStartTimeMills + rectDelayTiem))) / (maxDelayTime+200*row);
                    int rectAlpha = (int) (ham * 255);
                    rectAlpha = rectAlpha>255?255:rectAlpha;
                    if(rectAlpha >=255){
                        finishCount++;
                    }

                    int left = col * rectWidth;
                    int top = row * rectHeight;
                    int right = left + rectWidth;
                    int bottom = top + rectHeight;

                    drawRect(left, top, right, bottom, rectAlpha,mCanvas);
                }
            }
        }
        canvas.drawBitmap(currentShowingBitmap,0,0,null);

        if(finishCount == rectNumOfCol * rectNumOfRow){
            notifyFinishAnimation();
        }else if(State!=STATE_PAUSE){
            invalidate();
        }
    }


    /**
     * 判断动画完成后执行，把showNextBitmap()延时 间隔个时间执行
     * */
    private final static String TAG = "MyImageView";
    private void notifyFinishAnimation() {
        isAnimationDone = true;
        State = STATE_FINISH;
        nextIndex = currentShowIndex+1>=resId.length?0:currentShowIndex+1;
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
               showNextBitmap();
            }
        },gap);
    }


    /*
    * 测量，在这里获得Measuredwidth/Measuredheight的值
    * */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Measuredwidth = getMeasuredWidth();
        Measuredheight = getMeasuredHeight();
    }


    /**
     * 处理触摸事件，因为没写这部分的时候就已经发现的性能问题，因此就随便写写。。。。。
     * */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                isMove = false;
                isDealMove = false;
                downX = event.getX();
                downY = event.getY();

                break;
            case MotionEvent.ACTION_MOVE:
                if(!isDealMove){
                    checkMove(event);
                }
                isMove = true;
                return false;
            case MotionEvent.ACTION_UP:
                if(listener!=null&&!isMove){
                    listener.onClick(currentShowIndex,resId[currentShowIndex]);
                }
                break;
        }
        return true;
    }



    private void checkMove(MotionEvent event) {
        if(event.getX()-downX>200){
            isDealMove = true;
            setNextIndex(currentShowIndex-1);
            showNextBitmap();
            return;
        }
        if(event.getX() - downX < -200){
            isDealMove = true;
            setNextIndex(currentShowIndex+1);
            showNextBitmap();
            return;
        }
    }

    public static class Builder {
        public Builder() {
        }

        int[] resId;
        int rectNumOfCol = 4;
        int rectNumOfRow = 3;
        int gap = 2000;
        int duration = 1000;

        public Builder setRectNumOfCol(int rectNumOfCol) {
            this.rectNumOfCol = rectNumOfCol;
            return this;
        }

        public int getGap() {
            return gap;
        }

        public Builder setGap(int gap) {
            this.gap = gap;
            return this;
        }

        public int getDuration() {
            return duration;
        }

        public Builder setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public int[] getResId() {
            return resId;
        }

        public Builder setResId(int[] resId) {
            this.resId = resId;
            return this;
        }

        public int getRectNumOfCol() {
            return rectNumOfCol;
        }

        public Builder setRectNumOfColr(int rectNumOfColr) {
            this.rectNumOfCol = rectNumOfColr;
            return this;
        }

        public int getRectNumOfRow() {
            return rectNumOfRow;
        }

        public Builder setRectNumOfRow(int rectNumOfRow) {
            this.rectNumOfRow = rectNumOfRow;
            return this;
        }
    }

    public static interface ItemClickListener{
        void onClick(int position,int resId);
    }
}
