package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.Constants;

public class StockData extends StockDatabaseTable {
	private long mStockId;
	private String mDate;
	private String mTime;
	private String mPeriod;
	private double mOpen;
	private double mHigh;
	private double mLow;
	private double mClose;
	private int mDirection;
	private int mVertex;
	private double mVertexLow;
	private double mVertexHigh;
	private int mPosition;
	private double mOverlapLow;
	private double mOverlapHigh;
	private double mAverage5;
	private double mAverage10;
	private double mDIF;
	private double mDEA;
	private double mHistogram;
	private double mSigmaHistogram;
	private int mDivergence;
	private double mTrendsEfforts;
	private double mAverage;
	private String mAction;

	private int mIndex;
	private int mIndexStart;
	private int mIndexEnd;

	private StockData next;
	private static final Object sPoolSync = new Object();
	private static StockData sPool;

	public static StockData obtain() {
		synchronized (sPoolSync) {
			if (sPool != null) {
				StockData m = sPool;
				sPool = m.next;
				m.next = null;
				return m;
			}
		}
		return new StockData();
	}

	public static StockData obtain(String period) {
		StockData stockData = obtain();

		stockData.init();
		stockData.setPeriod(period);

		return stockData;
	}

	public StockData() {
		init();
	}

	public StockData(StockData stockData) {
		set(stockData);
	}

	public StockData(Cursor cursor) {
		set(cursor);
	}

	public boolean isEmpty() {
		boolean result = false;

		if ((mStockId == 0) && TextUtils.isEmpty(mDate)
				&& TextUtils.isEmpty(mTime)) {
			result = true;
		}

		return result;
	}

	public void init() {
		super.init();

		setTableName(DatabaseContract.StockData.TABLE_NAME);

		mStockId = 0;
		mDate = "";
		mTime = "";
		mPeriod = "";
		mOpen = 0;
		mHigh = 0;
		mLow = 0;
		mClose = 0;
		mDirection = Constants.STOCK_DIRECTION_NONE;
		mVertex = Constants.STOCK_VERTEX_NONE;
		mVertexLow = 0;
		mVertexHigh = 0;
		mPosition = Constants.STOCK_POSITION_NONE;
		mOverlapLow = 0;
		mOverlapHigh = 0;
		mAverage5 = 0;
		mAverage10 = 0;
		mDIF = 0;
		mDEA = 0;
		mHistogram = 0;
		mSigmaHistogram = 0;
		mDivergence = Constants.STOCK_DIVERGENCE_NONE;
		mTrendsEfforts = 0;
		mAverage = 0;
		mAction = "";

		mIndex = 0;
		mIndexStart = 0;
		mIndexEnd = 0;
	}

	public ContentValues getContentValues() {
		ContentValues contentValues = new ContentValues();
		super.getContentValues(contentValues);
		contentValues = getContentValues(contentValues);
		return contentValues;
	}

	ContentValues getContentValues(ContentValues contentValues) {
		contentValues.put(DatabaseContract.COLUMN_STOCK_ID, mStockId);
		contentValues.put(DatabaseContract.COLUMN_DATE, mDate);
		contentValues.put(DatabaseContract.COLUMN_TIME, mTime);
		contentValues.put(DatabaseContract.COLUMN_PERIOD, mPeriod);
		contentValues.put(DatabaseContract.COLUMN_OPEN, mOpen);
		contentValues.put(DatabaseContract.COLUMN_HIGH, mHigh);
		contentValues.put(DatabaseContract.COLUMN_LOW, mLow);
		contentValues.put(DatabaseContract.COLUMN_CLOSE, mClose);
		contentValues.put(DatabaseContract.COLUMN_DIRECTION, mDirection);
		contentValues.put(DatabaseContract.COLUMN_VERTEX, mVertex);
		contentValues.put(DatabaseContract.COLUMN_VERTEX_LOW, mVertexLow);
		contentValues.put(DatabaseContract.COLUMN_VERTEX_HIGH, mVertexHigh);
		contentValues.put(DatabaseContract.COLUMN_POSITION, mPosition);
		contentValues.put(DatabaseContract.COLUMN_OVERLAP, mOverlap);
		contentValues.put(DatabaseContract.COLUMN_OVERLAP_LOW, mOverlapLow);
		contentValues.put(DatabaseContract.COLUMN_OVERLAP_HIGH, mOverlapHigh);
		contentValues.put(DatabaseContract.COLUMN_AVERAGE5, mAverage5);
		contentValues.put(DatabaseContract.COLUMN_AVERAGE10, mAverage10);
		contentValues.put(DatabaseContract.COLUMN_DIF, mDIF);
		contentValues.put(DatabaseContract.COLUMN_DEA, mDEA);
		contentValues.put(DatabaseContract.COLUMN_HISTOGRAM, mHistogram);
		contentValues.put(DatabaseContract.COLUMN_SIGMA_HISTOGRAM,
				mSigmaHistogram);
		contentValues.put(DatabaseContract.COLUMN_DIVERGENCE, mDivergence);
		contentValues.put(DatabaseContract.COLUMN_TRENDS_EFFORTS,
				mTrendsEfforts);
		contentValues.put(DatabaseContract.COLUMN_AVERAGE, mAverage);
		contentValues.put(DatabaseContract.COLUMN_VELOCITY, mVelocity);
		contentValues.put(DatabaseContract.COLUMN_ACCELERATION, mAcceleration);
		contentValues.put(DatabaseContract.COLUMN_ACTION, mAction);

		return contentValues;
	}

	public void set(StockData stockData) {
		if (stockData == null) {
			return;
		}

		init();

		super.set(stockData);

		setStockId(stockData.mStockId);
		setDate(stockData.mDate);
		setTime(stockData.mTime);
		setPeriod(stockData.mPeriod);
		setOpen(stockData.mOpen);
		setHigh(stockData.mHigh);
		setLow(stockData.mLow);
		setClose(stockData.mClose);
		setDirection(stockData.mDirection);
		setVertex(stockData.mVertex);
		setVertexLow(stockData.mVertexLow);
		setVertexHigh(stockData.mVertexHigh);
		setPosition(stockData.mPosition);
		setOverlapLow(stockData.mOverlapLow);
		setOverlapHigh(stockData.mOverlapHigh);
		setAverage5(stockData.mAverage5);
		setAverage10(stockData.mAverage10);
		setDIF(stockData.mDIF);
		setDEA(stockData.mDEA);
		setHistogram(stockData.mHistogram);
		setSigmaHistogram(stockData.mSigmaHistogram);
		setDivergence(stockData.mDivergence);
		setTrendsEfforts(stockData.mTrendsEfforts);
		setAverage(stockData.mAverage);
		setVelocity(stockData.mVelocity);
		setAcceleration(stockData.mAcceleration);
		setAction(stockData.mAction);

		setIndex(stockData.mIndex);
		setIndexStart(stockData.mIndexStart);
		setIndexEnd(stockData.mIndexEnd);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setStockID(cursor);
		setDate(cursor);
		setTime(cursor);
		setPeriod(cursor);
		setOpen(cursor);
		setHigh(cursor);
		setLow(cursor);
		setClose(cursor);
		setDirection(cursor);
		setVertex(cursor);
		setVertexLow(cursor);
		setVertexHigh(cursor);
		setPosition(cursor);
		setOverlapLow(cursor);
		setOverlapHigh(cursor);
		setAverage5(cursor);
		setAverage10(cursor);
		setDIF(cursor);
		setDEA(cursor);
		setHistogram(cursor);
		setSigmaHistogram(cursor);
		setDivergence(cursor);
		setAverage(cursor);
		setVelocity(cursor);
		setAcceleration(cursor);
		setAction(cursor);
		setTrendsEfforts(cursor);
	}

	public long getStockId() {
		return mStockId;
	}

	public void setStockId(long stockId) {
		mStockId = stockId;
	}

	void setStockID(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setStockId(cursor.getLong(cursor
				.getColumnIndex(DatabaseContract.COLUMN_STOCK_ID)));
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String date) {
		mDate = date;
	}

	void setDate(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDate(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DATE)));
	}

	public String getTime() {
		return mTime;
	}

	public void setTime(String time) {
		mTime = time;
	}

	void setTime(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setTime(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TIME)));
	}

	public String getPeriod() {
		return mPeriod;
	}

	void setPeriod(String period) {
		mPeriod = period;
	}

	void setPeriod(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setPeriod(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PERIOD)));
	}

	public double getOpen() {
		return mOpen;
	}

	public void setOpen(double open) {
		mOpen = open;
	}

	void setOpen(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setOpen(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_OPEN)));
	}

	public double getHigh() {
		return mHigh;
	}

	public void setHigh(double high) {
		mHigh = high;
	}

	void setHigh(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setHigh(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_HIGH)));
	}

	public double getLow() {
		return mLow;
	}

	public void setLow(double low) {
		mLow = low;
	}

	void setLow(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setLow(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_LOW)));
	}

	public double getClose() {
		return mClose;
	}

	public void setClose(double close) {
		mClose = close;
	}

	void setClose(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setClose(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_CLOSE)));
	}

	public int getDirection() {
		return mDirection;
	}

	public void setDirection(int direction) {
		mDirection = direction;
	}

	void setDirection(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDirection(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIRECTION)));
	}

	public int getVertex() {
		return mVertex;
	}

	public void setVertex(int vertex) {
		mVertex = vertex;
	}

	void setVertex(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setVertex(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VERTEX)));
	}

	public double getVertexLow() {
		return mVertexLow;
	}

	public void setVertexLow(double vertexLow) {
		mVertexLow = vertexLow;
	}

	void setVertexLow(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setVertexLow(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VERTEX_LOW)));
	}

	public double getVertexHigh() {
		return mVertexHigh;
	}

	public void setVertexHigh(double vertexHigh) {
		mVertexHigh = vertexHigh;
	}

	void setVertexHigh(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setVertexHigh(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VERTEX_HIGH)));
	}

	int getPosition() {
		return mPosition;
	}

	void setPosition(int position) {
		mPosition = position;
	}

	void setPosition(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setPosition(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_POSITION)));
	}

	public double getOverlapLow() {
		return mOverlapLow;
	}

	public void setOverlapLow(double overlapLow) {
		mOverlapLow = overlapLow;
	}

	void setOverlapLow(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setOverlapLow(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_OVERLAP_LOW)));
	}

	public double getOverlapHigh() {
		return mOverlapHigh;
	}

	public void setOverlapHigh(double overlapHigh) {
		mOverlapHigh = overlapHigh;
	}

	void setOverlapHigh(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setOverlapHigh(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_OVERLAP_HIGH)));
	}

	public double getAverage5() {
		return mAverage5;
	}

	public void setAverage5(double average) {
		mAverage5 = average;
	}

	void setAverage5(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setAverage5(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_AVERAGE5)));
	}

	public double getAverage10() {
		return mAverage10;
	}

	public void setAverage10(double average) {
		mAverage10 = average;
	}

	void setAverage10(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setAverage10(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_AVERAGE10)));
	}

	public double getDIF() {
		return mDIF;
	}

	public void setDIF(double dif) {
		mDIF = dif;
	}

	void setDIF(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDIF(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIF)));
	}

	public double getDEA() {
		return mDEA;
	}

	public void setDEA(double dea) {
		mDEA = dea;
	}

	void setDEA(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDEA(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DEA)));
	}

	public double getHistogram() {
		return mHistogram;
	}

	public void setHistogram(double histogram) {
		mHistogram = histogram;
	}

	void setHistogram(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setHistogram(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_HISTOGRAM)));
	}

	double getSigmaHistogram() {
		return mSigmaHistogram;
	}

	public void setSigmaHistogram(double sigmaHistogram) {
		mSigmaHistogram = sigmaHistogram;
	}

	void setSigmaHistogram(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setSigmaHistogram(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_SIGMA_HISTOGRAM)));
	}

	int getDivergence() {
		return mDivergence;
	}

	public void setDivergence(int divergence) {
		mDivergence = divergence;
	}

	void setDivergence(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDivergence(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIVERGENCE)));
	}

	double getTrendsEfforts() {
		return mTrendsEfforts;
	}

	void setTrendsEfforts(double trendsEfforts) {
		mTrendsEfforts = trendsEfforts;
	}

	void setTrendsEfforts(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setTrendsEfforts(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TRENDS_EFFORTS)));
	}

	public double getAverage() {
		return mAverage;
	}

	public void setAverage(double average) {
		mAverage = average;
	}

	public void setAverage(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setAverage(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_AVERAGE)));
	}

	public String getAction() {
		return mAction;
	}

	public void setAction(String action) {
		mAction = action;
	}

	void setAction(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setAction(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACTION)));
	}

	public boolean vertexOf(int vertexType) {
		boolean result = false;

		if ((mVertex & vertexType) == vertexType) {
			result = true;
		}

		return result;
	}

	public int getIndex() {
		return mIndex;
	}

	public void setIndex(int index) {
		mIndex = index;
	}

	public int getIndexStart() {
		return mIndexStart;
	}

	public void setIndexStart(int index) {
		mIndexStart = index;
	}

	public int getIndexEnd() {
		return mIndexEnd;
	}

	public void setIndexEnd(int index) {
		mIndexEnd = index;
	}

	String getMapKey() {
		return getDate() + " " + getTime();
	}

	String getOHLCString() {
		return String.valueOf(getOpen()) + String.valueOf(getHigh())
				+ String.valueOf(getLow()) + String.valueOf(getClose());
	}

	public boolean include(StockData stockData) {
		boolean result = false;

		if ((getVertexHigh() >= stockData.getVertexHigh())
				&& (getVertexLow() <= stockData.getVertexLow())) {
			result = true;
		}

		return result;
	}

	public boolean includedBy(StockData stockData) {
		boolean result = false;

		if ((getVertexHigh() <= stockData.getVertexHigh())
				&& (getVertexLow() >= stockData.getVertexLow())) {
			result = true;
		}

		return result;
	}

	public int directionTo(StockData stockData) {
		int result = Constants.STOCK_DIRECTION_NONE;

		if ((getVertexHigh() >= stockData.getVertexHigh())
				&& (getVertexLow() > stockData.getVertexLow())) {
			result = Constants.STOCK_DIRECTION_UP;
		} else if ((getVertexHigh() < stockData.getVertexHigh())
				&& (getVertexLow() <= stockData.getVertexLow())) {
			result = Constants.STOCK_DIRECTION_DOWN;
		} else {
			result = Constants.STOCK_DIRECTION_NONE;
		}

		return result;
	}

	public int positionTo(StockData stockData) {
		int position = 0;

		if (getVertexLow() > stockData.getVertexHigh()) {
			position = Constants.STOCK_POSITION_ABOVE;
		} else if (getVertexHigh() < stockData.getVertexLow()) {
			position = Constants.STOCK_POSITION_BELOW;
		} else {
			position = Constants.STOCK_POSITION_NONE;
		}

		return position;
	}

	public void merge(int directionType, StockData stockData) {
		if (directionType == Constants.STOCK_DIRECTION_UP) {
			setVertexHigh(Math.max(getVertexHigh(), stockData.getVertexHigh()));
			setVertexLow(Math.max(getVertexLow(), stockData.getVertexLow()));
		} else if (directionType == Constants.STOCK_DIRECTION_DOWN) {
			setVertexHigh(Math.min(getVertexHigh(), stockData.getVertexHigh()));
			setVertexLow(Math.min(getVertexLow(), stockData.getVertexLow()));
		} else {
			setVertexHigh(Math.max(getVertexHigh(), stockData.getVertexHigh()));
			setVertexLow(Math.min(getVertexLow(), stockData.getVertexLow()));
		}
	}

	public void merge(StockData prev, StockData current) {
		if ((prev == null) || (current == null)) {
			return;
		}

		setStockId(prev.getStockId());
		setIndex(prev.getIndex());
		setIndexStart(prev.getIndexStart());
		setVertexLow(Math.min(prev.getVertexLow(), current.getVertexLow()));
		setVertexHigh(Math.max(prev.getVertexHigh(), current.getVertexHigh()));
	}

	public int divergenceValue(int direction, StockData stockData) {
		int result = Constants.STOCK_DIVERGENCE_NONE;

		if (direction == Constants.STOCK_DIRECTION_UP) {
			if (getVertexHigh() > stockData.getVertexHigh()) {
				result = divergenceValue(stockData);
			}
		} else if (direction == Constants.STOCK_DIRECTION_DOWN) {
			if (getVertexLow() < stockData.getVertexLow()) {
				result = divergenceValue(stockData);
			}
		} else {
			result = Constants.STOCK_DIVERGENCE_NONE;
		}

		return result;
	}

	int divergenceValue(StockData stockData) {
		int result = Constants.STOCK_DIVERGENCE_NONE;

		if (Math.abs(getSigmaHistogram()) < Math.abs(stockData
				.getSigmaHistogram())) {
			result += Constants.STOCK_DIVERGENCE_SIGMA_HISTOGRAM;
		}

		if (Math.abs(getDIF()) < Math.abs(stockData.getDIF())) {
			result += Constants.STOCK_DIVERGENCE_DIF;
		}

		if (Math.abs(getHistogram()) < Math.abs(stockData.getHistogram())) {
			result += Constants.STOCK_DIVERGENCE_HISTOGRAM;
		}

		return result;
	}
}