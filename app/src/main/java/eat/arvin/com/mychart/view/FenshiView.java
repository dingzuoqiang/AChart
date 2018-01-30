package eat.arvin.com.mychart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Html;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.ArrayList;

import eat.arvin.com.mychart.bean.CMinute;
import eat.arvin.com.mychart.bean.CrossBean;
import eat.arvin.com.mychart.bean.FenshiDataResponse;
import eat.arvin.com.mychart.utils.ColorUtil;
import eat.arvin.com.mychart.utils.DrawUtils;
import eat.arvin.com.mychart.utils.GridUtils;
import eat.arvin.com.mychart.utils.LineUtil;

/**
 * Created by Administrator on 2016/10/25.
 */
public class FenshiView extends ChartView {
    //分时数据
    private FenshiDataResponse data;
    //补全后的所有点
    private ArrayList<CMinute> minutes;
    //展示的数据
    private ArrayList<CMinute> showList;

    //所有价格
    private float[] price;
    //所有均线数据
    private float[] average;
    //分时线昨收
    private double yd;

    public FenshiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean onViewScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    private ScaleGestureDetector mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener(){
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if(minutes == null) return super.onScale(detector);
            //放大是由1变大，缩小是由1变小
            float scale = detector.getScaleFactor();
            //这个变化太快，把scale变慢一点
            scale = 1 + ((scale - 1) * 0.4f);
            drawCount = (int) (mWidth / DEFUALT_WIDTH);
            if(scale < 1 && drawCount >= minutes.size()) {
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
        if(minutes == null) return;
        yd = data.getParam().getLast();
//        yd = 0;
//        xUnit = mWidth / LineUtil.getShowCount(data.getParam().getDuration());
        drawCount = (int) (mWidth / DEFUALT_WIDTH);
        candleXDistance = drawCount * WIDTH_SCALE;
        if(minutes != null && minutes.size() > 0) {
            if(drawCount < minutes.size()) {
                getShowList(offset);
            } else {
                showList = new ArrayList<>();
                showList.addAll(minutes);
            }
        }
        if(showList == null) return;

        //计算最大最小值
        boolean first = true;
        for(CMinute c : showList) {
            if(first) {
                first = false;
                yMax = c.getPrice();
                yMin = c.getPrice();
            }
            yMax = c.getPrice() > yMax ? c.getPrice() : yMax;
            yMax = c.getAverage() > yMax ? c.getAverage() : yMax;
            if(c.getPrice() != 0)
                yMin = c.getPrice() < yMin ? c.getPrice() : yMin;
            if(c.getAverage() != 0 && c.getAverage() != 0.01)
                yMin = c.getAverage() < yMin ? c.getAverage() : yMin;
        }
        xUnit = mWidth / drawCount;
    }

    @Override
    protected void drawGrid(Canvas canvas) {
        //1,画网格
//        if(data != null && data.getParam() != null && LineUtil.isIrregular(data.getParam().getDuration())) {
//            //如果是不规则网格画不规则网格
//            GridUtils.drawIrregularGrid(canvas, mWidth, mainH, data.getParam().getDuration());
//            GridUtils.drawIrregularIndexGrid(canvas, indexStartY, mWidth, indexH, data.getParam().getDuration());
//        } else {
//            GridUtils.drawGrid(canvas, mWidth, mainH);
                 GridUtils.drawIndexGrid(canvas, indexStartY, mWidth, indexH);
//        }

    }

    /**
     * 重新画分时图
     * @param data
     */
    public void setDataAndInvalidate(FenshiDataResponse data) {
        this.data = data;
        minutes = LineUtil.getAllFenshiData(data);
        parseData();

        postInvalidate();
    }

    /**
     * 获取页面一页可展示的数据
     */
    private void getShowList(int offset) {
        if(offset != 0 && minutes.size() - drawCount - offset < 0) {
            offset = minutes.size() - drawCount;
        }
        showList = new ArrayList<>();
        showList.addAll(minutes.subList(minutes.size() - drawCount - offset, minutes.size() - offset));
    }


    /**
     * 计算各指标
     */
    private void parseData() {
        offset = 0;
        //根据当前显示的指标类型，优先计算指标
//        IndexParseUtil.initSma(this.data);

    }


    @Override
    protected void drawLines(Canvas canvas) {
        if(data == null) return;
        drawPriceLine(canvas);
        drawAverageLine(canvas);
    }


    @Override
    protected void drawText(Canvas canvas) {
        if(minutes == null || minutes.size() == 0) return;
//        DrawUtils.drawYPercentAndPrice(canvas, yMax, yMin, yd,mWidth, mainH);
        DrawUtils.drawXTime(canvas, data.getParam().getDuration(), data.getParam().getUntil(),mWidth, mainH);
    }

    @Override
    protected void drawVOL(Canvas canvas) {
        if(minutes == null || minutes.size() == 0) return;
        long max = 0;
        for(CMinute minute : showList) {
            max = minute.getCount() > max ? minute.getCount() : max;
        }
        //如果量全为0，则不画
        if(max != 0) {
            //2,画量线，多条竖直线
            DrawUtils.drawVOLRects(canvas, xUnit, indexStartY, indexH, max, (float) yd, showList);
        }
    }

    /**
     * 价格线
     * @param canvas
     */
    private void drawPriceLine(Canvas canvas) {
        price = new float[showList.size()];
        for(int i = 0; i < showList.size(); i++) {
            price[i] = (float) showList.get(i).getPrice();
        }
        //乘以1.001是为了让上下分别空一点出来
        double[] maxAndMin = LineUtil.getMaxAndMinByYd(yMax, yMin, yd);
        DrawUtils.drawPriceShader(canvas, price, xUnit, mainH, (float) maxAndMin[0], (float) maxAndMin[1]);

        DrawUtils.drawLines(canvas, price,xUnit , mainH, ColorUtil.COLOR_PRICE_LINE, (float) maxAndMin[0], (float) maxAndMin[1], false);
    }


    private void drawAverageLine(Canvas canvas) {
        average = new float[showList.size()];
        for(int i = 0; i < showList.size(); i++) {
            average[i] = (float) showList.get(i).getAverage();
        }
        float[] maxAndMin1 = LineUtil.getMaxAndMin(average);
        //如果均线值全为0.01则不画改线，否则会影响价格线展示
        if(maxAndMin1[0] == 0.01 && maxAndMin1[1] == 0.01)
            return;
        //乘以1.001是为了让上下分别空一点出来
        double[] maxAndMin = LineUtil.getMaxAndMinByYd(yMax, yMin, yd);
//        DrawUtils.drawPriceShader(canvas, price, xUnit, mainH, (float) maxAndMin[0], (float) maxAndMin[1]);
        DrawUtils.drawLines(canvas, average, xUnit, mainH, ColorUtil.COLOR_SMA_LINE, (float) maxAndMin[0], (float) maxAndMin[1], false);
    }

    /**
     * 当十字线移动到某点时，回调到此处，用此处的数据判断是否显示十字线
     * @param x x轴坐标
     * @param y y轴坐标
     */
    @Override
    public void onCrossMove(float x, float y) {
        super.onCrossMove(x, y);
        if(crossView == null || showList == null) return;
        int position = (int) Math.rint(new Double(x)/ new Double(DEFUALT_WIDTH));
        if(position < showList.size()) {
            CMinute cMinute = showList.get(position);
            float xIn = (mWidth / drawCount * position) + (mWidth / candleXDistance / 2);
            float cy = (float) getY(cMinute.getPrice());
            CrossBean bean = new CrossBean(xIn, cy);
            bean.y2 = (float) getY(cMinute.getAverage());
            bean.price = cMinute.getPrice() + "";
            bean.time = cMinute.getTime();
            setIndexTextAndColor(position, cMinute, bean);
            crossView.drawLine(bean);
            if(crossView.getVisibility() == GONE)
                crossView.setVisibility(VISIBLE);
            //TODO 此处把该点的数据写到界面上
            msgText.setVisibility(VISIBLE);
            msgText.setText(Html.fromHtml(getCurPriceInfo(cMinute)));
        }
    }

    @Override
    public void onDismiss() {
        msgText.setVisibility(INVISIBLE);
    }

    /**
     * 计算指标左上角应该显示的文字
     */
    private void setIndexTextAndColor(int position, CMinute cMinute, CrossBean bean) {
        switch (indexType) {
            case INDEX_VOL:
                bean.indexText = new String[]{"VOL:" + cMinute.getCount()};
                bean.indexColor = new int[]{cMinute.getPrice() > yd ? ColorUtil.INCREASING_COLOR : ColorUtil.DECREASING_COLOR};
                break;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(crossView != null)
            crossView.setVisibility(GONE);
    }

    //获取价格对应的Y轴
    private double getY(double price) {
        double[] maxAndMin = LineUtil.getMaxAndMinByYd(yMax, yMin, yd);
        if(price == maxAndMin[0]) return 0;
        if(price == maxAndMin[1]) return mainH;
        return mainH - (new Float(price) - maxAndMin[1]) / ((maxAndMin[0] - maxAndMin[1]) / mainH);
    }

    /**
     * 价格信息
     *
     * @param entity
     * @return
     */
    public String getCurPriceInfo(CMinute entity) {
        return ColorUtil.getCurPriceInfo(entity, yd);
    }
}
