package com.mixiaoxiao.datastatecontainer.debug;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.mixiaoxiao.datastatecontainer.DataStateInterface.IRefreshView;

public class DebugRefreshView extends TextView implements IRefreshView{

	
	private final int mExactHeight;
	
	public DebugRefreshView(Context context) {
		this(context, null, 0);
	}
	
	public DebugRefreshView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DebugRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mExactHeight = (int)(context.getResources().getDisplayMetrics().density * 48 + 0.5f );
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
	public void onRefreshEnd(boolean success) {
		setText("RefreshEnd " + (success ? "success" : "error") + "\n" + (success ? "刷新成功" : "刷新失败"));
	}


	@Override
	public void onRefreshIdle() {
		setText("RefreshIdle" + "\n下拉刷新");
		
	}

	@Override
	public void onRefreshIng() {
		setText("RefreshIng" + "\n正在刷新...");
		
	}

	@Override
	public void onRefreshDrag(float percent) {
		//setText("onRefreshDrag " + String.format("%.2f", percent * 100) + "%");
		setText("RefreshDrag " + (int)( percent * 100 + 0.5f) + "%" + "\n正在下拉");
	}

}
