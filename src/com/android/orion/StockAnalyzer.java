package com.android.orion;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.regression.SimpleRegression;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.android.orion.curve.BezierCurve;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockMatch;
import com.android.orion.indicator.MACD;
import com.android.orion.utility.StopWatch;
import com.android.orion.utility.Utility;

public class StockAnalyzer extends StockManager {

	public StockAnalyzer(Context context) {
		super(context);
	}

	void analyze(Stock stock, String period, ArrayList<StockData> stockDataList) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		if ((stock == null) || (stockDataList == null)) {
			return;
		}

		try {
			loadStockDataList(stock, period, stockDataList);
			if (stockDataList.size() < Constants.STOCK_VERTEX_TYPING_SIZE) {
				return;
			}

			setMACD(stock, period, stockDataList);
			if (Utility
					.getSettingBoolean(mContext, Constants.SETTING_KEY_MATCH)) {
				analyzeStockMatch(stock, period, stockDataList);
			}
			analyzeStockData(stock, period, stockDataList);
			updateDatabase(stock, period, stockDataList);
			writeMessage();
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Utility.Log("analyze:" + stock.getName() + " " + period + " "
				+ stopWatch.getInterval() + "s");
	}

	private void setMACD(Stock stock, String period,
			ArrayList<StockData> stockDataList) {
		int size = 0;
		int grade = 0;
		int beginIndex = 0;

		double t = 0;
		double average = 0;
		double velocity = 0;
		double acceleration = 0;

		double average5 = 0;
		double average10 = 0;
		double dif = 0;
		double dea = 0;
		double histogram = 0;

		MACD macd = new MACD();
		BezierCurve bezierCurve = new BezierCurve();

		size = stockDataList.size();

		if (size < Constants.STOCK_VERTEX_TYPING_SIZE) {
			return;
		}

		macd.mPriceList.clear();

		for (int i = 0; i < size; i++) {
			macd.mPriceList.add(stockDataList.get(i).getClose());
		}

		macd.calculate();

		grade = size - 1;

		if (size > Constants.BENZIER_CURVE_GRADE_MAX) {
			grade = Constants.BENZIER_CURVE_GRADE_MAX;
		}

		bezierCurve.init(grade);

		beginIndex = size - grade - 1;

		for (int i = 0; i < size; i++) {
			average5 = macd.mEMAAverage5List.get(i);
			average10 = macd.mEMAAverage10List.get(i);
			dif = macd.mDIFList.get(i);
			dea = macd.mDEAList.get(i);
			histogram = macd.mHistogramList.get(i);

			stockDataList.get(i).setAverage5(average5);
			stockDataList.get(i).setAverage10(average10);
			stockDataList.get(i).setDIF(dif);
			stockDataList.get(i).setDEA(dea);
			stockDataList.get(i).setHistogram(histogram);
			stockDataList.get(i).setAverage(average);
			stockDataList.get(i).setVelocity(velocity);
			stockDataList.get(i).setAcceleration(acceleration);

			if (i >= beginIndex) {
				bezierCurve.addControlData(i - beginIndex, histogram);
			}
		}

		for (int i = beginIndex; i < size; i++) {
			t = 1.0 * (i - beginIndex) / (size - 1 - beginIndex);
			average = bezierCurve.calculate(t);
			velocity = 10 * (average - stockDataList.get(i - 1).getAverage());
			acceleration = 2 * (velocity - stockDataList.get(i - 1)
					.getVelocity());

			if (i == beginIndex) {
				velocity = 0;
				acceleration = 0;
			} else if (i == beginIndex + 1) {
				acceleration = 0;
			}

			stockDataList.get(i).setAverage(average);
			stockDataList.get(i).setVelocity(velocity);
			stockDataList.get(i).setAcceleration(acceleration);
		}
	}

	private void analyzeStockMatch(Stock stock, String period,
			ArrayList<StockData> stockDataList) {
		double x = 0;
		double y = 0;
		double slope = 0;
		double mean = 0;
		double std = 0;
		double delta = 0;

		Stock stock_X;
		Stock stock_Y;
		ArrayList<StockMatch> stockMatchList;
		ArrayList<StockData> stockDataList_X = null;
		ArrayList<StockData> stockDataList_Y = null;

		SimpleRegression simpleRegression = new SimpleRegression();
		DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();

		simpleRegression.clear();
		descriptiveStatistics.clear();

		if ((stock == null) || (stockDataList == null)) {
			return;
		}

		stockMatchList = new ArrayList<StockMatch>();
		mStockDatabaseManager.getStockMatchList(stock, stockMatchList);
		if (stockMatchList.size() == 0) {
			return;
		}

		stock_X = Stock.obtain();
		stock_Y = Stock.obtain();

		for (StockMatch stockMatch : stockMatchList) {
			if (stock.getSE().equals(stockMatch.getSE_X())
					&& stock.getCode().equals(stockMatch.getCode_X())) {
				stock_X.set(stock);
				stockDataList_X = stockDataList;

				stock_Y.setSE(stockMatch.getSE_Y());
				stock_Y.setCode(stockMatch.getCode_Y());
				mStockDatabaseManager.getStock(stock_Y);
				stockDataList_Y = new ArrayList<StockData>();
				loadStockDataList(stock_Y, period, stockDataList_Y);
			} else if (stock.getSE().equals(stockMatch.getSE_Y())
					&& stock.getCode().equals(stockMatch.getCode_Y())) {
				stock_X.setSE(stockMatch.getSE_X());
				stock_X.setCode(stockMatch.getCode_X());
				mStockDatabaseManager.getStock(stock_X);
				stockDataList_X = new ArrayList<StockData>();
				loadStockDataList(stock_X, period, stockDataList_X);

				stock_Y.set(stock);
				stockDataList_Y = stockDataList;
			}

			if ((stockDataList_X == null) || (stockDataList_Y == null)
					|| (stockDataList_X.size() != stockDataList_Y.size())) {
				return;
			}

			if (stockDataList_X.size() < Constants.STOCK_VERTEX_TYPING_SIZE) {
				return;
			}

			simpleRegression.clear();

			for (int i = 0; i < stockDataList_X.size(); i++) {
				x = stockDataList_X.get(i).getClose();
				y = stockDataList_Y.get(i).getClose();

				simpleRegression.addData(x, y);
			}

			slope = simpleRegression.getSlope();

			descriptiveStatistics.clear();

			for (int i = 0; i < stockDataList_X.size(); i++) {
				x = stockDataList_X.get(i).getClose();
				y = stockDataList_Y.get(i).getClose();

				delta = y - slope * x;

				descriptiveStatistics.addValue(delta);
			}

			mean = descriptiveStatistics.getMean();
			std = descriptiveStatistics.getStandardDeviation();

			int i = stockDataList_X.size() - 1;
			x = stockDataList_X.get(i).getClose();
			y = stockDataList_Y.get(i).getClose();

			delta = y - slope * x;

			if (std == 0) {
				delta = 0;
			} else {
				delta = (delta - mean) / std;
			}

			delta = Utility.Round(delta, Constants.DOUBLE_FIXED_DECIMAL - 1);

			stockMatch.setValue(period, delta);

			mStockDatabaseManager.updateStockMatch(stockMatch);
		}
	}

	private void analyzeStockData(Stock stock, String period,
			ArrayList<StockData> stockDataList) {
		VertexAnalyzer vertexAnalyzer = new VertexAnalyzer();

		ArrayList<StockData> drawVertexList = new ArrayList<StockData>();
		ArrayList<StockData> drawDataList = new ArrayList<StockData>();
		ArrayList<StockData> strokeVertexList = new ArrayList<StockData>();
		ArrayList<StockData> strokeDataList = new ArrayList<StockData>();
		ArrayList<StockData> segmentVertexList = new ArrayList<StockData>();
		ArrayList<StockData> segmentDataList = new ArrayList<StockData>();
		ArrayList<StockData> overlapList = new ArrayList<StockData>();

		vertexAnalyzer.analyzeVertex(stockDataList, drawVertexList);
		vertexAnalyzer.vertexListToDataList(stockDataList, drawVertexList,
				drawDataList, false);
		// __TEST_CASE__
		// vertexAnalyzer.testShow(stockDataList, drawDataList);
		// __TEST_CASE__
		vertexAnalyzer.analyzeLine(stockDataList, drawDataList,
				strokeVertexList, Constants.STOCK_VERTEX_TOP_STROKE,
				Constants.STOCK_VERTEX_BOTTOM_STROKE);
		vertexAnalyzer.vertexListToDataList(stockDataList, strokeVertexList,
				strokeDataList, false);
		// __TEST_CASE__
		// vertexAnalyzer.testShow(stockDataList, strokeDataList);
		// __TEST_CASE__
		vertexAnalyzer.analyzeLine(stockDataList, strokeDataList,
				segmentVertexList, Constants.STOCK_VERTEX_TOP_SEGMENT,
				Constants.STOCK_VERTEX_BOTTOM_SEGMENT);
		vertexAnalyzer.vertexListToDataList(stockDataList, segmentVertexList,
				segmentDataList, true);

		vertexAnalyzer.analyzeOverlap(stockDataList, segmentDataList,
				overlapList);

		vertexAnalyzer.analyzeAction(stockDataList, segmentDataList,
				overlapList);

		setAction(stock, period, stockDataList, segmentDataList);
	}

	private void setAction(Stock stock, String period,
			ArrayList<StockData> stockDataList,
			ArrayList<StockData> segmentDataList) {
		String action = Constants.STOCK_ACTION_NONE;
		String direction = "";
		StockData segmentData = null;
		StockData endStockData = null;

		if ((stockDataList == null) || (segmentDataList == null)) {
			Utility.Log("setAction return" + " stockDataList = "
					+ stockDataList + " segmentDataList" + segmentDataList);
			return;
		}

		segmentData = segmentDataList.get(segmentDataList.size() - 1);
		endStockData = stockDataList.get(segmentData.getIndexEnd());

		action = endStockData.getAction();

		if (endStockData.getAcceleration() > 0) {
			direction += Constants.STOCK_ACTION_ADD;
		} else if (endStockData.getAcceleration() <= 0) {
			direction += Constants.STOCK_ACTION_MINUS;
		}

		direction += " ";

		if (endStockData.getVelocity() > 0) {
			direction += Constants.STOCK_ACTION_ADD;
		} else if (endStockData.getVelocity() <= 0) {
			direction += Constants.STOCK_ACTION_MINUS;
		}

		direction += " ";

		if (endStockData.getAverage() > 0) {
			direction += Constants.STOCK_ACTION_ADD;
		} else if (endStockData.getAverage() <= 0) {
			direction += Constants.STOCK_ACTION_MINUS;
		}

		action = direction;// + action;

		stock.setAction(period, action);
	}

	private void updateDatabase(Stock stock, String period,
			ArrayList<StockData> stockDataList) {
		ContentValues contentValues[] = new ContentValues[stockDataList.size()];

		if (mStockDatabaseManager == null) {
			Utility.Log("updateDatabase return " + " mStockDatabaseManager = "
					+ mStockDatabaseManager);
			return;
		}

		try {
			mStockDatabaseManager.deleteStockData(stock.getId(), period);

			for (int i = 0; i < stockDataList.size(); i++) {
				StockData stockData = stockDataList.get(i);
				contentValues[i] = stockData.getContentValues();
			}

			mStockDatabaseManager.bulkInsertStockData(contentValues);
			mStockDatabaseManager.updateStock(stock,
					stock.getContentValuesAnalyze(period));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	void writeMessage() {
		boolean bFound = false;
		List<Stock> stockList = null;

		String sortOrder = DatabaseContract.COLUMN_NET + " " + "DESC";

		String idString = "";
		String addressString = "106589996700";
		String headSting = "更新时间:" + Utility.getCurrentDateTimeString() + "\n";
		String bodySting = "";
		String footSting = "感谢您的使用，中国移动。";

		Cursor cursor = null;
		ContentValues contentValues = null;
		Uri uri = Uri.parse("content://sms/inbox");

		stockList = loadStockList(
				selectStock(Constants.STOCK_FLAG_MARK_FAVORITE), null,
				sortOrder);
		if ((stockList == null) || (stockList.size() == 0)) {
			return;
		}

		for (Stock stock : stockList) {
			bodySting += getBodyString(stock);
		}

		// Utility.Log(bodySting);

		contentValues = new ContentValues();
		contentValues.put("address", addressString);
		contentValues.put("body", headSting + bodySting + footSting);

		try {
			cursor = mContentResolver.query(uri, null, null, null, null);
			if (cursor == null) {
				mContentResolver.insert(uri, contentValues);
			} else {
				while (cursor.moveToNext()) {
					if ((cursor.getString(cursor.getColumnIndex("address"))
							.equals(addressString))) {
						idString = "_id="
								+ cursor.getString(cursor.getColumnIndex("_id"));
						bFound = true;
					}
				}

				if (bFound) {
					mContentResolver.update(uri, contentValues, idString, null);
				} else {
					mContentResolver.insert(uri, contentValues);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}
	}

	//
	// private void writeCallLog(Stock stock, String period, StockData
	// stockData) {
	// boolean bFound = false;
	// int nCallLogType;
	// long nMilliSeconds = 0;
	//
	// String idString = "";
	// String numberString = "10086" + stock.getCode();
	//
	// Cursor cursor = null;
	// ContentValues contentValues = null;
	// Uri uri = CallLog.Calls.CONTENT_URI;
	//
	// if (stockData == null) {
	// return;
	// }
	//
	// if (stock.getAction(period).contains(Constants.STOCK_ACTION_BUY)) {
	// nCallLogType = CallLog.Calls.MISSED_TYPE;
	// } else if (stock.getAction(period)
	// .contains(Constants.STOCK_ACTION_SELL)) {
	// nCallLogType = CallLog.Calls.INCOMING_TYPE;
	// } else {
	// return;
	// }
	//
	// numberString += getPeriodMinutes(stockData.getPeriod());
	// nMilliSeconds = Utility.getMilliSeconds(stockData.getDate(),
	// stockData.getTime());
	//
	// contentValues = new ContentValues();
	// contentValues.put(CallLog.Calls.NUMBER, numberString);
	// contentValues.put(CallLog.Calls.DATE, nMilliSeconds);
	// contentValues.put(CallLog.Calls.DURATION, 0);
	// contentValues.put(CallLog.Calls.TYPE, nCallLogType);
	// contentValues.put(CallLog.Calls.NEW, 0);
	// contentValues.put(CallLog.Calls.CACHED_NAME, "");
	// contentValues.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
	// contentValues.put(CallLog.Calls.CACHED_NUMBER_LABEL, "");
	//
	// try {
	// cursor = mContentResolver.query(uri, null, null, null, null);
	// if (cursor == null) {
	// mContentResolver.insert(uri, contentValues);
	// } else {
	// while (cursor.moveToNext()) {
	// if ((cursor.getString(cursor.getColumnIndex("number"))
	// .equals(numberString))) {
	// idString = "_id="
	// + cursor.getString(cursor.getColumnIndex("_id"));
	// bFound = true;
	// }
	// }
	//
	// if (bFound) {
	// mContentResolver.update(uri, contentValues, idString, null);
	// } else {
	// mContentResolver.insert(uri, contentValues);
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// if (cursor != null) {
	// if (!cursor.isClosed()) {
	// cursor.close();
	// }
	// }
	// }
	// }

	// private void updateNotification(Stock stock) {
	// int id = 0;
	// int defaults = 0;
	//
	// NotificationManager notificationManager = (NotificationManager) mContext
	// .getSystemService(Context.NOTIFICATION_SERVICE);
	//
	// id = (int) stock.getId();
	//
	// Intent intent = new Intent(Intent.ACTION_MAIN);
	// intent.setType("vnd.android-dir/mms-sms");
	// PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
	// intent, 0);
	//
	// NotificationCompat.Builder notification = new NotificationCompat.Builder(
	// mContext).setContentTitle("10086")
	// .setContentText(getBodyString(stock))
	// .setSmallIcon(R.drawable.ic_dialog_email).setAutoCancel(true)
	// .setLights(0xFF0000FF, 100, 300)
	// .setContentIntent(pendingIntent);
	//
	// if (Utility.getSettingBoolean(mContext,
	// Constants.SETTING_KEY_NOTIFICATION_LIGHTS)) {
	// defaults = defaults | Notification.DEFAULT_LIGHTS;
	// }
	// if (Utility.getSettingBoolean(mContext,
	// Constants.SETTING_KEY_NOTIFICATION_VIBRATE)) {
	// defaults = defaults | Notification.DEFAULT_VIBRATE;
	// }
	// if (Utility.getSettingBoolean(mContext,
	// Constants.SETTING_KEY_NOTIFICATION_SOUND)) {
	// defaults = defaults | Notification.DEFAULT_SOUND;
	// }
	// notification.setDefaults(defaults);
	//
	// notificationManager.notify(id, notification.build());
	// }

	String getBodyString(Stock stock) {
		String result = "";
		ArrayList<StockDeal> stockDealList = new ArrayList<StockDeal>();

		result += stock.getName();
		result += String.valueOf(stock.getPrice()) + " ";
		result += String.valueOf(stock.getNet()) + " ";

		for (int i = Constants.PERIODS.length - 1; i >= 0; i--) {
			String period = Constants.PERIODS[i];
			if (Utility.getSettingBoolean(mContext, period)) {
				result += stock.getAction(period) + " ";
			}
		}

		result += "\n";

		mStockDatabaseManager.getStockDealList(stock, stockDealList);

		for (StockDeal stockDeal : stockDealList) {
			if ((stockDeal.getDeal() > 0)
					&& Math.abs(stockDeal.getVolume()) > 0) {
				result += stockDeal.getDeal() + " ";
				result += stockDeal.getNet() + " ";
				result += stockDeal.getVolume() + " ";
				result += stockDeal.getProfit() + " ";
				result += "\n";
			}
		}

		return result;
	}
}
