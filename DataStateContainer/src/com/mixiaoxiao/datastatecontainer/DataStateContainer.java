package com.mixiaoxiao.datastatecontainer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.Scroller;

import com.mixiaoxiao.datastatecontainer.DataStateInterface.CallBack;
import com.mixiaoxiao.datastatecontainer.DataStateInterface.IEmptyView;
import com.mixiaoxiao.datastatecontainer.DataStateInterface.ILoadView;
import com.mixiaoxiao.datastatecontainer.DataStateInterface.IRefreshView;
import com.mixiaoxiao.datastatecontainer.DataStateInterface.IScrollableViewWrapper;
import com.mixiaoxiao.datastatecontainer.ScrollableViewWrapper.OnWrapperScrollListener;

public class DataStateContainer extends ViewGroup implements OnWrapperScrollListener , OnClickListener{
	static final String LOG_TAG = "DataStateContainer";
	
	private static final Interpolator sInterpolator = new DecelerateInterpolator(2f);
	/**空闲状态**/
	public static final int STATE_IDLE = 0;
//	public static final int STATE_REFRESH_DRAG = 1;
	/**正在刷新**/
	public static final int STATE_REFRESH_ING = 2;
	/**刷新完毕之后显示“刷新成功”或“刷新失败”，一段时间延时后才进入STATE_IDLE**/
	public static final int STATE_REFRESH_END = 3;
	/**正在加载**/
	public static final int STATE_LOAD_ING = 4;
	
	//self state
	private int mState = STATE_IDLE;
	//children 
	private IScrollableViewWrapper mScrollableView; 
	private ILoadView mLoadView;
	private IRefreshView mRefreshView;
	private IEmptyView mEmptyView;
	
	//For drag to refresh
	/**刷新完成后“刷新成功”或“刷新失败”延时，delay后进入STATE_IDLE**/
	private static final int REFRESH_END_DELAY_DURATION = 600;
	//copy form supportv4 SwipeRefreshLayout
    private static final int INVALID_POINTER = -1;
	private static final float DRAG_RATE = .5f;
	private static final int ANIMATE_DURATION = 200; 
	private int mTouchSlop;
	private float mTotalDragDistance = -1;
	private float mInitialMotionY;
    private float mInitialDownY;
    private boolean mIsBeingDragged;
    private int mActivePointerId = INVALID_POINTER;
    //
    private Scroller mScroller;
    
    private CallBack mCallBack;
    
    public void setCallBack(CallBack callBack){
    	mCallBack = callBack; 
    	final boolean clickToLoad = !mCallBack.autoLoadWhenScrollToLastItem();
    	mLoadView.onLoadIdle(mCallBack.hasMoreDataToLoad(), 
    			clickToLoad, false);
    }
    
    private Runnable mRefreshDelayToIdleRunable = new Runnable() {
		@Override
		public void run() {
			final int scrollY = getScrollY();
			mScroller.startScroll(0, scrollY, 0, -scrollY, ANIMATE_DURATION);
			invalidate();
			setStateInternal(STATE_IDLE);
		}
	};

	public DataStateContainer(Context context) {
		this(context, null, 0);
	}

	public DataStateContainer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DataStateContainer(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mScroller = new Scroller(context, sInterpolator);
		
	}
	/**主动调用刷新**/
	public boolean requestRefresh(){
		if(mIsBeingDragged){
			return false;
		}
		if(mState == STATE_IDLE && mScroller.isFinished()){
			mScroller.startScroll(0, 0, 0, -mRefreshView.getExactHeight(), ANIMATE_DURATION);
			invalidate();
			setStateInternal(STATE_REFRESH_ING);
			return true;
		}
		return false;
	}
	/**刷新完毕**/
	public boolean onRefreshEnd(boolean success){
		if(mState == STATE_REFRESH_ING){
			log("onRefreshEnd success->" + success);
			if(success){
				clearEmptyInternal();
			}
			mRefreshView.onRefreshEnd(success);
			postDelayed(mRefreshDelayToIdleRunable, REFRESH_END_DELAY_DURATION);
			setStateInternal(STATE_REFRESH_END);
			return true;
		}
		return false;
	}
	/**加载完毕**/
	public boolean onLoadEnd(boolean success){
		if(mState == STATE_LOAD_ING){
			log("onLoadEnd success->" + success);
			final boolean hasMoreDataToLoad = mCallBack.hasMoreDataToLoad();
			//public void onLoadIdle(boolean hasMoreDataToLoad, boolean clickToLoad, boolean retry);
			if(success){
//				View loadView = mLoadView.getView();
//				loadView.offsetTopAndBottom(getHeight() - loadView.getTop());
				//reset LoadView 重置LoadView
				setLoadViewTranslationY(0);
				final boolean clickToLoad = !mCallBack.autoLoadWhenScrollToLastItem();
				mLoadView.onLoadIdle(hasMoreDataToLoad, clickToLoad, false);
			}else{
				mLoadView.onLoadIdle(hasMoreDataToLoad, true, true);
//				if(mCallBack.autoLoadWhenScrollToLastItem()){
//					if(mCallBack.hasMoreDataToLoad()){
//						mLoadView.onClickToLoad(true);
//					}else{
//						mLoadView.onLoadIdle(false);
//					}
//				}else{
//					
//				}
//				if(mCallBack.hasMoreDataToLoad()){
//					//retry
//					mLoadView.onClickToLoad(true);
//				}else{
//					mLoadView.onLoadIdle(false);
//				}
			}
		
			invalidate();
			setStateInternal(STATE_IDLE);
			return true;
		}
		return false;
	}
	public void onEmpty(boolean empty, boolean retry){
		mEmptyView.onEmpty(empty, retry);
	}
	private void clearEmptyInternal(){
		mEmptyView.onEmpty(false, false);
	}
	
	void setStateInternal(int newState){
		if(mState != newState ){
			mState = newState;
			if(mState == STATE_REFRESH_ING){
				mRefreshView.onRefreshIng();
				if(mCallBack != null){
					mCallBack.onRefresh();
				}
			}else if(mState == STATE_REFRESH_END){
				
			}else if(mState == STATE_LOAD_ING){
				mLoadView.onLoadIng();
				if(mCallBack != null){
					mCallBack.onLoad();
				}
			}else if(mState == STATE_IDLE){
				mRefreshView.onRefreshIdle();
				if(mCallBack != null){
					//mLoadView.onLoadIdle(mCallBack.hasMoreDataToLoad());
				}
			}
		}
		
	}
	
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        ensureTarget();

        final int action = MotionEventCompat.getActionMasked(ev);

//        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
//            mReturningToStart = false;
//        }

//        if (!isEnabled() || mReturningToStart || canChildScrollUp() || mRefreshing) {
//            // Fail fast if we're not in a state where a swipe is possible
//            return false;
//        }
        if(mState != STATE_IDLE){
        	return false;
        }
        if(!isEnabled() || !mScroller.isFinished() || mScrollableView.canScrollUp()){
        	return false;
        }


        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCircleView.getTop(), true);
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                final float initialDownY = getMotionEventY(ev, mActivePointerId);
                if (initialDownY == -1) {
                    return false;
                }
                mInitialDownY = initialDownY;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }

                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                final float yDiff = y - mInitialDownY;
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    mInitialMotionY = mInitialDownY + mTouchSlop;
                    mIsBeingDragged = true;
                    //mProgress.setAlpha(STARTING_PROGRESS_ALPHA);
                }
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }
    
    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }
    
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }
    
    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // Nope.
    }
    
    @SuppressLint("ClickableViewAccessibility") @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
//        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
//            mReturningToStart = false;
//        }
//        if (!isEnabled() || mReturningToStart || canChildScrollUp()) {
//            // Fail fast if we're not in a state where a swipe is possible
//            return false;
//        }
        if(mState != STATE_IDLE){
        	return false;
        }
        if(!isEnabled() || !mScroller.isFinished() || mScrollableView.canScrollUp() ){
        	return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                break;

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                if (mIsBeingDragged) {
                    //mProgress.showArrow(true);
                    float originalDragPercent = overscrollTop / mTotalDragDistance;
                    if (originalDragPercent < 0) {
                        return false;
                    }
                    scrollTo(0, -Math.round(overscrollTop));
                    invalidate();
                    if(mState == STATE_IDLE){
                    	mRefreshView.onRefreshDrag(originalDragPercent);
                    }
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mActivePointerId == INVALID_POINTER) {
                    if (action == MotionEvent.ACTION_UP) {
                        Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    }
                    return false;
                }
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                mIsBeingDragged = false;
                
                final int currScrollY = -Math.round(overscrollTop);
                scrollTo(0, currScrollY); 
                invalidate();
                if(mState != STATE_IDLE){
                	//current is loading more
                	mScroller.startScroll(0, currScrollY, 0, -currScrollY, ANIMATE_DURATION);
                	return false;
                }
                if (overscrollTop > mTotalDragDistance) {
                	log("setRefreshing true");
                	setStateInternal(STATE_REFRESH_ING);
//                    setRefreshing(true, true /* notify */);
                	mScroller.startScroll(0, currScrollY, 0, Math.round(-currScrollY - mTotalDragDistance), ANIMATE_DURATION);
                } else {
                	setStateInternal(STATE_IDLE);
                	mScroller.startScroll(0, currScrollY, 0, -currScrollY, ANIMATE_DURATION);
                	log("cancel refresh");
                    // cancel refresh
//                    mRefreshing = false;
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
        }

        return true;
    }
    
    //scroll
    @Override
   	public void computeScroll() {
    	if(mScroller.computeScrollOffset()){
    		scrollTo(0, mScroller.getCurrY());
    		ViewCompat.postInvalidateOnAnimation(this);
    	}
    }

    
    
    //measure and layout
    @Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int height = MeasureSpec.getSize(heightMeasureSpec);
		if (width <= 0 || height <= 0) {
			throw new IllegalStateException("Width or height must > 0");
		}
		setMeasuredDimension(width, height);
		mScrollableView.getView().measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
		mLoadView.getView().measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(mLoadView.getExactHeight(), MeasureSpec.EXACTLY));
		mRefreshView.getView().measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(mRefreshView.getExactHeight(), MeasureSpec.EXACTLY));
		mEmptyView.getView().measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), 
				MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int width = getMeasuredWidth();//= r - l;
        final int height = getMeasuredHeight();//= b - t;
		mScrollableView.getView().layout(0, 0, width, height);
		mLoadView.getView().layout(0, height, width, height + mLoadView.getExactHeight());
		mRefreshView.getView().layout(0, - mRefreshView.getExactHeight(), width, 0);
		mEmptyView.getView().layout(0, 0, width, height);
	} 

	private void setLoadViewTranslationY(final int translationY){
		mLoadView.getView().setTranslationY(translationY);
	}
	
	//ScrollableViewWrapper
	@Override
	public void onLastChildScrolled(int bottomSpace) {
		log("onLastChildScrolled bottomSpace="+ bottomSpace);
		final int space = bottomSpace;//MathUtil.limitInt(bottomSpace, 0, mLoadView.getExactlyHeight());
		if(mState == STATE_IDLE){
			if(mCallBack != null){
				if(mCallBack.autoLoadWhenScrollToLastItem() && mCallBack.hasMoreDataToLoad()){
					if(space >= mLoadView.getExactHeight()){
						//RecyclerView在有Divider的时候拉到最下面时
						//space=mLoadView.getExactHeight()+divider的高度
						setStateInternal(STATE_LOAD_ING);
					}
				}else{//非自动加载，需要clickToLoad
					if(mCallBack.hasMoreDataToLoad()){
						mLoadView.onLoadIdle(true, true, false);
					}else{
						mLoadView.onLoadIdle(false, true , false);
					}
					
				}
			}
		}
		setLoadViewTranslationY(-space);
		//final int newLoadViewTop = getHeight() - space;
//		mLoadView.getView().offsetTopAndBottom(newLoadViewTop - mLoadView.getView().getTop()) ;
		//invalidate();
	}

	@Override
	public void onLastChildVisibilityChanged(boolean visible) {
		log("onLastChildVisibilityChanged visible=" + visible);
		mLoadView.getView().setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
	}
	
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		checkChildren();
	}

	private void checkChildren() {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child instanceof AbsListView) {
				AbsListView absListView = (AbsListView) child;
				absListView.setClipChildren(false);
				absListView.setClipToPadding(false);
				mScrollableView = new ScrollableViewWrapper(absListView, this);
			}else if(child instanceof RecyclerView){
				RecyclerView recyclerView = (RecyclerView) child;
				recyclerView.setClipChildren(false);
				recyclerView.setClipToPadding(false);
				mScrollableView = new ScrollableViewWrapper(recyclerView, this);
			}else if (child instanceof ILoadView) {
				mLoadView = (ILoadView) child;
				mLoadView.onLoadIdle(true, false, false);
				mLoadView.getView().setOnClickListener(this);
			}else if(child instanceof IRefreshView){
				mRefreshView = (IRefreshView) child	;
				mRefreshView.onRefreshIdle();
				mTotalDragDistance = mRefreshView.getExactHeight();
			}else if(child instanceof IEmptyView){
				mEmptyView = (IEmptyView) child;
				mEmptyView.getRetryView().setOnClickListener(this);
				clearEmptyInternal();
			}
		}
		mScrollableView.getView().setPadding(0, 0, 0, mLoadView.getExactHeight());
		onLastChildVisibilityChanged(false);//LoadView初始置为不可见
	}
	
	private final void log(String msg){
		Log.d("DataStateContainer", msg);
	}

	@Override
	public void onClick(View v) {
		if(v == mEmptyView.getRetryView()){
			requestRefresh();
		}else if( v== mLoadView.getView()){
			if(mState == STATE_IDLE){
				if(mCallBack.hasMoreDataToLoad()){
					setStateInternal(STATE_LOAD_ING);
				}else{
					mLoadView.onLoadIdle(false, true, false);
				}
			}
		}
	}

	

}
