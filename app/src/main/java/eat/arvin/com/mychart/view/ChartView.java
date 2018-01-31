package eat.arvin.com.mychart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.support.v4.view.NestedScrollingChild;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by Arvin on 2016/10/25.
 */
public abstract class ChartView extends View implements ChartConstant, CrossView.OnMoveListener, NestedScrollingChild {
    public interface OnDoubleTapListener{
        void onDoubleTap();
    }
    /**
     * 分时线或K线类型，取值见ChartConstant.TYPE_FENSHI
     */
    protected int lineType = TYPE_FENSHI;
    /**
     * 指标的类型
     */
    protected int indexType = INDEX_VOL;
    //分时图,K线高度
    protected float mainH;
    //指标高度
    protected float indexH;
    //指标左上y坐标
    protected float indexStartY;
    //时间左上Y坐标
    protected float timeStartY;
    //总宽
    protected float mWidth;
    //总高
    protected float mHeight;
    //十字线布局
    protected CrossView crossView;
    //十字线点对应的详情展示
    protected TextView msgText;
    //双击监听，全屏
    protected OnDoubleTapListener onDoubleTapListener;
    //十字线最后停下的点，当切换指标的时候，使用这个点来计算指标应该显示的文字
    private float lastX, lastY;

    //烛形图加空白的宽度和烛形图宽度之比
    public static final float WIDTH_SCALE = 1.2f;
    //烛形图和右侧空白的宽度
    protected float DEFUALT_WIDTH = 19;

    //一屏烛形图数量
    protected int drawCount;
    //没两个烛形图x轴的距离
    protected float candleXDistance;

    //当前画图偏移量（往右滑动之后）
    protected int offset;

    //y轴最大值
    protected double yMax;
    //y轴最小值
    protected double yMin;
    protected float xUnit;// 每个数据占的宽度 x轴每两点距离
    protected int scale = 2;// 价格 保留2位小数

    //是否可以加载更多,出现这个属性的原因，防止多次加载更多，不可修改
    protected boolean mCanLoadMore = true;

    /**
     * 左右拖动思路：这里开始处理分时图的左右移动问题，思路：当手指移动时，会有移动距离（A），我们又有x轴的单位距离(B)，
     * 所以可以计算出来需要移动的个数（C=A/B,注意向上取整）。
     * 这个时候，就可以确定新的开始位置（D）和新的结束位置（E）：
     * D=mBeginIndex±C,E=mEndIndex干C，正负号取决于移动方向。
     */
    //手指按下的个数
    protected int mFingerPressedCount;
    //是否是向右拉，不可修改
    protected boolean mPullRight = false;
    //按下的x轴坐标
    protected float mPressedX;
    //按下的时刻
    protected long mPressTime;
    //手指移动的类型，默认在最后边
    protected PullType mPullType = PullType.PULL_RIGHT_STOP;

    //默认情况下结束点距离右边边距
    protected float mInnerRightBlankPadding = 0;
    //右侧内边距，默认情况下结束点距离右边边距（单位：sp）
    protected static final float DEF_INNER_RIGHT_BLANK_PADDING = 0;

    // 注意，遵循取前不取后，因此mEndIndex这个点不应该取到,但是mBeginIndex会取到。
    //数据开始位置，数据集合的起始位置
    protected int mBeginIndex = 0;
    //数据的结束位置，这里之所以定义结束位置，因为数据可能会小于mShownMaxCount。
    protected int mEndIndex;

    //加载更多阀值。当在左侧不可见范围内还剩多少数据时开始加载更多。（单位：数据个数）
    protected static final int DEF_MINLEN_LOADMORE = 10;


    //事件监听回调
    protected TimeSharingListener mTimeSharingListener;

    //监听回调
    public interface TimeSharingListener {
//        void onLongTouch(Quotes preQuotes, Quotes currentQuotes);

        void onUnLongTouch();

        void needLoadMore();
    }

    protected enum PullType {
        PULL_RIGHT,//右->左滑动
        PULL_LEFT,//左->右滑动
        PULL_RIGHT_STOP,//滑动到最右边
        PULL_LEFT_STOP,//滑动到最左边
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //关闭硬件加速，不然虚线显示为实线了
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    protected abstract boolean onViewScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

    //点击手势监听
    protected GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(final MotionEvent e) {
            //延时300毫秒显示，为双击腾出时间
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //单击显示十字线
                    if(crossView != null) {
                        if (crossView.getVisibility() == View.GONE) {
                            onCrossMove(e.getX(), e.getY());
                        }
                    }
                }
            }, DOUBLE_TAP_DELAY);
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            onDoubleTapListener.onDoubleTap();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
           return onViewScroll(e1, e2, distanceX, distanceY);
        }


    });

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //1,初始化需要的数据
        initWidthAndHeight();
        //2，画网格
        drawGrid(canvas);
        //3，画线（分时线的价格线、均价线或K线的均线）
        drawLines(canvas);
        if(lineType != TYPE_FENSHI) {
            //4，如果是K线另外画烛形图
            drawCandles(canvas);
        }
        //5，写上XY轴的文字（写早了会被覆盖）
        drawText(canvas);
        //6，画需要显示的指标
        switch (indexType) {
            case INDEX_VOL:
                drawVOL(canvas);
                break;
        }
    }

    private void initWidthAndHeight() {
        mHeight = getMeasuredHeight();
        mWidth = getMeasuredWidth();
        mainH = mHeight * MAIN_SCALE;
        indexH = mHeight * INDEX_SCALE;
        indexStartY = mHeight - indexH;
        timeStartY = indexStartY - mHeight * TIME_SCALE;
        init();
    }

    /**
     * 画网格包含主网格和指标网格
     * @param canvas
     */
    protected abstract void drawGrid(Canvas canvas);

    /**
     * 写X，Y轴文字
     * @param canvas
     */
    protected abstract void drawText(Canvas canvas);

    /**
     * 画量
     * @param canvas
     */
    protected abstract void drawVOL(Canvas canvas);

    /**
     *在开始画图前，初始化数据
     */
    protected abstract void init();

    /**
     * 分时价格线均线、K线的均线
     * @param canvas
     */
    protected void drawLines(Canvas canvas) {}
    protected void drawCandles(Canvas canvas) {}

    /**
     * 十字线显示的时候，切换指标之后，让十字线显示的指标文字也切换掉
     */
    protected void resetIndexText() {
        if(crossView != null && crossView.getVisibility() == VISIBLE) {
            onCrossMove(lastX, lastY);
        }
    }

    /**
     * 设置需要使用的View
     * @param crossView
     * @param msgText
     */
    public void setUsedViews(CrossView crossView, TextView msgText) {
        this.crossView = crossView;
        this.msgText = msgText;
        crossView.setOnMoveListener(this);
    }

    public void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) {
        this.onDoubleTapListener = onDoubleTapListener;
    }

    @Override
    public void onCrossMove(float x, float y) {
        lastX = x;
        lastY = y;
    }

    public void setmTimeSharingListener(TimeSharingListener mTimeSharingListener) {
        this.mTimeSharingListener = mTimeSharingListener;
    }

    /**
     * 加载更多失败，在这里添加逻辑
     */
    public void loadMoreError() {
        mCanLoadMore = true;
//        Toast.makeText(mContext, "加载更多失败", Toast.LENGTH_SHORT).show();
    }

    /**
     * 加载更多成功，在这里添加逻辑
     */
    public void loadMoreSuccess() {
        mCanLoadMore = true;
//        Toast.makeText(getContext(), "加载更多成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 正在加载更多，在这里添加逻辑
     */
    public void loadMoreIng() {
        mCanLoadMore = false;
//        Toast.makeText(mContext, "正在加载更多", Toast.LENGTH_SHORT).show();
    }

    /**
     * 没有更多数据，在这里添加逻辑
     */
    public void loadMoreNoData() {
        mCanLoadMore = false;
//        Toast.makeText(mContext, "加载更多，没有数据了...", Toast.LENGTH_SHORT).show();
    }

}
