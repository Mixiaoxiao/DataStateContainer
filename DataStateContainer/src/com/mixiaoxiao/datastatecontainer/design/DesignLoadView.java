package com.mixiaoxiao.datastatecontainer.design;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.mixiaoxiao.datastatecontainer.DataStateInterface.ILoadView;

public class DesignLoadView extends TextView implements ILoadView{
	
	private final int mExactHeight;

	public DesignLoadView(Context context) {
		this(context, null, 0);
	}

	public DesignLoadView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@SuppressWarnings("deprecation")
	public DesignLoadView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mExactHeight = (int)(context.getResources().getDisplayMetrics().density * 48 + 0.5f );
		StateListDrawable background = new StateListDrawable();
		background.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(0x22000000));
		background.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT));
		setBackgroundDrawable(background);
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
		setText("Loading...");
	}

	@Override
	public void onLoadIdle(boolean hasMoreDataToLoad, boolean clickToLoad, boolean retry) {
		if(hasMoreDataToLoad){
			if(clickToLoad){
				if(retry){
					setText("Click to retry");//点击重新加载
				}else{
					setText("Click to load more");//点击加载更多
				}
			}else{
				setText("Pull up to load more");//自动加载
			}
			//我们这里认为只要hasMore，均可以使用clickToLoad
			setClickable(true);
		}else{
			setClickable(false);
			setText("All loaded");//已加载全部数据
		}
		
	}

	

}
