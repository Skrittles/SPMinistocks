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

import android.graphics.Color;

import nitezh.ministock.domain.Widget;

public class WidgetRow {
    Widget widget;

    String symbol;
    int symbolDisplayColor;
    String price;
    int priceColor;
    String volume;
    int volumeColor;
    String stockInfo;
    int stockInfoColor;
    String stockInfoExtra;
    int stockInfoExtraColor;
    String stockInfoExtra2;
    int stockInfoExtra2Color;
    String stockInfoExtra3;
    int stockInfoExtra3Color;
    String visualColor;

    private boolean hasNoData;

    public WidgetRow(Widget widget) {
        this.widget = widget;

        this.symbol = "";
        this.symbolDisplayColor = Color.WHITE;
        this.price = "";
        this.priceColor = Color.WHITE;
        this.volume = "";
        this.volumeColor = Color.WHITE;
        this.stockInfo = "";
        this.stockInfoColor = Color.WHITE;
        this.stockInfoExtra = "";
        this.stockInfoExtraColor = Color.WHITE;
        this.stockInfoExtra2 = "";
        this.stockInfoExtra2Color = Color.WHITE;
        this.stockInfoExtra3 = "";
        this.stockInfoExtra3Color = Color.WHITE;
        this.visualColor = "#80D5D5D5";

        this.hasNoData = false;
    }

    public int getSymbolDisplayColor() {
        return symbolDisplayColor;
    }

    public void setSymbolDisplayColor(int symbolDisplayColor) {
        this.symbolDisplayColor = symbolDisplayColor;
    }

    public int getPriceColor() {
        return priceColor;
    }

    public void setPriceColor(int priceColor) {
        this.priceColor = priceColor;
    }

    public int getVolumeColor() {
        return volumeColor;
    }

    public void setVolumeColor(int volumeColor) {
        this.volumeColor = volumeColor;
    }

    public int getStockInfoColor() {
        return stockInfoColor;
    }

    public void setStockInfoColor(int stockInfoColor) {
        this.stockInfoColor = stockInfoColor;
    }

    public int getStockInfoExtraColor() {
        return stockInfoExtraColor;
    }

    public void setStockInfoExtraColor(int stockInfoExtraColor) {
        this.stockInfoExtraColor = stockInfoExtraColor;
    }

    public boolean isHasNoData() {
        return hasNoData;
    }

    public void setHasNoData(boolean hasNoData) {
        this.hasNoData = hasNoData;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        if (this.widget.getHideSuffix()) {
            int dotIndex = symbol.indexOf(".");
            if (dotIndex > -1) {
                symbol = symbol.substring(0, dotIndex);
            }
        }
        this.symbol = symbol;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getStockInfo() {
        return stockInfo;
    }

    public void setStockInfo(String stockInfo) {
        this.stockInfo = stockInfo;
    }

    public String getStockInfoExtra() {
        return stockInfoExtra;
    }

    public void setStockInfoExtra(String stockInfoExtra) {
        this.stockInfoExtra = stockInfoExtra;
    }

    public  void setStockInfoExtra2(String stockInfoExtra2) {
        this.stockInfoExtra2 = stockInfoExtra2;
    }

    public String getStockInfoExtra2(){return this.stockInfoExtra2;}


    public int getStockInfoExtra2Color() {
        return stockInfoExtra2Color;
    }

    public void setStockInfoExtra2Color(int stockInfoExtra2Color) {
        this.stockInfoExtra2Color = stockInfoExtra2Color;
    }

    public String getStockInfoExtra3() {
        return stockInfoExtra3;
    }

    public void setStockInfoExtra3(String stockInfoExtra3) {
        this.stockInfoExtra3 = stockInfoExtra3;
    }

    public int getStockInfoExtra3Color() {
        return stockInfoExtra3Color;
    }

    public void setStockInfoExtra3Color(int stockInfoExtra3Color) {
        this.stockInfoExtra3Color = stockInfoExtra3Color;
    }

    public String getVisualColor() {
        return visualColor;
    }

    public void setVisualColor(String visualColor) {
        this.visualColor = visualColor;
    }

}
