package com.mixiaoxiao.datastatecontainer;

import android.view.View;

public class DataStateInterface {

	private DataStateInterface() {
		super();
	}

	public static interface CallBack {
		public void onRefresh();

		public void onLoad();

		public boolean autoLoadWhenScrollToLastItem();

		public boolean hasMoreDataToLoad();
	}

	/** Same as the function of AnimatorListenerAdapter **/
	public static class CallBackAdapter implements CallBack {

		@Override
		public void onRefresh() {
		}

		@Override
		public void onLoad() {
		}

		@Override
		public boolean autoLoadWhenScrollToLastItem() {
			return true;
		}

		@Override
		public boolean hasMoreDataToLoad() {
			return true;
		}

	}

	public static interface IScrollableViewWrapper {
		public View getView();

		public boolean canScrollUp();
	}

	public static interface IRefreshView {
		public View getView();

		public int getExactHeight();

		public void onRefreshIdle();

		public void onRefreshDrag(float percent);

		public void onRefreshIng();

		public void onRefreshEnd(boolean success);
	}

	public static interface ILoadView {
		public View getView();

		public int getExactHeight();

		/** clickToLoad仅在hasMoreDataToLoad=true是有效，retry仅在clickToLoad=true时有效 **/
		public void onLoadIdle(boolean hasMoreDataToLoad, boolean clickToLoad, boolean retry);

		public void onLoadIng();
	}

	public static interface IEmptyView {
		public View getView();

		public View getRetryView();

		public void onEmpty(boolean empty, boolean retry);
	}
}
