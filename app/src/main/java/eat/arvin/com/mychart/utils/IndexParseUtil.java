package eat.arvin.com.mychart.utils;


import java.util.List;

import eat.arvin.com.mychart.bean.StickData;

/**
 * Created by Arvin on 2016/9/14.
 * 计算各种指标
 */
public class IndexParseUtil {

    //均线跨度(SMA5,SMA10,SMA20),注意修改该值时，需要同时增加StickData里面的sma字段、修改本类initSma方法，否则不会生效
    public static final int START_SMA5 = 5;
    public static final int START_SMA10 = 10;
    public static final int START_SMA20 = 20;

    public static final int[] SMA = {START_SMA5,START_SMA10, START_SMA20};

    /**
     * 把list里面所有数据对应的均线计算出来并且赋值到里面
     *
     * @param list k线数据
     */
    public static void initSma(List<StickData> list) {
        if (list == null) return;
        for (int i = 0; i < list.size(); i++) {
            for (int j : SMA) {
                if (i + j <= list.size()) {
                    //第5日开始计算5日均线
                    if (j == START_SMA5) {
                        //K线的SMA5
                        list.get(i + j - 1).setSma5(getCloseSma(list.subList(i, i + j)));
                    } else
                        //第10日开始计算10日均线
                        if (j == START_SMA10) {
                            //K线的SMA10
                            list.get(i + j - 1).setSma10(getCloseSma(list.subList(i, i + j)));
                        }else
                            //第20日开始计算20日均线
                            if (j == START_SMA20) {
                                //K线的SMA20
                                list.get(i + j - 1).setSma20(getCloseSma(list.subList(i, i + j)));
                            }
                }
            }
        }
    }

    /**
     * K线收盘价计算移动平均价
     * @param datas
     * @return
     */
    private static double getCloseSma(List<StickData> datas) {
        if (datas == null) return -1;
        double sum = 0;
        for (StickData data : datas) {
            sum += data.getClose();
        }
        return NumberUtil.doubleDecimal(sum / datas.size());
    }

}
