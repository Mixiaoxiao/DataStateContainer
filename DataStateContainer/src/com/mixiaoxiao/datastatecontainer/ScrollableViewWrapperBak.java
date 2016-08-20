package com.mixiaoxiao.datastatecontainer;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.mixiaoxiao.datastatecontainer.DataStateInterface.IScrollableViewWrapper;

public class ScrollableViewWrapperBak implements IScrollableViewWrapper {
	
	private void log(String msg){
		Log.d("ScrollableViewWrapper", msg);
	}

	public static interface OnWrapperScrollListener {
		//public void onScrollFirstChild(View view, int topSpace);
		/**bottomSpace是指LastChild.bottom距离parent的底部的距离**/
		public void onLastChildScrolled(int bottomSpace);
		//public void onScrollIdle(View view);
		public void onLastChildVisibilityChanged(boolean visible);
	}

	private final View mView;
	private final OnWrapperScrollListener mScrollListener;
	private boolean mLastChildVisible = false;
	

	public ScrollableViewWrapperBak(final AbsListView listView, OnWrapperScrollListener scrollListener) {
		this.mView = listView;   
		this.mScrollListener = scrollListener;
		//scrollListener.onLastChildVisibilityChanged(mLastChildVisible);
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:
					// Log.v("已经停止：SCROLL_STATE_IDLE");
					break;
				case OnScrollListener.SCROLL_STATE_FLING:
					// Log.v("开始滚动：SCROLL_STATE_FLING");
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					// Log.v("正在滚动：SCROLL_STATE_TOUCH_SCROLL");
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				boolean lastItemVisible = false; 
				if (totalItemCount > 0) {
					final int lastVisiblePosition = view.getLastVisiblePosition();
					if (lastVisiblePosition == (totalItemCount - 1)) {
						lastItemVisible = mLastChildVisible;
						View lastChild = listView.getChildAt(listView.getChildCount() - 1);
						if(!lastItemVisible ){
							//仅当lastChild的底部在parent之外的时候把lastItemVisible置为true;
							//这样在全部child不满一屏幕的时候lastItemVisible始终是false;
							if(lastChild.getBottom() >= mView.getHeight()){
								lastItemVisible = true;
							}
						}
						if(lastItemVisible){
							mScrollListener.onLastChildScrolled(mView.getHeight() - lastChild.getBottom());
						}
					}
				}
				if(mLastChildVisible != lastItemVisible){
					mLastChildVisible = lastItemVisible;
					mScrollListener.onLastChildVisibilityChanged(lastItemVisible);
				}
			}
		});

	}

	public ScrollableViewWrapperBak(RecyclerView recyclerView, OnWrapperScrollListener scrollListener) {
		this.mView = recyclerView;
		this.mScrollListener = scrollListener;
		//scrollListener.onLastChildVisibilityChanged(mLastChildVisible);
		//Fuck the setOnScrollListener
		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				LayoutManager layoutManager = recyclerView.getLayoutManager();
				if (layoutManager instanceof LinearLayoutManager) {
					//是LinearLayoutManager或GridLayoutManager，因为GridLayoutManager继承于LinearLayoutManager
					//GridLayoutManager extends android.support.v7.widget.LinearLayoutManager 
					//StaggeredGridLayoutManager extends android.support.v7.widget.RecyclerView$LayoutManager
					//StaggeredGridLayoutManager.findLastVisibleItemPositions(int[] arg);
					//log("RecyclerView.onScrolled->" + dy);
					LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
					//findLastCompletelyVisibleItemPosition();//
					final int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
					final int totalItemCount = linearLayoutManager.getItemCount();
					log("lastVisiblePosition->" + lastVisiblePosition + " totalItemCount->" + totalItemCount);
					boolean lastItemVisible = false; 
					if(totalItemCount > 0 ){
						if (lastVisiblePosition == (totalItemCount - 1)) {
							lastItemVisible = mLastChildVisible;
							//RecyclerView.findViewHolderForLayoutPosition:
							//Return the ViewHolder for the item in the given position of the data set as of the latest
							//LayoutManager.findViewByPosition:
							//Finds the view which represents the given adapter position.
							final View lastChild = linearLayoutManager.findViewByPosition(lastVisiblePosition);
							if(lastChild != null){
//								View lastChild = recyclerView.findViewHolderForLayoutPosition(lastVisiblePosition).itemView;  
//								View lastChild1 = linearLayoutManager.findViewByPosition(lastVisiblePosition);
//								log("lastChild == lastChild1 ->" + (lastChild == lastChild1));
//								View lastChild2 = recyclerView.findViewHolderForAdapterPosition(lastVisiblePosition).itemView;
//								log("lastChild == lastChild2 ->" + (lastChild == lastChild2));
								//经测试 这三种方式都是同一个View
//								if(lastChild instanceof TextView){
									//log("lastChild->" + ((TextView)lastChild).getText());
//								}
								if(!lastItemVisible){
									if(lastChild.getBottom() >= (mView.getHeight()- mView.getPaddingBottom())){
									//Oh, fuck the recyclerView, lastChild.getBottom()是永远< mView.getHeight的
										//需要减掉mView.getPaddingBottom？？
										lastItemVisible = true;
									}
								}
								log("lastChild.getBottom->" + lastChild.getBottom() + " mView.getHeight->" + mView.getHeight() + " lastItemVisible->" + lastItemVisible);
								if(lastItemVisible){
									mScrollListener.onLastChildScrolled(mView.getHeight() - lastChild.getBottom());
								}
							}else{
								log("lastChild is NULL???");
							}
						}
					}
					log("check over lastItemVisible->" + lastItemVisible);
					if(mLastChildVisible != lastItemVisible){
						mLastChildVisible = lastItemVisible;
						mScrollListener.onLastChildVisibilityChanged(lastItemVisible);
					}
				}
			}
		});
	}

	@Override
	public View getView() {
		return mView;
	}

	@Override
	public boolean canScrollUp() {
		// copy form swipeRefreshLayout canChildScrollUp
		View mTarget = mView;
		if (android.os.Build.VERSION.SDK_INT < 14) {
			if (mTarget instanceof AbsListView) {
				final AbsListView absListView = (AbsListView) mTarget;
				return absListView.getChildCount() > 0
						&& (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0).getTop() < absListView
								.getPaddingTop());
			} else {
				return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
			}
		} else {
			return ViewCompat.canScrollVertically(mTarget, -1);
		}
	}

}
