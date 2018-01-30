package eat.arvin.com.mychart.utils;

import android.graphics.Color;

import eat.arvin.com.mychart.bean.CMinute;
import eat.arvin.com.mychart.bean.StickData;


/**
 * Created by Administrator on 2016/8/25.
 */
public class ColorUtil {

//    <color name="chart_info_color">#88000000</color>
//    <color name="chart_info_text_color">#ffffff</color>
//    <color name="chart_no_data_color">#be945c</color>
//    <color name="highlight_color">#be945c</color>

    // 坐标轴 lable 颜色
    public static final int AXIS_COLOR = Color.parseColor("#999999");
    // 中间 极限线
    public static final int LIMIT_COLOR = Color.parseColor("#AFAFAF");
    // 阴影层
    public static final int COLOR_SHADOW = Color.parseColor("#D4E8F1");

    //跌颜色
    public static final int DECREASING_COLOR = Color.parseColor("#E8003F");
    //涨颜色
    public static final int INCREASING_COLOR = Color.parseColor("#0B841A");

    //平灰
    public static final int COLOR_PING_ASH = Color.parseColor("#333333");
    //平白
    public static final int COLOR_PING_WHITE = Color.parseColor("#efefef");
    //5均线颜色
    public static final int COLOR_SMA5 = Color.parseColor("#43556D");
    //10均线颜色
    public static final int COLOR_SMA10 = Color.parseColor("#D8C02C");
    //20均线颜色
    public static final int COLOR_SMA20 = Color.parseColor("#e9837e");

    //分时线价格颜色
    public static final int COLOR_PRICE_LINE = Color.parseColor("#308BBA");
    //分时线均线颜色
    public static final int COLOR_SMA_LINE = Color.parseColor("#FED100");
    public static final int COLOR_CROSS_LINE = Color.parseColor("#2e68b2");


    public static String getColorRGB(double curr, double change) {
        if (curr > change) return "#e36d50";
        if (curr < change) return "#3b7f19";
        return "#333333";
    }

    /**
     * 获取价格显示的颜色，curr>change是绿，等于是黑，小于是红
     * 平是灰色
     *
     * @param curr   当前价
     * @param change 变化颜色的价格
     * @return
     */
    public static int getTextColorAsh(double curr, double change) {
        if (curr == change)
            return COLOR_PING_ASH;
        if (curr < change)
            return DECREASING_COLOR;
        return INCREASING_COLOR;
    }

    /**
     * 价格信息
     * 十字线滑动时，显示部分信息
     *
     * @param entity
     * @return
     */
    public static String getCurPriceInfo(CMinute entity, double yd) {

        StringBuffer sb = new StringBuffer();
        sb.append("   " + entity.getTimeStr());
        if (entity.getPrice() >= yd) {
            sb.append("  价格" + "<font color='#e36d50'>" + NumberUtil.beautifulDouble(entity.getPrice()) + "</font>");
        } else {
            sb.append("  价格" + "<font color='#3b7f19'>" + NumberUtil.beautifulDouble(entity.getPrice()) + "</font>");
        }
        if (entity.getRate() >= 0) {
            sb.append("  涨幅" + "<font color='#e36d50'>" + NumberUtil.beautifulDouble(entity.getRate()) + "%</font>");
        } else {
            sb.append("  涨幅" + "<font color='#3b7f19'>" + NumberUtil.beautifulDouble(entity.getRate()) + "%</font>");
        }
        sb.append("  成交" + entity.getCount());
        if (entity.getAverage() >= yd) {
            sb.append("  均价" + "<font color='#e36d50'>" + NumberUtil.beautifulDouble(entity.getAverage()) + "</font>");
        } else {
            sb.append("  均价" + "<font color='#3b7f19'>" + NumberUtil.beautifulDouble(entity.getAverage()) + "</font>");
        }
        return sb.toString();
    }

    /**
     * 价格、涨跌量、涨幅
     *
     * @param now
     * @param diff
     * @param rate
     * @return
     */
    public static String getHtmlText(String now, String diff, String rate) {
        StringBuffer sb = new StringBuffer();
        if (Double.parseDouble(diff) >= 0) {
            sb.append("<font color='#e36d50'>" + NumberUtil.getMoneyString(Double.parseDouble(now == null ? "0" : now)) + "</font>");
            sb.append("　　<font color='#e36d50'>+" + diff + "</font>");
            sb.append("　　<font color='#e36d50'>" + rate + "%</font>");
        } else {
            sb.append("<font color='#3b7f19'>" + NumberUtil.getMoneyString(Double.parseDouble(now == null ? "0" : now)) + "</font>");
            sb.append("　　<font color='#3b7f19'>" + diff + "</font>");
            sb.append("　　<font color='#3b7f19'>" + rate + "%</font>");
        }
        return sb.toString();
    }


    public static String getCurPriceInfo(StickData entity) {
        StringBuffer sb = new StringBuffer();
        sb.append("  " + entity.getTime());
        if (entity.getOpen() >= entity.getLast()) {
            sb.append("  开" + "<font color='#e36d50'>" + entity.getOpen() + "</font>");
        } else {
            sb.append("  开" + "<font color='#3b7f19'>" + entity.getOpen() + "</font>");
        }

        if (entity.getClose() >= entity.getLast()) {
            sb.append("  收" + "<font color='#3b7f19'>" + entity.getClose() + "</font>");
        } else {
            sb.append("  收" + "<font color='#e36d50'>" + entity.getClose() + "</font>");
        }

        if (entity.getHigh() >= entity.getLast()) {
            sb.append("  高" + "<font color='#e36d50'>" + entity.getHigh() + "</font>");
        } else {
            sb.append("  高" + "<font color='#3b7f19'>" + entity.getHigh() + "</font>");
        }
        if (entity.getLow() >= entity.getLast()) {
            sb.append("  低" + "<font color='#e36d50'>" + entity.getLow() + "</font>");
        } else {
            sb.append("  低" + "<font color='#3b7f19'>" + entity.getLow() + "</font>");
        }
        sb.append("  量" + entity.getCount());
        sb.append("  额" + NumberUtil.formartBigNumber(entity.getMoney()));
//        if (entity.getClose() >= entity.getLast()) {
//            sb.append("  量" + "<font color='#3b7f19'>" + entity.getCount() + "</font>");
//        } else {
//            sb.append("  量" + "<font color='#e36d50'>" + entity.getCount() + "</font>");
//        }
//        if (entity.getOpen() >= entity.getClose()) {
//            sb.append("  额" + "<font color='#3b7f19'>" + NumberUtil.formartBigNumber(entity.getMoney()) + "</font>");
//        } else {
//            sb.append("  额" + "<font color='#e36d50'>" + NumberUtil.formartBigNumber(entity.getMoney()) + "</font>");
//        }
        sb.append("  SMA5:" + "<font color='#f2cfa9'>" + NumberUtil.beautifulDouble(entity.getSma5()) + "</font>");
        sb.append("  SMA10:" + "<font color='#687cd5'>" + NumberUtil.beautifulDouble(entity.getSma10()) + "</font>");
        sb.append("  SMA20:" + "<font color='#e9837e'>" + NumberUtil.beautifulDouble(entity.getSma20()) + "</font>");
        return sb.toString();
    }
}
