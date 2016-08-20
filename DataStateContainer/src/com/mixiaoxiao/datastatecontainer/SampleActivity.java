package com.mixiaoxiao.datastatecontainer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.mixiaoxiao.datastatecontainer.DataStateInterface.CallBack;


public class SampleActivity extends Activity {
	
	final int FAKE_DELAY = 1000;
	ListView mListView;
	ArrayAdapter<String> mListViewAdapter;
	ArrayList<String> mListViewData = new ArrayList<String>();
	ArrayList<String> mRecyclerViewData = new ArrayList<String>();
	RecyclerView mRecyclerView;
	SimpleRecyclerViewAdapter mRecyclerViewAdapter;
	DataStateContainer mListViewContainer, mRecyclerViewContainer;
	CheckBox mCheckBoxAutoLoad, mCheckBoxHasMore, mCheckBoxDataSuccess;
	Button mEmpty, mEmptyRetry;
	RadioGroup mRadioGroup;
	
	private LayoutManager mLLayoutManager, mGLayoutManager, mSLayoutManager;
	
    @Override 
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        mCheckBoxAutoLoad = (CheckBox) findViewById(R.id.sample_checkbox_autoLoad);
        mCheckBoxDataSuccess = (CheckBox) findViewById(R.id.sample_checkbox_dataSuccess);
        mCheckBoxHasMore = (CheckBox) findViewById(R.id.sample_checkbox_hasMore);
        mEmpty = (Button) findViewById(R.id.sample_empty);
        mEmptyRetry = (Button) findViewById(R.id.sample_empty_retry);
        mListView = (ListView) findViewById(R.id.sample_listview);
        for(int i = 0 ; i < 17 ; i++){
        	if(i < 8){
        		mListViewData.add("ListView Item " + i);
        	}
        	mRecyclerViewData.add("RecyclerView Item " + i);
        }
        mListViewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mListViewData);
        mListView.setAdapter(mListViewAdapter);
        mListViewContainer = (DataStateContainer) findViewById(R.id.listview_dataStateContainer);
        mListViewContainer.setCallBack(new CallBack() {
			@Override
			public void onRefresh() {
				//toast("onRefresh");
				mListViewContainer.postDelayed(new Runnable() {
					@Override
					public void run() {
						///toast("onRefreshEnd");
						if(dataSuccess()){
							mListViewData.add(0 , "ListView Item Refresh " + mListViewData.size());
							mListViewData.add(0 , "ListView Item Refresh " + mListViewData.size());
							mListViewAdapter.notifyDataSetChanged();
						}
						mListViewContainer.onRefreshEnd(dataSuccess());
					}
				}, FAKE_DELAY);
			}
			
			@Override
			public void onLoad() {
				//toast("onLoad");
				mListViewContainer.postDelayed(new Runnable() {
					@Override
					public void run() {
						//toast("onLoadEnd");
						if(dataSuccess()){
							mListViewData.add("ListView Item Load " + mListViewData.size());
							mListViewData.add("ListView Item Load " + mListViewData.size());
							mListViewAdapter.notifyDataSetChanged();
						}
						mListViewContainer.onLoadEnd(dataSuccess());
					}
				}, FAKE_DELAY);
			}

			@Override
			public boolean autoLoadWhenScrollToLastItem() {
				return autoLoad();
			}

			@Override
			public boolean hasMoreDataToLoad() {
				return hasMore();
			}
		});
        
        
        mLLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mGLayoutManager = new GridLayoutManager(this, 2);
        mSLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
		mRecyclerView = (RecyclerView) findViewById(R.id.sample_recyclerview);
		mRecyclerView.setLayoutManager(mLLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerViewAdapter= new SimpleRecyclerViewAdapter(mRecyclerViewData);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerViewContainer = (DataStateContainer) findViewById(R.id.recyclerview_dataStateContainer);
        mRecyclerViewContainer.setCallBack(new CallBack() {
			@Override
			public void onRefresh() {
				//toast("onRefresh");
				mRecyclerViewContainer.postDelayed(new Runnable() {
					@Override
					public void run() {
						///toast("onRefreshEnd");
						if(dataSuccess()){
							mRecyclerViewData.add(0 , "Recycler Item Refresh " + mRecyclerViewData.size());
							mRecyclerViewData.add(0 , "Recycler Item Refresh " + mRecyclerViewData.size());
							if(mRecyclerView.getLayoutManager() == mGLayoutManager){
								mRecyclerViewData.add(0, "Recycler Item Refresh " + mRecyclerViewData.size());
							}else if(mRecyclerView.getLayoutManager() == mSLayoutManager){
								mRecyclerViewData.add(0 , "Recycler \nItem \nLoad " + mRecyclerViewData.size()+ "\nStaggeredGrid");
							}
							mRecyclerViewAdapter.notifyDataSetChanged();
						}
						mRecyclerViewContainer.onRefreshEnd(dataSuccess());
					}
				}, FAKE_DELAY);
			}
			
			@Override
			public void onLoad() {
				//toast("onLoad");
				mRecyclerViewContainer.postDelayed(new Runnable() {
					@Override
					public void run() {
						//toast("onLoadEnd");
						if(dataSuccess()){
							mRecyclerViewData.add("Recycler Item Load " + mRecyclerViewData.size());
							mRecyclerViewData.add("Recycler Item Load " + mRecyclerViewData.size());
							if(mRecyclerView.getLayoutManager() == mGLayoutManager){
								mRecyclerViewData.add("Recycler Item Load " + mRecyclerViewData.size());
							}else if(mRecyclerView.getLayoutManager() == mSLayoutManager){
								mRecyclerViewData.add("Recycler \nItem \nLoad " + mRecyclerViewData.size()+ "\nStaggeredGrid");
							}
							mRecyclerViewAdapter.notifyDataSetChanged();
						}
						mRecyclerViewContainer.onLoadEnd(dataSuccess());
					}
				}, FAKE_DELAY);
			}

			@Override
			public boolean autoLoadWhenScrollToLastItem() {
				return autoLoad();
			}

			@Override
			public boolean hasMoreDataToLoad() {
				return hasMore();
			}
		});
        
        mEmpty.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setEmpty(false);
			}
		});
        mEmptyRetry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setEmpty(true);
			}
		});
        mRadioGroup = (RadioGroup) findViewById(R.id.sample_radiogroup);
        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == R.id.sample_radio_listview){
					mListViewContainer.setVisibility(View.VISIBLE);
					mRecyclerViewContainer.setVisibility(View.GONE);
				}else{
					mListViewContainer.setVisibility(View.GONE);
					mRecyclerViewContainer.setVisibility(View.VISIBLE);
				}
			}
		});
        findViewById(R.id.sample_changelayoutmanager).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final LayoutManager currLayoutManager = mRecyclerView.getLayoutManager();
				if(currLayoutManager == mLLayoutManager){
					mRecyclerView.setLayoutManager(mGLayoutManager);
				}else if(currLayoutManager == mGLayoutManager){
					mRecyclerView.setLayoutManager(mSLayoutManager);
				}else if(currLayoutManager == mSLayoutManager){
					mRecyclerView.setLayoutManager(mLLayoutManager);
				}
				toast("change to " + mRecyclerView.getLayoutManager().getClass().getSimpleName());
			}
		});
        mRecyclerViewContainer.post(new Runnable() {
			@Override
			public void run() {
				mRecyclerViewContainer.requestRefresh();
			}
		});
    }
    
    private void setEmpty(boolean retry){
    	mListViewData.clear();
		mListViewAdapter.notifyDataSetChanged();
		mListViewContainer.onEmpty(true, retry);
		
		mRecyclerViewData.clear();
		mRecyclerViewAdapter.notifyDataSetChanged();
		mRecyclerViewContainer.onEmpty(true, retry);
    }
    
    private boolean hasMore(){
    	return mCheckBoxHasMore.isChecked();
    }
    private boolean autoLoad(){
    	return mCheckBoxAutoLoad.isChecked();
    }
    private boolean dataSuccess(){
    	return mCheckBoxDataSuccess.isChecked();
    }
    private void toast(String text){
    	Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    
    private static  class SimpleViewHolder extends ViewHolder{
    	TextView textView;
		public SimpleViewHolder(TextView view) {
			super(view);
			textView = view;
			textView.setBackgroundResource(R.drawable.common_background);
			this.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(v.getContext(), "RecyclerView " + 
							SimpleViewHolder.this.getLayoutPosition(), Toast.LENGTH_SHORT).show();
				}
			});
		}
    }
		
    private static class SimpleRecyclerViewAdapter extends RecyclerView.Adapter<SimpleViewHolder>{
    	
    	final ArrayList<String>  data;
    	public SimpleRecyclerViewAdapter(ArrayList<String> data) {
			super();
			this.data = data;
		}

		@Override
		public int getItemCount() {
			return data.size();
		}

		@Override
		public void onBindViewHolder(SimpleViewHolder holder, int position) {
			holder.textView.setText(data.get(position));
		}

		@Override
		public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			TextView textView = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_recyclerview_item, parent, false);
			return new SimpleViewHolder(textView);
		}
    }
    
    public class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private final int[] ATTRS = new int[]{
                android.R.attr.listDivider
        };

        public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

        public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

        private Drawable mDivider;

        private int mOrientation;

        public DividerItemDecoration(Context context, int orientation) {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            mDivider = a.getDrawable(0);
            a.recycle();
            setOrientation(orientation);
        }

        public void setOrientation(int orientation) {
            if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
                throw new IllegalArgumentException("invalid orientation");
            }
            mOrientation = orientation;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent) {
            if (mOrientation == VERTICAL_LIST) {
                drawVertical(c, parent);
            } else {
                drawHorizontal(c, parent);
            }
        }

        public void drawVertical(Canvas c, RecyclerView parent) {
//            final int left = parent.getPaddingLeft();
//            final int right = parent.getWidth() - parent.getPaddingRight();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final int left = child.getLeft();
                final int right = child.getRight();
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin +
                        Math.round(ViewCompat.getTranslationY(child));
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        public void drawHorizontal(Canvas c, RecyclerView parent) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int left = child.getRight() + params.rightMargin +
                        Math.round(ViewCompat.getTranslationX(child));
                final int right = left + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            if (mOrientation == VERTICAL_LIST) {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            } else {
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
            }
        }
    }


}
