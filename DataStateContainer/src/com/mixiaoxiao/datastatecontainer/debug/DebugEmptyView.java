package com.mixiaoxiao.datastatecontainer.debug;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mixiaoxiao.datastatecontainer.DataStateInterface.IEmptyView;
import com.mixiaoxiao.datastatecontainer.R;

public class DebugEmptyView extends FrameLayout implements IEmptyView{
	
	private final TextView mTextView;

	public DebugEmptyView(Context context) {
		this(context, null, 0);
	}

	public DebugEmptyView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DebugEmptyView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mTextView = new TextView(context, attrs);
		final int padding = (int) (context.getResources().getDisplayMetrics().density * 16f);
		mTextView.setPadding(padding, padding, padding, padding);
		mTextView.setBackgroundResource(R.drawable.common_background);
		FrameLayout.LayoutParams lp = new LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
		addView(mTextView, lp);
	}

	@Override
	public View getView() {
		return this;
	} 

	@Override
	public void onEmpty(boolean empty, boolean retry) {
		setVisibility(empty ? View.VISIBLE : View.GONE);
		if(retry){
			mTextView.setText("Empty\nClick to retry" );
			mTextView.setClickable(true);
		}else{
			mTextView.setText("There is empty really");
			mTextView.setClickable(false);
		}
	}
	@Override
	public void onRefreshIng() {
		mTextView.setText("Refreshing...");
	}

	@Override
	public View getRetryView() {
		return mTextView;
	}

}
