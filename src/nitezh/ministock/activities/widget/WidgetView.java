/*
 The MIT License
 
 Copyright (c) 2013 Nitesh Patel http://niteshpatel.github.io/ministocks
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package nitezh.ministock.activities.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import nitezh.ministock.PreferenceStorage;
import nitezh.ministock.R;
import nitezh.ministock.Storage;
import nitezh.ministock.activities.PreferencesActivity;
import nitezh.ministock.utils.StorageCache;
import nitezh.ministock.WidgetProvider;
import nitezh.ministock.domain.AndroidWidgetRepository;
import nitezh.ministock.domain.PortfolioStock;
import nitezh.ministock.domain.PortfolioStockRepository;
import nitezh.ministock.domain.StockQuote;
import nitezh.ministock.domain.Widget;
import nitezh.ministock.domain.WidgetRepository;
import nitezh.ministock.domain.WidgetStock;
import nitezh.ministock.utils.CurrencyTools;
import nitezh.ministock.utils.NumberTools;
import nitezh.ministock.utils.ReflectionTools;

import static nitezh.ministock.activities.widget.WidgetProviderBase.UpdateType;
import static nitezh.ministock.activities.widget.WidgetProviderBase.ViewType;

public class WidgetView {

    private final RemoteViews remoteViews;
    private final Widget widget;
    private final boolean hasPortfolioData;
    private final List<String> symbols;
    private final HashMap<String, PortfolioStock> portfolioStocks;
    private final HashMap<String, StockQuote> quotes;
    private final UpdateType updateMode;
    private final String quotesTimeStamp;
    private final Context context;
    private HashMap<ViewType, Boolean> enabledViews;
    // The amount of stock preferences in preferences.xml
    private static final int MAX_STOCKS = PreferencesActivity.MAX_STOCKS;

    public WidgetView(Context context, int appWidgetId, UpdateType updateMode,
                      HashMap<String, StockQuote> quotes, String quotesTimeStamp) {
        WidgetRepository widgetRepository = new AndroidWidgetRepository(context);

        this.context = context;
        this.widget = widgetRepository.getWidget(appWidgetId);
        this.quotes = quotes;
        this.quotesTimeStamp = quotesTimeStamp;
        this.updateMode = updateMode;
        this.symbols = widget.getSymbols();

        Storage storage = PreferenceStorage.getInstance(context);
        this.portfolioStocks = new PortfolioStockRepository(PreferenceStorage.getInstance(context),
                new StorageCache(storage), widgetRepository).getStocksForSymbols(symbols);
        this.hasPortfolioData = !portfolioStocks.isEmpty();

        this.remoteViews = this.getBlankRemoteViews(this.widget, context.getPackageName());
        this.enabledViews = this.calculateEnabledViews(this.widget);
    }

    private RemoteViews getBlankRemoteViews(Widget widget, String packageName) {
        String backgroundStyle = widget.getBackgroundStyle();
        boolean useLargeFont = widget.useLargeFont();
        RemoteViews views;

        //Load layout depending on whether visual stockboard and uselargefont are enabled
        if (widget.isVisual()) {
            backgroundStyle = widget.getVsBackgroundStyle();
            useLargeFont = widget.useVsLargeFont();
            if (widget.getSize() == 1) {
                if (useLargeFont) {
                    views = new RemoteViews(packageName, R.layout.widget_visual_1x4_large);
                } else {
                    views = new RemoteViews(packageName, R.layout.widget_visual_1x4);
                }
            } else if (widget.getSize() == 2) {
                if (useLargeFont) {
                    views = new RemoteViews(packageName, R.layout.widget_visual_2x2_large);
                } else {
                    views = new RemoteViews(packageName, R.layout.widget_visual_2x2);
                }
            } else if (widget.getSize() == 3) {
                if (useLargeFont) {
                    views = new RemoteViews(packageName, R.layout.widget_visual_2x4_large);
                } else {
                    views = new RemoteViews(packageName, R.layout.widget_visual_2x4);
                }
            } else {
                if (useLargeFont) {
                    views = new RemoteViews(packageName, R.layout.widget_visual_1x2_large);
                } else {
                    views = new RemoteViews(packageName, R.layout.widget_visual_1x2);
                }
            }
        } else {
            if (widget.getSize()== 1) {
                if (useLargeFont) {
                    views = new RemoteViews(packageName, R.layout.widget_1x4_large);
                } else {
                    views = new RemoteViews(packageName, R.layout.widget_1x4);
                }
            } else if (widget.getSize() == 2) {
                if (useLargeFont) {
                    views = new RemoteViews(packageName, R.layout.widget_2x2_large);
                } else {
                    views = new RemoteViews(packageName, R.layout.widget_2x2);
                }
            } else if (widget.getSize() == 3) {
                if (useLargeFont) {
                    views = new RemoteViews(packageName, R.layout.widget_2x4_large);
                } else {
                    views = new RemoteViews(packageName, R.layout.widget_2x4);
                }
            } else {
                if (useLargeFont) {
                    views = new RemoteViews(packageName, R.layout.widget_1x2_large);
                } else {
                    views = new RemoteViews(packageName, R.layout.widget_1x2);
                }
            }
        }

        views.setImageViewResource(R.id.widget_bg,
                getImageViewSrcId(backgroundStyle, useLargeFont));
        this.hideUnusedStocks(views, widget.getSymbolCount());
        return views;
    }

    //Load widget background depending on whether font size and visual stockboard are enabled
    private int getImageViewSrcId(String backgroundStyle, Boolean useLargeFont) {
        Integer imageViewSrcId;
        switch (backgroundStyle) {
            case "transparent":
                if (widget.isVisual()) {
                    imageViewSrcId = R.drawable.ministock_bg_transparent68;
                }else if(useLargeFont) {
                    imageViewSrcId = R.drawable.ministock_bg_transparent68_large;
                } else {
                    imageViewSrcId = R.drawable.ministock_bg_transparent68;
                }
                break;
            case "none":
                imageViewSrcId = R.drawable.blank;
                break;
            default:
                if (widget.isVisual()) {
                    imageViewSrcId = R.drawable.ministock_bg;
                } else if (useLargeFont) {
                    imageViewSrcId = R.drawable.ministock_bg_large;
                } else {
                    imageViewSrcId = R.drawable.ministock_bg;
                }
                break;
        }
        return imageViewSrcId;
    }

    // Global formatter so we can perform global text formatting in one place
    private SpannableString applyFormatting(String s) {
        SpannableString span = new SpannableString(s);
        String font = this.widget.getVsFont();
        if (widget.isVisual()) {
            //change font for Visual Stockboard
            if (font.equals("light")) {
                span.setSpan(new TypefaceSpan("sans-serif-light"),0,s.length(),0);
            } else if (font.equals("condensed")) {
                span.setSpan(new TypefaceSpan("sans-serif-light"),0,s.length(),0);
            } else {
                span.setSpan(new TypefaceSpan("sans-serif"),0,s.length(),0);
            }
            //change text style for Visual Stockboard
            if (this.widget.getVsTextStyle()) {
                span.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), 0);
            } else {
                span.setSpan(new StyleSpan(Typeface.NORMAL), 0, s.length(), 0);
            }
        //change text style in standard view
        } else
        if (this.widget.getTextStyle()) {
            span.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), 0);
        } else {
            span.setSpan(new StyleSpan(Typeface.NORMAL), 0, s.length(), 0);
        }
        return span;
    }

    public void setOnClickPendingIntents() {
        Intent leftTouchIntent = new Intent(this.context, WidgetProvider.class);
        leftTouchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, this.widget.getId());
        leftTouchIntent.setAction("LEFT");
        this.remoteViews.setOnClickPendingIntent(R.id.widget_left,
                PendingIntent.getBroadcast(this.context, this.widget.getId(), leftTouchIntent, 0));

        Intent rightTouchIntent = new Intent(this.context, WidgetProvider.class);
        rightTouchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, this.widget.getId());
        rightTouchIntent.setAction("RIGHT");
        this.remoteViews.setOnClickPendingIntent(R.id.widget_right,
                PendingIntent.getBroadcast(this.context, this.widget.getId(), rightTouchIntent, 0));
    }

    public HashMap<WidgetProviderBase.ViewType, Boolean> getEnabledViews() {
        return this.enabledViews;
    }

    public HashMap<ViewType, Boolean> calculateEnabledViews(Widget widget) {
        if (widget.isVisual()){
            HashMap<WidgetProviderBase.ViewType, Boolean> enabledViewsVs = new HashMap<>();
            enabledViewsVs.put(ViewType.VIEW_DAILY_PERCENT, true);
            enabledViewsVs.put(ViewType.VIEW_DAILY_CHANGE, false);
            enabledViewsVs.put(ViewType.VIEW_PL_CHANGE, false);
            enabledViewsVs.put(ViewType.VIEW_PL_DAILY_CHANGE, false);
            enabledViewsVs.put(ViewType.VIEW_PL_PERCENT, this.hasPortfolioData);
            enabledViewsVs.put(ViewType.VIEW_PL_DAILY_PERCENT, false);
            enabledViewsVs.put(ViewType.VIEW_PL_PERCENT_AER, this.hasPortfolioData);
            enabledViewsVs.put(ViewType.VIEW_PORTFOLIO_CHANGE, false);
            enabledViewsVs.put(ViewType.VIEW_PORTFOLIO_PERCENT, this.hasPortfolioData);
            enabledViewsVs.put(ViewType.VIEW_PORTFOLIO_PERCENT_AER, false);
            return enabledViewsVs;
        } else {
            HashMap<WidgetProviderBase.ViewType, Boolean> enabledViews = new HashMap<>();
            enabledViews.put(ViewType.VIEW_DAILY_PERCENT, widget.hasDailyPercentView());
            enabledViews.put(ViewType.VIEW_DAILY_CHANGE, widget.hasDailyChangeView());
            enabledViews.put(ViewType.VIEW_PORTFOLIO_PERCENT, widget.hasTotalPercentView() && this.hasPortfolioData);
            enabledViews.put(ViewType.VIEW_PORTFOLIO_CHANGE, widget.hasTotalChangeView() && this.hasPortfolioData);
            enabledViews.put(ViewType.VIEW_PORTFOLIO_PERCENT_AER, widget.hasTotalChangeAerView() && this.hasPortfolioData);
            enabledViews.put(ViewType.VIEW_PL_DAILY_PERCENT, widget.hasDailyPlPercentView() && this.hasPortfolioData);
            enabledViews.put(ViewType.VIEW_PL_DAILY_CHANGE, widget.hasDailyPlChangeView() && this.hasPortfolioData);
            enabledViews.put(ViewType.VIEW_PL_PERCENT, widget.hasTotalPlPercentView() && this.hasPortfolioData);
            enabledViews.put(ViewType.VIEW_PL_CHANGE, widget.hasTotalPlChangeView() && this.hasPortfolioData);
            enabledViews.put(ViewType.VIEW_PL_PERCENT_AER, widget.hasTotalPlPercentAerView() && this.hasPortfolioData);
            return enabledViews;
        }
    }

    private WidgetRow getRowInfo(String symbol, ViewType widgetView) {
        WidgetRow widgetRow = new WidgetRow(this.widget);
        widgetRow.setSymbol(symbol);

        // If there is no quote info return immediately
        StockQuote quote = this.quotes.get(symbol);
        if (quote == null || quote.getPrice() == null || quote.getPercent() == null) {
            widgetRow.setHasNoData(true);
            if (this.widget.isNarrow()) {
                widgetRow.setPrice("no");
                widgetRow.setPriceColor(Color.GRAY);
                widgetRow.setStockInfo("data");
                widgetRow.setStockInfoColor(Color.GRAY);
            } else if (this.widget.isVisual()) {
                widgetRow.setStockInfoExtra2("no");
                widgetRow.setStockInfoExtra2Color(Color.GRAY);
                widgetRow.setStockInfoExtra3("data");
                widgetRow.setStockInfoExtra3Color(Color.GRAY);
            } else {
                widgetRow.setStockInfoExtra("no");
                widgetRow.setStockInfoExtraColor(Color.GRAY);
                widgetRow.setStockInfo("data");
                widgetRow.setStockInfoColor(Color.GRAY);
            }
                return widgetRow;
        }

            // Set default values
            PortfolioStock portfolioStock = this.portfolioStocks.get(symbol);
            WidgetStock widgetStock = new WidgetStock(quote, portfolioStock);
            widgetRow.setPrice(widgetStock.getPrice());
            widgetRow.setStockInfo(widgetStock.getDailyPercent());
            widgetRow.setStockInfoColor(WidgetColors.NA);
            if (!widget.isNarrow() && !widget.isVisual()) {
                widgetRow.setSymbol(widgetStock.getDisplayName());
                widgetRow.setVolume(widgetStock.getVolume());
                widgetRow.setVolumeColor(WidgetColors.VOLUME);
                widgetRow.setStockInfoExtra(widgetStock.getDailyChange());
                widgetRow.setStockInfoExtraColor(WidgetColors.NA);
            }
            if (widget.isVisual()) {
                widgetRow.setVolume(widgetStock.getVolume());
                widgetRow.setVolumeColor(WidgetColors.VOLUME);
                widgetRow.setStockInfoExtra(widgetStock.getDailyChange());
                widgetRow.setStockInfoExtraColor(WidgetColors.NA);
                widgetRow.setStockInfoExtra2(widgetStock.getPlTotalChange());
            }

        Boolean plView = false;
        Boolean plChange = false;
        String priceColumn = null;
        String stockInfo = null;
        String stockInfoExtra = null;
        String stockInfoExtra2 = null;
        String stockInfoExtra3 = null;

        switch (widgetView) {
            case VIEW_DAILY_PERCENT:
                    stockInfo = widgetStock.getDailyPercent();
                    stockInfoExtra = widgetStock.getDailyChange();
                break;

            case VIEW_DAILY_CHANGE:
                stockInfo = widgetStock.getDailyChange();
                stockInfoExtra = widgetStock.getDailyPercent();
                break;

            case VIEW_PORTFOLIO_PERCENT:
                if (!widget.isVisual()) {
                    stockInfo = widgetStock.getTotalPercent();
                    stockInfoExtra = widgetStock.getTotalChange();
                } else {
                    stockInfo = widgetStock.getDailyPercent();
                    stockInfoExtra = widgetStock.getDailyChange();
                    stockInfoExtra2 = widgetStock.getTotalPercent();
                    stockInfoExtra3 = widgetStock.getTotalChange();
                }
                break;

            case VIEW_PORTFOLIO_CHANGE:
                stockInfo = widgetStock.getTotalChange();
                stockInfoExtra = widgetStock.getTotalPercent();
                break;

            case VIEW_PORTFOLIO_PERCENT_AER:
                if (!widget.isVisual()) {
                    stockInfo = widgetStock.getTotalPercentAer();
                    stockInfoExtra = widgetStock.getTotalChangeAer();
                } else {
                    stockInfo = widgetStock.getDailyPercent();
                    stockInfoExtra = widgetStock.getDailyChange();
                    stockInfoExtra2 = widgetStock.getTotalPercentAer();
                    stockInfoExtra3 = widgetStock.getTotalChangeAer();
                }
                break;

            case VIEW_PL_DAILY_PERCENT:
                plView = true;
                plChange = true;
                priceColumn = widgetStock.getPlHolding();
                stockInfo = widgetStock.getDailyPercent();
                stockInfoExtra = widgetStock.getPlDailyChange();
                break;

            case VIEW_PL_DAILY_CHANGE:
                plView = true;
                plChange = true;
                if(!widget.isVisual()) {
                    priceColumn = widgetStock.getPlHolding();
                    stockInfo = widgetStock.getPlDailyChange();
                } else {
                    stockInfo = widgetStock.getDailyChange();
                    stockInfoExtra = widgetStock.getDailyPercent();
                    stockInfoExtra2 = widgetStock.getTotalPercent();
                    stockInfoExtra3 = widgetStock.getTotalChange();
                }
                break;

            case VIEW_PL_PERCENT:
                plView = true;
                plChange = true;
                priceColumn = widgetStock.getPlHolding();
                stockInfo = widgetStock.getTotalPercent();
                stockInfoExtra = widgetStock.getPlTotalChange();
                break;

            case VIEW_PL_CHANGE:
                plView = true;
                plChange = true;
                priceColumn = widgetStock.getPlHolding();
                stockInfo = widgetStock.getPlTotalChange();
                stockInfoExtra = widgetStock.getTotalPercent();
                break;

            case VIEW_PL_PERCENT_AER:
                plView = true;
                plChange = true;
                if (!widget.isVisual()) {
                    priceColumn = widgetStock.getPlHolding();
                    stockInfo = widgetStock.getTotalPercentAer();
                    stockInfoExtra = widgetStock.getPlTotalChangeAer();
                } else {
                    stockInfo = widgetStock.getDailyPercent();
                    stockInfoExtra = widgetStock.getDailyChange();
                    stockInfoExtra2 = widgetStock.getTotalPercentAer();
                    stockInfoExtra3 = widgetStock.getPlTotalChangeAer();
                }
                break;
        }


        // Set the price column colour if we have hit an alert
        // (this is only relevant for non-profit and loss views)
        if (widgetStock.getLimitHighTriggered() && !plView) {
            widgetRow.setPriceColor(WidgetColors.HIGH_ALERT);
        }
        if (widgetStock.getLimitLowTriggered() && !plView) {
            widgetRow.setPriceColor(WidgetColors.LOW_ALERT);
        }

        // Set the price column to the holding value and colour
        // the column blue if we have no holdings
        if (plView && priceColumn == null && !widget.isVisual()) {
            widgetRow.setPriceColor(WidgetColors.NA);
        }

        if (widget.isVisual() && stockInfoExtra2 == null) {
            stockInfoExtra2 = widgetRow.getStockInfo();
            widgetRow.setStockInfoExtra2Color(WidgetColors.NA);
        }

        if (widget.isVisual() && stockInfoExtra3 == null) {
            stockInfoExtra3 = widgetRow.getStockInfoExtra();
            widgetRow.setStockInfoExtra3Color(WidgetColors.NA);
        }


        // Add currency symbol if we have a holding
        if (priceColumn != null) {
            widgetRow.setPrice(CurrencyTools.addCurrencyToSymbol(priceColumn, symbol));
        }

        // Set the value and colour for the change values
        if (!widget.isNarrow() || widget.isVisual()) {
            if (stockInfoExtra != null) {
                if (plChange) {
                    widgetRow.setStockInfoExtra(CurrencyTools.addCurrencyToSymbol(stockInfoExtra, symbol));
                } else {
                    widgetRow.setStockInfoExtra(stockInfoExtra);
                }
                widgetRow.setStockInfoExtraColor(getColourForChange(stockInfoExtra));
            }
            if (stockInfo != null) {
                widgetRow.setStockInfo(stockInfo);
                widgetRow.setStockInfoColor(getColourForChange(stockInfo));
            }
            // Setup extra entries for the panels in Visual Stockboard
            if (widget.isVisual()) {
                if (stockInfoExtra2 != null) {
                    widgetRow.setStockInfoExtra2(stockInfoExtra2);
                    widgetRow.setStockInfoExtra2Color(getColourForChange(stockInfoExtra2));
                }
                if (stockInfoExtra3 != null) {
                    if (plChange) {
                        widgetRow.setStockInfoExtra3(CurrencyTools.addCurrencyToSymbol(stockInfoExtra3, symbol));
                    } else {
                        widgetRow.setStockInfoExtra3(stockInfoExtra3);
                    }
                    widgetRow.setStockInfoExtra3Color(getColourForChange(stockInfoExtra3));
                }


                // Set Background colour for each Panel of Viusal Stockboard
                // use percentage or numeric change depending on widget settings
                if (this.widget.getVsColorCalculation().equals("percentage")) {
                    widgetRow.setVisualColor(getColourForPanelPercent(stockInfoExtra2));
                } else {
                    widgetRow.setVisualColor(getColourForPanelNumeric(stockInfoExtra3));
                }

            }
        } else {
            if (stockInfo != null) {
                if (plChange) {
                    widgetRow.setStockInfo(CurrencyTools.addCurrencyToSymbol(stockInfo, symbol));
                } else {
                    widgetRow.setStockInfo(stockInfo);
                }
                widgetRow.setStockInfoColor(getColourForChange(stockInfo));
            }
        }
        return widgetRow;
    }

    private String getColourForPanelNumeric(String value) {
        final double MAX_VALUE = 50;
        final double MIN_VALUE = 0.1;

        String green = "04BF3C";
        String red = "D23641";
        String neutral = "#80D5D5D5";
        String colour;

        double parsedValue = NumberTools.parseDouble(value, 0d);

        // Calculate colour transparency
        if(parsedValue > MAX_VALUE)
            parsedValue = MAX_VALUE;
        else if (parsedValue < -MAX_VALUE)
            parsedValue = -MAX_VALUE;

        double normDouble = (Math.abs(parsedValue) - MIN_VALUE)/(MAX_VALUE - MIN_VALUE);

        final int MIN = 64;
        final int MAX = 230;
        int normInt =  (int) (normDouble*(MAX-MIN) + MIN);

        String hex = Integer.toHexString(normInt);

        // Set colour green (positive), red (negative) or grey (neutral)
        if (parsedValue > 0)
            colour = "#" + hex + green;
        else if (parsedValue < 0)
            colour = "#" + hex + red;
        else
            colour = neutral;
        return colour;
    }

    private String getColourForPanelPercent(String value){
        final double MAX_VALUE = 10;
        final double MIN_VALUE = 0.1;

        String green = "04BF3C";
        String red = "D23641";
        String neutral = "#80D5D5D5";
        String colour;

        double parsedValue = NumberTools.parseDouble(value, 0d);

        // Calculate colour transparency
        if(parsedValue > MAX_VALUE)
            parsedValue = MAX_VALUE;
        else if (parsedValue < -MAX_VALUE)
            parsedValue = -MAX_VALUE;

        double normDouble = (Math.abs(parsedValue) - MIN_VALUE)/(MAX_VALUE - MIN_VALUE);

        final int MIN = 64;
        final int MAX = 230;
        int normInt =  (int) (normDouble*(MAX-MIN) + MIN);

        String hex = Integer.toHexString(normInt);

        // Set colour green, red or grey
        if (parsedValue > 0)
            colour = "#" + hex + green;
        else if (parsedValue < 0)
            colour = "#" + hex + red;
        else
            colour = neutral;

         return colour;
    }


    private int getColourForChange(String value) {
        double parsedValue = NumberTools.parseDouble(value, 0d);
        int colour;
        // Set text colour for Visual Stockboard to default
        if (widget.isVisual()) {
            colour = WidgetColors.SAME;

        // Change colour for default view
        } else {
            if (parsedValue < 0) {
                colour = WidgetColors.LOSS;
            } else if (parsedValue == 0) {
                colour = WidgetColors.SAME;
            } else {
                colour = WidgetColors.GAIN;
            }
        }
        return colour;
    }

    public void clear() {
        int columnCount = (!widget.isNarrow()) ? 6 : 4;
        for (int i = 1; i < this.widget.getSymbolCount() + 1; i++) {
            for (int j = 1; j < columnCount; j++) {
                this.setStockRowItemText(i, j, "");
            }
        }
    }

    // Set visibility for unused Stocks
    // Makes every Stock invisible, then makes only used stocks visible again
    private void hideUnusedStocks(RemoteViews views, int count) {
        for (int i = 0; i <= MAX_STOCKS; i++) {
            int viewId;
            if (!widget.isVisual()) {
                //Enable rows for non-visual view
                viewId = ReflectionTools.getFieldId("line" + i);
                if (viewId > 0) {
                    views.setViewVisibility(ReflectionTools.getFieldId("line" + i), View.GONE);
                }
                // Set used rows visible
                for (int j = 1; j < count + 1; j++) {
                    views.setViewVisibility(ReflectionTools.getFieldId("line" + j), View.VISIBLE);
                }
            } else {
                //Enable panels for visual view
                viewId = ReflectionTools.getFieldId("Panel" + i);
                if (viewId > 0) {
                    views.setViewVisibility(ReflectionTools.getFieldId("Panel"+ i), View.INVISIBLE);
                }

                // Find used Symbols
                int usedSymbols = 0;
                for (String s : symbols) {
                    if (!s.equals(""))
                        usedSymbols++;
                }

                // Set used Panels visible
                for (int j = 1; j <= usedSymbols; j++) {
                    views.setViewVisibility(ReflectionTools.getFieldId("Panel" + j), View.VISIBLE);
                }
            }
        }
    }

    public RemoteViews getRemoteViews() {
        return remoteViews;
    }

    public int getNextView(UpdateType updateMode) {
        int currentView = this.widget.getPreviousView();
        if (updateMode == UpdateType.VIEW_CHANGE) {
            currentView += 1;
            currentView = currentView % 10;
        }

        // Skip views as relevant
        int count = 0;
        while (!this.getEnabledViews().get(ViewType.values()[currentView])) {
            count += 1;
            currentView += 1;
            currentView = currentView % 10;
            // Percent change as default view if none selected
            if (count > 10) {
                currentView = 0;
                break;
            }
        }
        widget.setView(currentView);
        return currentView;
    }

    public void setStockRowItemText(int row, int col, Object text) {
            try {
                this.remoteViews.setTextViewText(
                        ReflectionTools.getFieldId("text" + row + col),
                        !text.equals("") ? applyFormatting((String) text) : "");
            }catch (Exception e){}
    }

    public void setStockRowItemColor(int row, int col, int color) {
        this.remoteViews.setTextColor(ReflectionTools.getFieldId("text" + row + col), color);
    }

    public void applyPendingChanges() {
        int widgetDisplay = this.getNextView(this.updateMode);
        this.clear();

        int lineNo = 0;
        for (String symbol : this.symbols) {
            if (symbol.equals("")) {
                continue;
            }

            // Get the info for this quote
            lineNo++;
            WidgetRow rowInfo = getRowInfo(symbol, ViewType.values()[widgetDisplay]);

            // Values
            if (!widget.isVisual()) {
                setStockRowItemText(lineNo, 1, rowInfo.getSymbol());
                setStockRowItemText(lineNo, 2, rowInfo.getPrice());
                if (widget.isNarrow()) {
                    setStockRowItemText(lineNo, 3, rowInfo.getStockInfo());
                } else {
                    setStockRowItemText(lineNo, 3, rowInfo.getVolume());
                    setStockRowItemText(lineNo, 4, rowInfo.getStockInfoExtra());
                    setStockRowItemText(lineNo, 5, rowInfo.getStockInfo());
                }
            } else {
                setStockRowItemText(lineNo, 1, rowInfo.getSymbol());
                setStockRowItemText(lineNo, 2, rowInfo.getPrice());
                setStockRowItemText(lineNo, 3, rowInfo.getStockInfoExtra2());
                setStockRowItemText(lineNo, 4, rowInfo.getStockInfoExtra3());
                setStockRowItemText(lineNo, 5, rowInfo.getStockInfo());
                setStockRowItemText(lineNo, 6, rowInfo.getStockInfoExtra());
            }

            // Colours
            if (!this.widget.isVisual()) {
                setStockRowItemColor(lineNo, 1, rowInfo.getSymbolDisplayColor());
                if (!this.widget.getColorsOnPrices()) {
                    setStockRowItemColor(lineNo, 2, rowInfo.getPriceColor());

                    if (widget.isNarrow()) {
                        setStockRowItemColor(lineNo, 3, rowInfo.getStockInfoColor());
                    } else {
                        setStockRowItemColor(lineNo, 3, rowInfo.getVolumeColor());
                        setStockRowItemColor(lineNo, 4, rowInfo.getStockInfoExtraColor());
                        setStockRowItemColor(lineNo, 5, rowInfo.getStockInfoColor());
                    }
                } else {
                    setStockRowItemColor(lineNo, 2, rowInfo.getStockInfoColor());

                    if (widget.isNarrow()) {
                        setStockRowItemColor(lineNo, 3, rowInfo.getPriceColor());
                    } else {
                        setStockRowItemColor(lineNo, 3, rowInfo.getVolumeColor());
                        setStockRowItemColor(lineNo, 4, rowInfo.getPriceColor());
                        setStockRowItemColor(lineNo, 5, rowInfo.getPriceColor());
                    }
                }
            } else {
                setStockRowItemColor(lineNo, 1, rowInfo.getSymbolDisplayColor());
                setStockRowItemColor(lineNo, 2, rowInfo.getPriceColor());
                setStockRowItemColor(lineNo, 3, rowInfo.getStockInfoExtra2Color());
                setStockRowItemColor(lineNo, 4, rowInfo.getStockInfoExtra3Color());
                setStockRowItemColor(lineNo, 5, rowInfo.getStockInfoColor());
                setStockRowItemColor(lineNo, 6, rowInfo.getStockInfoExtraColor());

                int panelInt = ReflectionTools.getFieldId("Panel" + lineNo);
                remoteViews.setInt(panelInt, "setBackgroundColor", Color.parseColor(rowInfo.getVisualColor()));
            }
        }

        // Set footer display
       if( widget.getStorage().getBoolean("visual_stockboard",false)) {

            if (this.widget.getVsFooterVisibility().equals("invisible"))
                remoteViews.setViewVisibility(R.id.text_footer, View.INVISIBLE);
            else {
                    remoteViews.setViewVisibility(R.id.text_footer, View.VISIBLE);

                    // Set time stamp
                    int footerColor = this.getFooterColor();
                    remoteViews.setTextViewText(R.id.text5, applyFormatting(this.getVsTimeStamp()));
                    remoteViews.setTextColor(R.id.text5, footerColor);

                    // Set the view label
                    remoteViews.setTextViewText(R.id.text6, applyFormatting(this.getLabel(widgetDisplay)));
                    remoteViews.setTextColor(R.id.text6, footerColor);

            }

            }else{

                switch (this.widget.getFooterVisibility()) {
                    case "remove":
                        remoteViews.setViewVisibility(R.id.text_footer, View.GONE);
                        break;

                    case "invisible":
                        remoteViews.setViewVisibility(R.id.text_footer, View.INVISIBLE);
                        break;

                    default:
                        remoteViews.setViewVisibility(R.id.text_footer, View.VISIBLE);

                        // Set time stamp
                        int footerColor = this.getFooterColor();
                        remoteViews.setTextViewText(R.id.text5, applyFormatting(this.getTimeStamp()));
                        remoteViews.setTextColor(R.id.text5, footerColor);

                        // Set the view label
                        remoteViews.setTextViewText(R.id.text6, applyFormatting(this.getLabel(widgetDisplay)));
                        remoteViews.setTextColor(R.id.text6, footerColor);
                        break;
                }
           }
    }


    public int getFooterColor() {
        String colorType;
        if( widget.getStorage().getBoolean("visual_stockboard",false))
            colorType = this.widget.getVsFooterColor();
        else
            colorType = this.widget.getFooterColor();

        int color = Color.parseColor("#555555");
        if (colorType.equals("light")) {
            color = Color.GRAY;
        } else if (colorType.equals("yellow")) {
            color = Color.parseColor("#cccc77");
        }

        return color;
    }

    public String getLabel(int widgetDisplay) {
        // Set the widget view text in the footer
        String label = "";
        if (widget.isNarrow() && !widget.isVisual()) {
            switch (ViewType.values()[widgetDisplay]) {
                case VIEW_DAILY_PERCENT:
                    label = "D%";
                    break;

                case VIEW_DAILY_CHANGE:
                    label = "DA";
                    break;

                case VIEW_PORTFOLIO_PERCENT:
                    label = "PF T%";
                    break;

                case VIEW_PORTFOLIO_CHANGE:
                    label = "PF TA";
                    break;

                case VIEW_PORTFOLIO_PERCENT_AER:
                    label = "PF AER";
                    break;

                case VIEW_PL_DAILY_PERCENT:
                    label = "P/L D%";
                    break;

                case VIEW_PL_DAILY_CHANGE:
                    label = "P/L DA";
                    break;

                case VIEW_PL_PERCENT:
                    label = "P/L T%";
                    break;

                case VIEW_PL_CHANGE:
                    label = "P/L TA";
                    break;

                case VIEW_PL_PERCENT_AER:
                    label = "P/L AER";
                    break;
            }
        } else {
            switch (ViewType.values()[widgetDisplay]) {
                case VIEW_DAILY_PERCENT:
                    label = "D%";
                    break;

                case VIEW_DAILY_CHANGE:
                    label = "";
                    break;

                case VIEW_PORTFOLIO_PERCENT:
                    label = "PF T";
                    break;

                case VIEW_PORTFOLIO_CHANGE:
                    label = "PF T";
                    break;

                case VIEW_PORTFOLIO_PERCENT_AER:
                    label = "PF AER";
                    break;

                case VIEW_PL_DAILY_PERCENT:
                    label = "P/L D";
                    break;

                case VIEW_PL_DAILY_CHANGE:
                    label = "P/L D";
                    break;

                case VIEW_PL_PERCENT:
                    label = "P/L T";
                    break;

                case VIEW_PL_CHANGE:
                    label = "P/L T";
                    break;

                case VIEW_PL_PERCENT_AER:
                    label = "P/L AER";
                    break;
            }
        }

        return label;
    }

    public String getVsTimeStamp() {
        String timeStamp = this.quotesTimeStamp;
        if (!this.widget.showVsShortTime()) {
            String date = new SimpleDateFormat("dd MMM").format(new Date()).toUpperCase();

            // Check if we should use yesterdays date or today's time
            String[] parts = timeStamp.split(" ");
            String fullDate = parts[0] + " " + parts[1];
            if (fullDate.equals(date)) {
                timeStamp = parts[2];
            } else {
                timeStamp = fullDate;
            }
        }

        return timeStamp;
    }

    public String getTimeStamp() {
        String timeStamp = this.quotesTimeStamp;
        if (!this.widget.showShortTime()) {
            String date = new SimpleDateFormat("dd MMM").format(new Date()).toUpperCase();

            // Check if we should use yesterdays date or today's time
            String[] parts = timeStamp.split(" ");
            String fullDate = parts[0] + " " + parts[1];
            if (fullDate.equals(date)) {
                timeStamp = parts[2];
            } else {
                timeStamp = fullDate;
            }
        }

        return timeStamp;
    }

    public boolean canChangeView() {
        HashMap<ViewType, Boolean> enabledViews = this.getEnabledViews();
        boolean hasMultipleDefaultViews = enabledViews.get(ViewType.VIEW_DAILY_PERCENT)
                && enabledViews.get(ViewType.VIEW_DAILY_CHANGE);

        return !(this.updateMode == UpdateType.VIEW_CHANGE
                && !this.hasPortfolioData
                && !hasMultipleDefaultViews);
    }

    public boolean hasPendingChanges() {
        return (!this.quotes.isEmpty() || this.canChangeView());
    }
}