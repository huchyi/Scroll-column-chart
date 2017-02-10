package com.scrollcolumnchart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColumnCharView extends View {
  private int xStart;//圆点x坐标
  private int xEnd;
  private int yStart;
  private int yEnd;//圆点y坐标
  private int xinit;//第一个点x坐标
  private int minXinit;//在移动时，第一个点允许最小的x坐标
  private int maxXinit;//在移动时，第一个点允许允许最大的x坐标
  private int xylinewidth;//xy坐标轴大小
  private int xytextsize;//xy坐标轴文字大小
  private float interval;//坐标间的间隔
  private int bgColor;//背景颜色
  private List<String> dayTime;//x坐标点的值
  private int width;//控件宽度
  private int heigth;//控件高度
  private float startX = 0;//滑动时候，上一次手指的x坐标

  private Paint xPaintFillLine; //x轴的实线 和x中轴线的实线
  private Paint yPaintFillLine; //y轴
  private Paint xPaintStrokeLine;// x顶部的虚线
  private Paint xPaintDefFillLine;// x轴预计值80%的的黄线和红线
  private Path xPaintStrokeLinePath; //虚线的路径
  private Paint yPaintTipsText; //y轴上的标示
  private Paint xPaintPAL;  //x轴上的点和标示
  private Paint mPolylinePaint; //折线图
  private Paint mCirclePaint; //折线上的圆点

  private int mYTextWidth; //y坐标轴文字的的宽度
  private int mYTextHeight;//y坐标轴文字的的高度

  private GestureDetector mGestureDetector; //获取滚动的速度,然后离手后继续滚动一段距离的方法类

  private List<Integer> drugTimeValue;

  private boolean isShowHighLight = false;
  private int drugTimeDay = 0;
  private Handler handler;

  /**
   * 用于完成滚动操作的实例
   */
  private Scroller mScroller;

  public ColumnCharView(Context context, AttributeSet attrs) {
    super(context, attrs);
    xylinewidth = 2;
    xytextsize = 20;
    interval = 100;
    bgColor = Color.TRANSPARENT;

    handler = new Handler();

    dayTime = new ArrayList<>();
    drugTimeValue = new ArrayList<>();
    initData(context);
    mGestureDetector = new GestureDetector(context, new MyOnGestureListener());
    // 第一步，创建Scroller的实例
    mScroller = new Scroller(context);
  }

  public ColumnCharView(Context context) {
    super(context);
  }

  private OnTouchClickListener onTouchClickListener;

  /**
   *
   * */
  public interface OnTouchClickListener {
    void OnClickListener();
  }

  public void setOnTouchEventClickListener(OnTouchClickListener onTouchClickListener) {
    this.onTouchClickListener = onTouchClickListener;
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  @SuppressLint("DrawAllocation") @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    if (changed) {
      width = getWidth();
      heigth = getHeight();

      interval = (float) (width) / 10; //x轴的间隔

      xStart = (int) interval;     // 起始的x轴点
      xEnd = width;

      yStart = 30;
      yEnd = heigth - xytextsize - 2 * xylinewidth - 3;// 结束的y轴点

      minXinit = width - (int) (dayTime.size() * interval); //x轴的最小点
      maxXinit = xStart;   //x轴的第一个点;  //x轴的最大点

      //默认第一个点在最大的x轴,左大右小。如果有pef,则跳到pef第一个点的那页。
      xinit = maxXinit;
      for (int i = 0; i < dayTime.size(); i++) {
        int x = xinit - (int) (i * interval);
        if (drugTimeValue.get(i) > 0 || drugTimeValue.get(i) > 0) {

          if (x < minXinit) {
            xinit = minXinit;
          } else if (x > maxXinit) {
            xinit = maxXinit;
          } else {
            xinit = x;
          }
          break;
        }
      }
      setBackgroundColor(bgColor);
    }
    super.onLayout(changed, left, top, right, bottom);
  }

  //初始化所有的paint和值
  private void initData(Context context) {
    xPaintFillLine = new Paint(); //实线图
    xPaintFillLine.setAntiAlias(true);//抗锯齿
    xPaintFillLine.setDither(true);//抖动，让图形看起来没有毛边
    xPaintFillLine.setStrokeWidth(2);

    yPaintFillLine = new Paint();
    yPaintFillLine.setAntiAlias(true);
    yPaintFillLine.setDither(true);
    yPaintFillLine.setStrokeWidth(dp2px(2));

    xPaintStrokeLine = new Paint(); //虚线图
    xPaintStrokeLine.setStrokeWidth(dp2px(1));
    xPaintStrokeLine.setColor(0x2F8F8F8F);
    xPaintStrokeLine.setStyle(Paint.Style.STROKE);
    PathEffect effects = new DashPathEffect(new float[] { 0, 3, 3, 3, 3 }, 3);
    xPaintStrokeLine.setPathEffect(effects);
    xPaintStrokeLinePath = new Path();

    xPaintDefFillLine = new Paint(); //预计的黄线和红线
    xPaintDefFillLine.setAntiAlias(true);
    xPaintDefFillLine.setDither(true);
    xPaintDefFillLine.setStrokeWidth(dp2px(1));

    yPaintTipsText = new Paint();
    yPaintTipsText.setColor(0xFF5F5F5F);
    yPaintTipsText.setTextSize(dp2px(8));
    yPaintTipsText.setAntiAlias(true);//抗锯齿
    yPaintTipsText.setDither(true);//抖动，让图形看起来没有毛边

    xPaintPAL = new Paint();
    xPaintPAL.setAntiAlias(true);//抗锯齿
    xPaintPAL.setDither(true);//抖动，让图形看起来没有毛边
    xPaintPAL.setTextSize(xytextsize);
    xPaintPAL.setStyle(Paint.Style.FILL);

    mPolylinePaint = new Paint();
    mPolylinePaint.setAntiAlias(true);//抗锯齿
    mPolylinePaint.setDither(true);//抖动，让图形看起来没有毛边
    mPolylinePaint.setTextSize(xytextsize);
    mPolylinePaint.setStyle(Paint.Style.FILL);
    mPolylinePaint.setStrokeWidth(dp2px(1.5f));

    mCirclePaint = new Paint();
    mCirclePaint.setAntiAlias(true);//抗锯齿
    mCirclePaint.setDither(true);//抖动，让图形看起来没有毛边
    mCirclePaint.setColor(Color.WHITE);
    mCirclePaint.setStyle(Paint.Style.FILL);

    String text = "000";
    Rect rect = new Rect();
    yPaintTipsText.getTextBounds(text, 0, text.length(), rect);
    mYTextWidth = rect.width() + 5;//文本的宽度
    mYTextHeight = rect.height();//文本的高度
  }

  @SuppressLint("DrawAllocation") @Override protected void onDraw(Canvas canvas) {

    //最大
    xPaintStrokeLinePath.reset();
    xPaintStrokeLinePath.moveTo(xStart + 5, yStart);//起始坐标
    xPaintStrokeLinePath.lineTo(width, yStart);//终点坐标
    canvas.drawPath(xPaintStrokeLinePath, xPaintStrokeLine);

    //最大的2/3
    xPaintStrokeLinePath.reset();
    xPaintStrokeLinePath.moveTo(xStart + 5, (yEnd + 2 * yStart) / 3);//起始坐标
    xPaintStrokeLinePath.lineTo(width, (yEnd + 2 * yStart) / 3);//终点坐标
    canvas.drawPath(xPaintStrokeLinePath, xPaintStrokeLine);

    //最大的1/3
    xPaintStrokeLinePath.reset();
    xPaintStrokeLinePath.moveTo(xStart + 5, (2 * yEnd + yStart) / 3);//起始坐标
    xPaintStrokeLinePath.lineTo(width, (2 * yEnd + yStart) / 3);//终点坐标
    canvas.drawPath(xPaintStrokeLinePath, xPaintStrokeLine);

    //tips
    String str = "30";
    canvas.drawText(str, maxXinit / 2 - mYTextWidth / 2, yStart + mYTextHeight / 2,
        yPaintTipsText); //最大
    str = "20";
    canvas.drawText(str, maxXinit / 2 - mYTextWidth / 2, (yEnd + 2 * yStart) / 3 + mYTextHeight / 2,
        yPaintTipsText); //  2/3的位置
    str = "10";
    canvas.drawText(str, maxXinit / 2 - mYTextWidth / 2, (2 * yEnd + yStart) / 3 + mYTextHeight / 2,
        yPaintTipsText); //  1/3的位置

    canvas.clipRect(maxXinit, yStart, width, heigth, Region.Op.INTERSECT);

    //x轴
    xPaintFillLine.setColor(0xFF9B9B9B);
    canvas.drawLine(0, yEnd, xEnd, yEnd, xPaintFillLine); //最底部的x轴

    //内容
    int maxSize = dayTime.size();
    for (int i = 0; i < maxSize; i++) {
      float x = i * interval + xinit;

      //画矩形
      float drugTimeCount = (float) drugTimeValue.get(i);

      if (isShowHighLight && i + 1 == drugTimeDay) {
        mPolylinePaint.setColor(0x7Fff0000);

        String text = (int) drugTimeCount + "分钟";
        float yy = (1 - drugTimeCount / 30) * yEnd + drugTimeCount / 30 * yStart - 10;
        if (yy < yStart + mYTextHeight / 2) {
          yy = yStart + mYTextHeight / 2;
        }
        canvas.drawText(text, x + interval / 2 - xPaintPAL.measureText(text) / 2, yy,
            mPolylinePaint);
      } else {
        mPolylinePaint.setColor(0xFF9BC6EC);
      }

      if (drugTimeCount <= 30) {
        canvas.drawRect(x + (interval / 2 - 8),
            (1 - drugTimeCount / 30) * yEnd + drugTimeCount / 30 * yStart, x + (interval / 2 + 8),
            yEnd, mPolylinePaint);
      } else {
        canvas.drawRect(x + (interval / 2 - 8), yStart, x + (interval / 2 + 8), yEnd,
            mPolylinePaint);
      }

      //画x轴的点
      if (i == 0 || i == 4 || i == 9 || i == 14 || i == 19 || i == 24 || i == (maxSize - 1)) {
        xPaintPAL.setColor(0xFF9B9B9B);
        //画x轴的横轴标示
        String text = Integer.valueOf(dayTime.get(i)) + "";

        //canvas.drawCircle(x + interval / 2, yEnd - 1, xylinewidth * 2, xPaintPAL);
        canvas.drawText(text, x + interval / 2 - xPaintPAL.measureText(text) / 2,
            yEnd + xytextsize + xylinewidth * 2, xPaintPAL);
      }
    }
  }

  private float mToX = 0;

  @Override public boolean onTouchEvent(MotionEvent event) {
    mScroller.abortAnimation();

    if (interval * drugTimeValue.size() <= width - xStart) {//如果不用滑动就可以展示所有数据，就不让滑动
      return false;
    }
    mGestureDetector.onTouchEvent(event);

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        startX = event.getX();
        break;

      case MotionEvent.ACTION_MOVE:
        float dis = (event.getX() - startX) * 1.5f;
        startX = event.getX();
        if (xinit + dis > maxXinit) {
          xinit = maxXinit;
        } else if (xinit + dis < minXinit) {
          xinit = minXinit;
        } else {
          xinit = (int) (xinit + dis);
        }
        mToX = event.getX();
        invalidate();

        break;
      case MotionEvent.ACTION_UP:
        break;
      case MotionEvent.ACTION_CANCEL:
        float velocityX = (event.getX() - mToX) * 100;
        if (velocityX > 8000) {
          velocityX = 8000;
        } else if (velocityX < -8000) {
          velocityX = -8000;
        }
        mScroller.fling((int) startX, 0, (int) -velocityX, 0, (int) -velocityX / 10,
            Math.abs((int) velocityX), 0, 0);
        invalidate();

        break;
    }

    return true;
  }

  private class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {

    @Override public boolean onSingleTapUp(MotionEvent e) {
      if (onTouchClickListener != null) {
        onTouchClickListener.OnClickListener(); // 单击事件
      }
      showHighLight(e.getX(), e.getY());
      return super.onSingleTapUp(e);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      // 第二步，调用startScroll()方法来初始化滚动数据并刷新界面

      mScroller.fling((int) startX, 0, (int) -velocityX, 0, (int) -velocityX / 10,
          Math.abs((int) velocityX), 0, 0);
      invalidate();
      return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      return super.onScroll(e1, e2, distanceX, distanceY);
    }
  }

  @Override public void computeScroll() {
    // 第三步，重写computeScroll()方法，并在其内部完成平滑滚动的逻辑
    if (mScroller.computeScrollOffset()) {
      float dis = startX - mScroller.getCurrX();
      startX = mScroller.getCurrX();

      if (xinit + dis > maxXinit) {
        xinit = maxXinit;
      } else if (xinit + dis < minXinit) {
        xinit = minXinit;
      } else {
        xinit = (int) (xinit + dis);
      }
      invalidate();
    }
  }

  private void showHighLight(float x, float y) {

    if (x >= maxXinit) {
      int day;
      int startDay = (int) ((maxXinit - xinit) / interval) + 1;
      float moreX = interval - (maxXinit - xinit) % interval + maxXinit;
      if (x - moreX < 0) {
        day = startDay;
      } else {
        day = startDay + (int) ((x - moreX) / interval) + 1;
      }

      if (day > 0 && day - 1 < drugTimeValue.size()) {

        isShowHighLight = true;
        drugTimeDay = day;
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 1500);
        invalidate();
      }
    }
  }

  private Runnable runnable = new Runnable() {
    @Override public void run() {
      isShowHighLight = false;
      postInvalidate();
    }
  };

  /**
   * dp和像素转换
   */
  private int dp2px(float dipValue) {
    float m = getContext().getResources().getDisplayMetrics().density;
    return (int) (dipValue * m + 0.5f);
  }

  /**
   * 判断大小月
   *
   * @param month 月
   * @param year 年
   * @return 当月的天数
   */
  public static int isBigOrLittleMonth(int month, int year) {
    String[] monthsBig = { "1", "3", "5", "7", "8", "10", "12" };
    String[] monthsLittle = { "4", "6", "9", "11" };
    int days = 0;
    final List<String> listBig = Arrays.asList(monthsBig);
    final List<String> listLittle = Arrays.asList(monthsLittle);
    if (listBig.contains(String.valueOf(month))) {//大月
      days = 31;
    } else if (listLittle.contains(String.valueOf(month))) {//小月
      days = 30;
    } else {//2月
      if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
        days = 29;
      } else {
        days = 28;
      }
    }

    return days;
  }

  /**
   * 设置坐标折线图值
   *
   * @param year 年
   * @param month 月
   */
  public void setValue(int year, int month) {

    int monthDays = isBigOrLittleMonth(month, year);

    //时间对应的value
    List<Integer> drugTimeValue = new ArrayList<>();
    // 设置时间
    List<String> dayTime = new ArrayList<>();
    for (int i = 1; i <= monthDays; i++) {
      dayTime.add(i + "");
      drugTimeValue.add((int) (30 * Math.random()));
    }

    this.drugTimeValue = drugTimeValue;
    this.dayTime = dayTime;

    width = getWidth();
    heigth = getHeight();

    xStart = mYTextWidth;     // 起始的x轴点
    xEnd = width;
    xinit = xStart;   //x轴的第一个点

    yStart = 30;
    yEnd = heigth - xytextsize - 2 * xylinewidth - 3;// 结束的y轴点

    interval = (float) (xEnd - xStart) / monthDays; //x轴的间隔

    invalidate();
  }
}
