package com.mixiaoxiao.datastatecontainer;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;

import com.mixiaoxiao.datastatecontainer.DataStateInterface.IScrollableViewWrapper;

public class ScrollableViewWrapper implements IScrollableViewWrapper {
	
	private void log(String msg){
		Log.d("ScrollableViewWrapper", msg);
	}

	public static interface OnWrapperScrollListener {
		//public void onScrollFirstChild(View view, int topSpace);
		/**bottomSpace是指LastChild.bottom距离parent的底部的距离**/
		public void onLastChildScrolled(int bottomSpace, boolean scrollStateIdle);
		//public void onScrollIdle(View view);
		public void onLastChildVisibilityChanged(boolean visible);
		public void onLastChildTerminalChanged(boolean terminal);
	}

	private final View mView;
	private final OnWrapperScrollListener mScrollListener;
	/**标记LastChild是否可见**/
	private boolean mLastChildVisible = false;
	/**标记LastChild是否“到达终点”，即不可再上滑，注意这个仅在LastChildVisible=true时判断并回调**/
	private boolean mLastChildTerminal = false;
	
	private void dispatchBottomSpaceChanged(final int bottomSpace){
		if(mLastChildTerminal){
			if(bottomSpace < mView.getPaddingBottom()){
				mLastChildTerminal = false;
				mScrollListener.onLastChildTerminalChanged(mLastChildTerminal);
			}
		}else{
			//>=是因为RecyclerView在有Divider的时候拉到最下面时
			//bottomSpace=mLoadView.getExactHeight()+divider的高度
			if(bottomSpace >= mView.getPaddingBottom()){
				mLastChildTerminal = true;
				mScrollListener.onLastChildTerminalChanged(mLastChildTerminal);
			}
		} 
		//log("dispatchBottomSpaceChanged->" + bottomSpace + " Terminal->" + mLastChildTerminal);
		
//		final boolean terminal = !canScrollDown();
		//canScrollDown判断不对啊,ListView的最后一个View.bottomo到线时就“不能滚动了”
		//没考虑paddingbottom
//		if(mLastChildTerminal != terminal){
//			mLastChildTerminal = terminal;
//			mScrollListener.onLastChildTerminalChanged(terminal);
//		}
		mScrollListener.onLastChildScrolled(bottomSpace, 
				true);
	}
	
	public ScrollableViewWrapper(final AbsListView listView, OnWrapperScrollListener scrollListener) {
		this.mView = listView;   
		this.mScrollListener = scrollListener;
		//scrollListener.onLastChildVisibilityChanged(mLastChildVisible);
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}

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
							final int bottomSpace = mView.getHeight() - lastChild.getBottom();
							dispatchBottomSpaceChanged(bottomSpace);
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

	public ScrollableViewWrapper(RecyclerView recyclerView, OnWrapperScrollListener scrollListener) {
		this.mView = recyclerView;
		this.mScrollListener = scrollListener;
		//scrollListener.onLastChildVisibilityChanged(mLastChildVisible);
		//Fuck the setOnScrollListener
		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			int[] lastPositions;
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
					final int totalItemCount = linearLayoutManager.getItemCount();
					
					boolean lastItemVisible = false; 
					if(totalItemCount > 0 ){
						final int parentHeight = mView.getHeight();
						final int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
						log("lastVisiblePosition->" + lastVisiblePosition + " totalItemCount->" + totalItemCount);
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
									if(lastChild.getBottom() >= (parentHeight - mView.getPaddingBottom())){
										//Oh, fuck the recyclerView
										//LinearLayoutManager（非Grid）的lastChild.getBottom()是永远< mView.getHeight的
										//需要减掉mView.getPaddingBottom？？
										lastItemVisible = true;
									}
								}
								log("lastChild.getBottom->" + lastChild.getBottom() + " parentHeight->" + parentHeight + " lastItemVisible->" + lastItemVisible);
								if(lastItemVisible){
									final int bottomSpace = parentHeight - lastChild.getBottom();
									dispatchBottomSpaceChanged(bottomSpace);
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
				}else if(layoutManager instanceof StaggeredGridLayoutManager){
					StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
					final int totalItemCount = staggeredGridLayoutManager.getItemCount();
					if(totalItemCount > 0){
						final int parentHeight = mView.getHeight();
						final int spanCount = staggeredGridLayoutManager.getSpanCount();
						if(lastPositions == null || lastPositions.length != spanCount){
							lastPositions = new int[spanCount];
						}
						//Returns the adapter position of the first visible view for each span.
						//源码中如果参数=null则new int[mSpanCount]，如果不为null则必须参数.length=spanCount
						//如果传了参数则返回参数，未传参数则返回new int[mSpanCount]
						staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);  
						int lastChildBottom = 0;//取最大的childBottom
						int lastVisiblePosition = 0;//取最大的position
						//lastChildBottom和lastVisiblePosition可能不对应同一个View
						//lastVisiblePosition用来判断是否最后一个View已经显示
						//lastChildBottom用来计算bottomSpace
						for(int position : lastPositions){
							if(position > lastVisiblePosition){
								lastVisiblePosition = position;
							}
							View child = staggeredGridLayoutManager.findViewByPosition(position);
							if(child != null){
								final int childBottom = child.getBottom();
								if(lastChildBottom < childBottom){
									lastChildBottom = childBottom;
								}
							}
						}
						boolean lastItemVisible = false; 
						if (lastVisiblePosition == (totalItemCount - 1)) {
							lastItemVisible = mLastChildVisible;
							if(!lastItemVisible){
								if(lastChildBottom >= (parentHeight - mView.getPaddingBottom())){
									lastItemVisible = true;
								}
							}
							log("staggeredGrid.lastChildBottom->" + lastChildBottom + " parentHeight->" + parentHeight + " lastItemVisible->" + lastItemVisible);
							if(lastItemVisible){
								final int bottomSpace = mView.getHeight() - lastChildBottom;
								dispatchBottomSpaceChanged(bottomSpace);
							}
						}
						log("staggeredGrid.check over lastItemVisible->" + lastItemVisible);
						if(mLastChildVisible != lastItemVisible){
							mLastChildVisible = lastItemVisible;
							mScrollListener.onLastChildVisibilityChanged(lastItemVisible);
						}
					}
				}else{
					throw new RuntimeException("Unsupported LayoutManager! Your should add your codes to support your LayoutManager here");
				}
			}
		});
	}
	
	@Override
	public View getView() {
		return mView;
	}
	
	/**是否可向上翻页，注意ScrollUp是手指向下滑动***/
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
