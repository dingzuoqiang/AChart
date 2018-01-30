package eat.arvin.com.mychart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.Html;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.ArrayList;

import eat.arvin.com.mychart.bean.CrossBean;
import eat.arvin.com.mychart.bean.StickData;
import eat.arvin.com.mychart.utils.ColorUtil;
import eat.arvin.com.mychart.utils.DrawUtils;
import eat.arvin.com.mychart.utils.GridUtils;
import eat.arvin.com.mychart.utils.IndexParseUtil;
import eat.arvin.com.mychart.utils.LineUtil;
import eat.arvin.com.mychart.utils.NumberUtil;

/**
 * Created by Arvin on 2016/10/25.
 * K线View
 */
public class KLineView extends ChartView implements ChartConstant {

    //K线所有数据
    private ArrayList<StickData> data;
    //K线展示的数据
    private ArrayList<StickData> showList;

    protected float yUnit;


    public KLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean onViewScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if(data != null && drawCount < data.size() && Math.abs(distanceX) > DEFUALT_WIDTH) {
            int temp = offset + (int)(0 - distanceX / DEFUALT_WIDTH);
            if(temp < 0 || temp + drawCount > data.size()) {

            } else {
                offset = temp;
                postInvalidate();
            }
            return true;
        }
        return false;
    }

    private ScaleGestureDetector mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener(){
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if(data == null) return super.onScale(detector);
            //放大是由1变大，缩小是由1变小
            float scale = detector.getScaleFactor();
            //这个变化太快，把scale变慢一点
            scale = 1 + ((scale - 1) * 0.4f);
            drawCount = (int) (mWidth / DEFUALT_WIDTH);
            if(scale < 1 && drawCount >= data.size()) {
                //缩小时，当缩到一屏显示完时，则不再缩小
            } else if(scale > 1 && drawCount < 50) {
                //放大时，当一屏少于20个时，则不再放大
            } else {
                DEFUALT_WIDTH = DEFUALT_WIDTH * scale;
                invalidate();
            }
            return super.onScale(detector);
        }
    });



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector != null)
            gestureDetector.onTouchEvent(event);
        if(mScaleGestureDetector != null)
            mScaleGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    protected void init() {
        if(data == null) return;
        drawCount = (int) (mWidth / DEFUALT_WIDTH);
        candleXDistance = drawCount * WIDTH_SCALE;
        if(data != null && data.size() > 0) {
            if(drawCount < data.size()) {
                getShowList(offset);
            } else {
                showList = new ArrayList<>();
                showList.addAll(data);
            }
        }
        if(showList == null) return;
        float[] low = new float[showList.size()];
        float[] high = new float[showList.size()];
        int i = 0;
        for(StickData d : showList) {
            low[i] = (float) d.getLow();
            high[i] = (float) d.getHigh();
            i++;
        }
        float[] maxAndMin = LineUtil.getMaxAndMin(low, high);
        yMax = maxAndMin[0];
        yMin = maxAndMin[1];
        yUnit = (float) (yMax - yMin) / mainH;
        xUnit = mWidth / drawCount;
    }
    @Override
    protected void drawGrid(Canvas canvas) {
//        GridUtils.drawGrid(canvas, mWidth, mainH);
        GridUtils.drawIndexGrid(canvas, indexStartY, mWidth, indexH);
    }

    @Override
    protected void drawCandles(Canvas canvas) {
        if(data == null || data.size() == 0) return;
        float x = 0;
        if(showList == null || showList.size() == 0) return;
        //计算出页面能显示多少个
        for(int i = 0; i < showList.size(); i++) {
            if(drawCount < data.size()) {
                x = mWidth - (mWidth / drawCount * (showList.size() - i));
            } else {
                x = (mWidth / drawCount *i);
            }
            DrawUtils.drawCandle(canvas,
                    parseNumber(showList.get(i).getHigh()),
                    parseNumber(showList.get(i).getLow()),
                    parseNumber(showList.get(i).getOpen()),
                    parseNumber(showList.get(i).getClose()),
                    x,
                    parseNumber(showList.get(i).getHigh()),
                    candleXDistance,
                    mWidth);
        }
    }

    /**
     * SMA5 SMA10 SMA20
     * @param canvas
     */
    @Override
    protected void drawLines(Canvas canvas) {
        if(data == null || data.size() == 0) return;
        float[] sma5 = new float[showList.size()];
        float[] sma10 = new float[showList.size()];
        float[] sma20 = new float[showList.size()];
        int size = showList.size();
        for (int i = 0; i < showList.size(); i++) {
            if(size > IndexParseUtil.START_SMA5)
                sma5[i] = (float) showList.get(i).getSma5();
            if(size > IndexParseUtil.START_SMA10)
                sma10[i] = (float) showList.get(i).getSma10();
            if(size > IndexParseUtil.START_SMA20)
                sma20[i] = (float) showList.get(i).getSma20();
        }
        DrawUtils.drawLineWithXOffset(canvas, sma5, DEFUALT_WIDTH, mainH, ColorUtil.COLOR_SMA5, (float) yMax, (float) yMin, DEFUALT_WIDTH / 2);
        DrawUtils.drawLineWithXOffset(canvas, sma10, DEFUALT_WIDTH, mainH, ColorUtil.COLOR_SMA10, (float) yMax, (float) yMin, DEFUALT_WIDTH / 2);
        DrawUtils.drawLineWithXOffset(canvas, sma20, DEFUALT_WIDTH, mainH, ColorUtil.COLOR_SMA20, (float) yMax, (float) yMin, DEFUALT_WIDTH / 2);
    }

    @Override
    protected void drawText(Canvas canvas) {
        if(data == null || data.size() == 0) return;
        //4，画X轴时间
        if(showList.size() <= drawCount) {
            DrawUtils.drawKLineXTime(canvas, showList.get(0).getTime(), showList.get(showList.size() - 1).getTime(), mWidth, mainH);
        } else {
            DrawUtils.drawKLineXTime(canvas, showList.get(0).getTime(), null, mWidth, mainH);
        }
        //5，画Y轴价格
//        DrawUtils.drawKLineYPrice(canvas, yMax, yMin, mainH);
    }

    @Override
    protected void drawVOL(Canvas canvas) {
        if(data == null || data.size() == 0) return;
        long max = 0;
        for(StickData data : showList) {
            max = data.getCount() > max ? data.getCount() : max;
        }
        //如果量全为0，则不画
        if(max == 0) return;
        //2,画量线，多条竖直线
        DrawUtils.drawVOLRects(canvas, xUnit,indexStartY, indexH, max, showList);
    }

    /**
     * 把传入的参数计算成坐标，直接展示到界面上
     * @param input
     * @return 返回里面的StickData的最高价最低价，都是可以直接显示在坐标上的
     */
    private float parseNumber(double input) {
        return mainH - (float)(( input - yMin) / yUnit);
    }

    public void setDataAndInvalidate(ArrayList<StickData> data) {
        this.data = data;
        parseData();
        postInvalidate();
    }

    /**
     * 获取页面一页可展示的数据
     */
    private void getShowList(int offset) {
        if(offset != 0 && data.size() - drawCount - offset < 0) {
            offset = data.size() - drawCount;
        }
        showList = new ArrayList<>();
        showList.addAll(data.subList(data.size() - drawCount - offset, data.size() - offset));
    }

    /**
     * 设置K线类型
     */
    public void setType(int type) {
        lineType = type;
    }

    /**
     * 计算各指标
     */
    private void parseData() {
        offset = 0;
        //根据当前显示的指标类型，优先计算指标
        IndexParseUtil.initSma(this.data);

    }

    @Override
    public void onCrossMove(float x, float y) {
        super.onCrossMove(x, y);
        if(crossView == null || showList == null) return;
        int position = (int) Math.rint(new Double(x)/ new Double(DEFUALT_WIDTH));
        if(position < showList.size()) {
            StickData data = showList.get(position);
            float xIn = (mWidth / drawCount * position) + (mWidth / candleXDistance / 2);
            CrossBean bean = new CrossBean(xIn, getY(data.getClose()));
            bean.price = data.getClose() + "";
            //注意，这个值用来区分，是否画出均线的小圆圈
            bean.y2 = -1;
            bean.timeYMD = data.getTime();
            crossView.drawLine(bean);
            if(crossView.getVisibility() == GONE)
                crossView.setVisibility(VISIBLE);
           //TODO 这里显示该点数据到屏幕
            msgText.setVisibility(VISIBLE);
            msgText.setText(Html.fromHtml(ColorUtil.getCurPriceInfo(data)));
        }
    }

    @Override
    public void onDismiss() {
        msgText.setVisibility(INVISIBLE);
    }

    //获取价格对应的Y轴
    private float getY(double price) {
        return mainH - (new Float(price) - (float)yMin) / yUnit;
    }


}
