package com.mixiaoxiao.datastatecontainer.design;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mixiaoxiao.datastatecontainer.DataStateInterface.IEmptyView;

public class DesignEmptyView extends LinearLayout implements IEmptyView{
	
	private final TextView mInfoView, mRetryView; 
	private boolean mIsEmpty;

	public DesignEmptyView(Context context) {
		this(context, null, 0);
	}

	public DesignEmptyView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@SuppressWarnings("deprecation")
	public DesignEmptyView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setGravity(Gravity.CENTER);
		setOrientation(LinearLayout.VERTICAL);
		mInfoView = new TextView(context); 
		final int dp8 = (int) (context.getResources().getDisplayMetrics().density * 8f + 0.5f);
		mInfoView.setPadding(dp8, dp8, dp8, dp8);
		mInfoView.setTextColor(0xff666666);
		mInfoView.setText("Info");
		mInfoView.setGravity(Gravity.CENTER);
		mInfoView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		addView(mInfoView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		
		mRetryView = new TextView(context);
		mRetryView.setTextColor(0xffffffff);
		mRetryView.setClickable(true);
		mRetryView.setGravity(Gravity.CENTER);
		mRetryView.setPadding(dp8 * 2, dp8, dp8 * 2, dp8);
		mRetryView.setMinimumWidth(dp8 * 10);
		mRetryView.setVisibility(View.GONE);
		mRetryView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		LinearLayout.LayoutParams lp = new  LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.topMargin = dp8;
		mRetryView.setBackgroundDrawable(makeRetryBackground(dp8));
		mRetryView.setText("Retry");
		addView(mRetryView, lp);
	}

	@Override
	public View getView() {
		return this;
	} 

	@Override
	public void onEmpty(boolean empty, boolean retry) {
		mIsEmpty = empty;
		setVisibility(empty ? View.VISIBLE : View.GONE);
		if(retry){
			mInfoView.setText("Something wrong happened");
			mRetryView.setClickable(true);
			mRetryView.setVisibility(View.VISIBLE);
		}else{
			mInfoView.setText("Nothing here");
			mRetryView.setClickable(false);
			mRetryView.setVisibility(View.GONE);
		}
	}
	

	@Override
	public View getRetryView() {
		return mRetryView;
	}
	
	@Override
	public void onRefreshIng() {
		if(mIsEmpty){
			mInfoView.setText("Refreshing...");
			mRetryView.setVisibility(View.GONE);
		}
	}
	
	private Drawable makeRetryBackground(float cornerRadius){
		StateListDrawable background = new StateListDrawable();
		GradientDrawable pressed = new GradientDrawable();
		pressed.setColor(0xff0288d1);
		pressed.setCornerRadius(cornerRadius);
		GradientDrawable normal = new GradientDrawable();
		normal.setColor(0xff03a9f4);
		normal.setCornerRadius(cornerRadius);
		background.addState(new int[]{android.R.attr.state_pressed}, pressed);
		background.addState(new int[]{}, normal);
		return background;
		
	}

}
