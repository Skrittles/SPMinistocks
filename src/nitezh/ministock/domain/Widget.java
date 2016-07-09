package nitezh.ministock.domain;

import org.json.JSONObject;

import java.util.List;

import nitezh.ministock.Storage;


public interface Widget {
    Storage getStorage();

    void setWidgetPreferencesFromJson(JSONObject jsonPrefs);

    JSONObject getWidgetPreferencesAsJson();

    void enablePercentChangeView();

    void enableDailyChangeView();

    void setStock1(String s);

    void setStock1Summary(String s);

    void save();

    int getId();

    int getSize();

    void setSize(int size);

    boolean isNarrow();

    String getStock(int i);

    int getPreviousView();

    void setView(int view);

    List<String> getSymbols();

    int getSymbolCount();

    String getBackgroundStyle();

    String getVsBackgroundStyle();


    boolean useLargeFont();

    boolean useVsLargeFont();

    boolean getHideSuffix();

    boolean getTextStyle();

    boolean getVsTextStyle();

    String getVsFont();

    boolean getColorsOnPrices();

    String getFooterVisibility();

    String getVsFooterVisibility();

    String getFooterColor();

    String getVsFooterColor();

    boolean showShortTime();

    boolean showVsShortTime();

    String getVsColorCalculation();

    boolean hasDailyChangeView();

    boolean hasTotalPercentView();

    boolean hasDailyPercentView();

    boolean hasTotalChangeView();

    boolean hasTotalChangeAerView();

    boolean hasDailyPlChangeView();

    boolean hasDailyPlPercentView();

    boolean hasTotalPlPercentView();

    boolean hasTotalPlChangeView();

    boolean hasTotalPlPercentAerView();

    boolean isVisual();

}
