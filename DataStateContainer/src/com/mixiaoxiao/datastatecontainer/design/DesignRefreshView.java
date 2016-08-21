package com.mixiaoxiao.datastatecontainer.design;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import com.mixiaoxiao.datastatecontainer.DataStateInterface.IRefreshView;

public class DesignRefreshView extends View implements IRefreshView {

	private static final int COLOR_BLUE = 0xff03a9f4;
	private static final int COLOR_WHITE = 0xffffffff;

	private static final int STATE_IDLE = 0;
	private static final int STATE_DRAG = 1;
	private static final int STATE_ING = 2;
	private static final int STATE_END = 3;

	private final int mExactHeight;
	private final float mDensity;
	private float mDragPercent = 0f;
	private int mState = STATE_IDLE;
	private final Paint mPaint;
	private boolean mEndSuccess = true;
	private final float mBackgroundCircleRadius;
	private final Path mDragArrowPath, mIngPath, mEndSuccessPath, mEndFailurePath;
	private final RotateAnimation mRefreshIngAnimation;
	private final RectF mBackgroundCircleRectF;

	public DesignRefreshView(Context context) {
		this(context, null, 0);
	}

	public DesignRefreshView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DesignRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mDensity = context.getResources().getDisplayMetrics().density;
		mExactHeight = (int)( dp2px(56f) + 0.5f);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStrokeWidth(dp2px(2f));// 2dp
		mBackgroundCircleRadius = dp2px(14);
		mBackgroundCircleRectF = new RectF();
		mBackgroundCircleRectF.left = -mBackgroundCircleRadius;
		mBackgroundCircleRectF.top = -mBackgroundCircleRadius;
		mBackgroundCircleRectF.right = mBackgroundCircleRadius;
		mBackgroundCircleRectF.bottom = mBackgroundCircleRadius;
		final float markHalfSize = dp2px(8); // 以此为基准
		// arrow mark
		mDragArrowPath = new Path();
		final float arrowHalfWidth = markHalfSize * 0.65f;
		final float arrowHalfHeight = markHalfSize * 0.9f;
		mDragArrowPath.moveTo(0, -arrowHalfHeight);
		mDragArrowPath.lineTo(0, arrowHalfHeight);// 竖线
		mDragArrowPath.moveTo(-arrowHalfWidth, arrowHalfHeight * 0.27f);// 箭头左边
		mDragArrowPath.lineTo(0, arrowHalfHeight);// 下
		mDragArrowPath.lineTo(arrowHalfWidth, arrowHalfHeight * 0.27f);// 右

		// refreshIng mark
		mIngPath = new Path();
		final float ingRadius = markHalfSize;
		RectF oval = new RectF();
		oval.left = -ingRadius;
		oval.top = -ingRadius;
		oval.right = ingRadius;
		oval.bottom = ingRadius;
		final float offsetAngle = 16;
		// 右半边弧线
		mIngPath.arcTo(oval, -(90 - offsetAngle), 180 - 2 * offsetAngle, true);
		mIngPath.lineTo(dp2px(3), ingRadius - dp2px(4));
		// 左半边弧线
		// 我擦 如果这个path不是empty，那么这个arc的起点会由上个终点lineTo新的arc的起点
		// 所以还是采取旋转一下canvas再画一次
		// mIngPath.arcTo(oval, 90 + offsetAngle, 180 - 2 * offsetAngle, true);
		// mIngPath.lineTo(dp2px(2), ingRadius - dp2px(4));

		// end success
		mEndSuccessPath = new Path();
		final float hookHalfSize = markHalfSize * 0.9f;
		mEndSuccessPath.moveTo(-hookHalfSize, -hookHalfSize * 0.05f);// 左
		mEndSuccessPath.lineTo(-hookHalfSize * 0.25f, hookHalfSize * 0.8f);// 下
		mEndSuccessPath.lineTo(hookHalfSize, -hookHalfSize * 0.6f);// 右
		// end failure
		mEndFailurePath = new Path();
		final float xHalfSize = markHalfSize * 0.65f;
		mEndFailurePath.moveTo(-xHalfSize, -xHalfSize);// 左上角
		mEndFailurePath.lineTo(xHalfSize, xHalfSize);// 右下角
		mEndFailurePath.moveTo(xHalfSize, -xHalfSize);// 右上角
		mEndFailurePath.lineTo(-xHalfSize, xHalfSize);// 左下角

		mRefreshIngAnimation = new RotateAnimation(0, 360, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mRefreshIngAnimation.setInterpolator(new LinearInterpolator());
		mRefreshIngAnimation.setRepeatCount(Animation.INFINITE);
		mRefreshIngAnimation.setDuration(500);
	}

	private void setStateInternal(final int newState) {
		if (mState != newState) {
			mState = newState;
			invalidate();
			clearAnimation();
			if (newState == STATE_ING) {
				startAnimation(mRefreshIngAnimation);
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		final int sc = canvas.save();
		canvas.translate(getWidth() / 2f, getHeight() / 2f);
		switch (mState) {
		case STATE_DRAG:
			drawDrag(canvas);
			break;
		case STATE_ING:
			drawIng(canvas);
			break;
		case STATE_END:
			drawEnd(canvas);
			break;
		default:// IDLE
			drawIdle(canvas);
			break;
		}
		canvas.restoreToCount(sc);
	}

	// draw state
	private void drawIdle(Canvas canvas) {
		drawDragArrow(canvas);
	}

	private void drawDrag(Canvas canvas) {
		if (mDragPercent < 1f) {
			mPaint.setStyle(Style.STROKE);
			canvas.drawArc(mBackgroundCircleRectF, -90,360 * mDragPercent, false, mPaint);
			drawDragArrow(canvas);
		} else {
			drawCommonBackground(canvas);
			drawRefreshMark(canvas);
		}

	}

	private void drawIng(Canvas canvas) {
		drawCommonBackground(canvas);
		drawRefreshMark(canvas);
	}

	private void drawEnd(Canvas canvas) {
		drawCommonBackground(canvas);
		mPaint.setStyle(Style.STROKE);
		mPaint.setColor(COLOR_WHITE);
		if (mEndSuccess) {
			canvas.drawPath(mEndSuccessPath, mPaint);
		} else {
			canvas.drawPath(mEndFailurePath, mPaint);
		}
	}

	// draw common content
	private void drawCommonBackground(Canvas canvas) {
		//因为drag状态时画的进度空心圆是STROKE的（会比用FILL画出来的圆形半径稍大半个strokeWidth?）
		//这里取FILL_AND_STROKE画的实心圆就和STROKE的空心圆一样大了
		mPaint.setStyle(Style.FILL_AND_STROKE);
		mPaint.setColor(COLOR_BLUE);
		canvas.drawCircle(0, 0, mBackgroundCircleRadius, mPaint);
	}

	private void drawDragArrow(Canvas canvas) {
		mPaint.setStyle(Style.STROKE);
		mPaint.setColor(COLOR_BLUE);
		canvas.drawPath(mDragArrowPath, mPaint);
	}

	private void drawRefreshMark(Canvas canvas) {
		mPaint.setStyle(Style.STROKE);
		mPaint.setColor(COLOR_WHITE);
		canvas.drawPath(mIngPath, mPaint);
		canvas.rotate(180);
		canvas.drawPath(mIngPath, mPaint);
	}

	private float dp2px(float dp) {
		return mDensity * dp;
	}

	// IRefreshView
	@Override
	public View getView() {
		return this;
	}

	@Override
	public int getExactHeight() {
		return mExactHeight;
	}

	@Override
	public void onRefreshIdle() {
		setStateInternal(STATE_IDLE);
	}

	@Override
	public void onRefreshDrag(float percent) {
		setStateInternal(STATE_DRAG);
		mDragPercent = percent;
		invalidate();
	}

	@Override
	public void onRefreshIng() {
		setStateInternal(STATE_ING);
	}

	@Override
	public void onRefreshEnd(boolean success) {
		setStateInternal(STATE_END);
		mEndSuccess = success;
	}
}
