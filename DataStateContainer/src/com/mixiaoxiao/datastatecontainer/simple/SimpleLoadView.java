package com.mixiaoxiao.datastatecontainer.simple;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mixiaoxiao.datastatecontainer.DataStateInterface.ILoadView;

public class SimpleLoadView extends TextView implements ILoadView{
	
	private void log(String msg){
		Log.d("SimpleLoadView", msg);
	}
	
	private final int mExactHeight;// = (int)(Resources.getSystem().getDisplayMetrics().density * 48 + 0.5f ); 

	public SimpleLoadView(Context context) {
		this(context, null, 0);
	}

	public SimpleLoadView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SimpleLoadView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mExactHeight = (int)(context.getResources().getDisplayMetrics().density * 48 + 0.5f ); ;
	}

	@Override
	public View getView() {
		return this;
	} 
 
	@Override
	public int getExactHeight() {
		return mExactHeight;
	}

	@Override
	public void onLoadIng() {
		log("onLoadIng");
		setText("Loading" + "\n正在加载更多...");
	}

	@Override
	public void onLoadIdle(boolean hasMoreDataToLoad, boolean clickToLoad, boolean retry) {
		log("onLoadIdle hasMore->" + hasMoreDataToLoad+ " click->" + clickToLoad + " retry->" + retry);
		if(hasMoreDataToLoad){
			if(clickToLoad){
				if(retry){
					setText("ClickToLoad Retry\n点击重新加载");
				}else{
					setText("ClickToLoad\n点击加载更多");
				}
				setClickable(true);
			}else{
				setText("AutoLoad(ClickToLoad=false)\n加载更多");
				setClickable(false);
			}
			
		}else{
			setClickable(false);
			setText("NoMoreDataToLoad\n已加载全部数据");
		}
		
	}

	

}
