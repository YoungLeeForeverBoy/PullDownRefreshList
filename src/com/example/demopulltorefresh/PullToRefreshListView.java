package com.example.demopulltorefresh;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PullToRefreshListView extends ListView implements OnScrollListener{
    public final static String TAG = "PullToRefreshListView";

    public final static int STATE_NORMAL = 0;
    public final static int STATE_PULL_TO_REFRESH = 1;
    public final static int STATE_RELEASE_TO_REFRESH = 2;
    public final static int STATE_REFRESHING = 3;

    private View mHeaderView;
    private ProgressBar mRefreshingBar;
    private ImageView mArrowImgView;
    private TextView mPromptTv;
    private TextView mLastUpdateTv;

    private RotateAnimation mFlipAnim;
    private RotateAnimation mReverseAnim;

    private OnClickRefreshListener onClickRefreshListener;
    private OnRefreshListener onRefreshListener;
    private OnScrollListener onScrollListener;

    private int refreshState = STATE_NORMAL;
    private int scrollState = OnScrollListener.SCROLL_STATE_IDLE;
    private int headerViewOriginalTopPadding = 0;
    private int headerViewHeight = 0;
    private boolean isBack = false;
    private boolean isRecord = false;
    private int firstVisibleItem = -1;
    private int lastMotionY = -1;

    public PullToRefreshListView(Context context) {
        super(context);
        init(context);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mFlipAnim = new RotateAnimation(0, 180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnim.setFillAfter(true);
        mFlipAnim.setDuration(250);
        mFlipAnim.setInterpolator(new LinearInterpolator());
        mReverseAnim = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseAnim.setFillAfter(true);
        mReverseAnim.setDuration(250);
        mReverseAnim.setInterpolator(new LinearInterpolator());

        mHeaderView = LayoutInflater.from(context).inflate(
                R.layout.layout_pull_to_refresh_header, this, false);
        mRefreshingBar = (ProgressBar)mHeaderView.findViewById(R.id.refresh_bar);
        mArrowImgView = (ImageView)mHeaderView.findViewById(R.id.arrow_imgview);
        mPromptTv = (TextView)mHeaderView.findViewById(R.id.prompt_tv);
        mLastUpdateTv = (TextView)mHeaderView.findViewById(R.id.last_update_tv);

        headerViewOriginalTopPadding = mHeaderView.getPaddingTop();
        addHeaderView(mHeaderView);
        refreshState = STATE_NORMAL;
        measureView(mHeaderView);
        headerViewHeight = mHeaderView.getMeasuredHeight();

        mHeaderView.setMinimumHeight(70);

        //call super.setOnScrollListener() but not itself
        super.setOnScrollListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setSelection(1);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        setSelection(1);
    }

    public void setLastUpdate(String date) {
        if(date != null && !TextUtils.isEmpty(date)) {
            mLastUpdateTv.setVisibility(View.VISIBLE);
            mLastUpdateTv.setText(date);
        } else {
            mLastUpdateTv.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch(ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if(refreshState != STATE_REFRESHING &&
                        firstVisibleItem == 0 && !isRecord) {
                    lastMotionY = (int)ev.getY();
                    isRecord = true;
                }
            }break;

            case MotionEvent.ACTION_MOVE: {
                if(refreshState != STATE_REFRESHING &&
                        firstVisibleItem == 0 && !isRecord) {
                    lastMotionY = (int)ev.getY();
                    isRecord = true;
                }

                //for smooth, do not care the refreshState, just set top padding of
                //mHeaderView, the refreshState we can change it at onScroll()
                if(firstVisibleItem == 0 && isRecord &&
                        ((int)ev.getY() - lastMotionY >= headerViewHeight)) {
                    mHeaderView.setPadding(mHeaderView.getPaddingLeft(),
                            (int)ev.getY() - lastMotionY - headerViewHeight,
                            mHeaderView.getPaddingRight(), mHeaderView.getPaddingBottom());
                }
            }break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if(refreshState == STATE_RELEASE_TO_REFRESH) {
                    isRecord = false;
                    lastMotionY = -1;

                    prepareRefreshing();
                    onRefresh();
                } else if(firstVisibleItem == 0 && refreshState != STATE_REFRESHING) {
                    setSelection(1);
                    resetHeaderView();
                }

                if(!isVerticalScrollBarEnabled())
                    setVerticalScrollBarEnabled(true);
            }break;
        }

        return super.onTouchEvent(ev);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        this.firstVisibleItem = firstVisibleItem;

        if(refreshState != STATE_REFRESHING &&
                scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            if(firstVisibleItem == 0) {
                if(mHeaderView.getBottom() >= headerViewHeight + 20 &&
                        refreshState != STATE_RELEASE_TO_REFRESH) {
                    mArrowImgView.clearAnimation();
                    mArrowImgView.startAnimation(mFlipAnim);
                    mPromptTv.setText(R.string.prompt_release_to_refresh);

                    refreshState = STATE_RELEASE_TO_REFRESH;

                    if(isVerticalScrollBarEnabled())
                        setVerticalScrollBarEnabled(false);
                } else if(mHeaderView.getBottom() > 0 &&
                        mHeaderView.getBottom() < headerViewHeight + 20) {
                    if(refreshState == STATE_RELEASE_TO_REFRESH) {
                        mArrowImgView.clearAnimation();
                        mArrowImgView.startAnimation(mReverseAnim);
                    }
                    mPromptTv.setText(R.string.prompt_pull_to_refresh);

                    refreshState = STATE_PULL_TO_REFRESH;
                }
            } else {
                resetHeaderView();
            }
        } else if(firstVisibleItem == 0 &&
                scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            setSelection(1);
        }

        if(onScrollListener != null)
            onScrollListener.onScroll(view, firstVisibleItem,
                    visibleItemCount, totalItemCount);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollState = scrollState;

        if(onScrollListener != null)
            onScrollListener.onScrollStateChanged(view, scrollState);
    }

    private void resetHeaderView() {
        if(refreshState != STATE_NORMAL) {
            refreshState = STATE_NORMAL;

            resetHeaderPadding();

            mPromptTv.setText(R.string.prompt_pull_to_refresh);
            mArrowImgView.clearAnimation();
            mLastUpdateTv.setVisibility(View.INVISIBLE);
            mRefreshingBar.setVisibility(View.GONE);
        }
    }

    private void resetHeaderPadding() {
        mHeaderView.setPadding(
                mHeaderView.getPaddingLeft(), headerViewOriginalTopPadding,
                mHeaderView.getPaddingRight(), mHeaderView.getPaddingBottom());
    }

    private void prepareRefreshing() {
        resetHeaderPadding();

        mArrowImgView.setVisibility(View.GONE);
        mArrowImgView.clearAnimation();
        mRefreshingBar.setVisibility(View.VISIBLE);
        mLastUpdateTv.setVisibility(View.GONE);
        mPromptTv.setText(R.string.prompt_refreshing);

        refreshState = STATE_REFRESHING;
    }

    public void measureView(View child) {
        ViewGroup.LayoutParams params = child.getLayoutParams();
        if(params == null) {
            params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, params.width);
        int childHeightSpec = 0;
        if(params.height > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(
                    params.height, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(
                    0, MeasureSpec.UNSPECIFIED);
        }

        child.measure(childWidthSpec, childHeightSpec);
    }

    private void onRefresh() {
        if(onRefreshListener != null)
            onRefreshListener.onRefresh();
    }

    public void onRefreshComplete() {
        mHeaderView.setPadding(
                mHeaderView.getPaddingLeft(), headerViewOriginalTopPadding,
                mHeaderView.getPaddingRight(), mHeaderView.getPaddingBottom());
        mHeaderView.invalidate();

        mArrowImgView.setVisibility(View.VISIBLE);
        mArrowImgView.clearAnimation();
        mLastUpdateTv.setVisibility(View.INVISIBLE);
        mPromptTv.setText(R.string.prompt_pull_to_refresh);
        mRefreshingBar.setVisibility(View.GONE);

        refreshState = STATE_NORMAL;

        if(firstVisibleItem == 0) {
            //attention, run invalidateViews() before setSlecltion(1)
            invalidateViews();
            setSelection(1);
        }
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        onScrollListener = l;
    }

    public void setOnClickRefreshListener(OnClickRefreshListener listener) {
        if(listener != null)
            onClickRefreshListener = listener;
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        if(listener !=null)
            onRefreshListener = listener;
    }

    public interface OnRefreshListener {
        public void onRefresh();
    }

    public interface OnClickRefreshListener {
        public void onClick(View v);
    }
}
