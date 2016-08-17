package com.nexusunsky.liuhao.reformtheactivity.commonlist;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nexusunsky.liuhao.reformtheactivity.R;
import com.nexusunsky.liuhao.reformtheactivity.model.ItemModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by LiuHao on 16/1/4.
 */
public class ActivityRecycler extends RecyclerView {

    private boolean pullRefreshEnabled = true;
    private RecyclerScrollListener mRecyclerScrollListener;
    private RecyclerRefreshHeader mRefreshHeader;
    private View mEmptyView;
    private final RecyclerView.AdapterDataObserver mDataObserver = new DataObserver();
    private float mLastY = -1;
    private static final float DRAG_RATE = 3;

    private RecyclerHeaderFooterAdapterWrapper mWrapAdapter;
    private boolean isNoMore = false;

    //scroll variables begin
    /**
     * 当前RecyclerView类型
     */
    protected LayoutManagerType layoutManagerType;

    /**
     * 最后一个的位置
     */
    private int[] lastPositions;

    /**
     * 最后一个可见的item的位置
     */
    private int lastVisibleItemPosition;

    /**
     * 当前滑动的状态
     */
    private int currentScrollState = 0;

    /**
     * 触发在上下滑动监听器的容差距离
     */
    private static final int HIDE_THRESHOLD = 20;

    /**
     * 滑动的距离
     */
    private int mDistance = 0;

    /**
     * 是否需要监听控制
     */
    private boolean mIsScrollDown = true;

    /**
     * Y轴移动的实际距离（最顶部为0）
     */
    private int mScrolledYDistance = 0;

    /**
     * X轴移动的实际距离（最左侧为0）
     */
    private int mScrolledXDistance = 0;

    public ActivityRecycler(Context context) {
        this(context, null);
    }

    public ActivityRecycler(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActivityRecycler(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        Adapter oldAdapter = getAdapter();
        if (oldAdapter != null && mDataObserver != null) {
            oldAdapter.unregisterAdapterDataObserver(mDataObserver);
        }
        super.setAdapter(adapter);
        adapter.registerAdapterDataObserver(mDataObserver);
        mDataObserver.onChanged();

        mWrapAdapter = (RecyclerHeaderFooterAdapterWrapper) getAdapter();
        mRefreshHeader = mWrapAdapter.getRefreshHeader();

    }

    private class DataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            Adapter<?> adapter = getAdapter();

            if (adapter instanceof RecyclerHeaderFooterAdapterWrapper) {
                RecyclerHeaderFooterAdapterWrapper headerAndFooterAdapter =
                        (RecyclerHeaderFooterAdapterWrapper) adapter;
                if (headerAndFooterAdapter.getInnerAdapter() != null && mEmptyView != null) {
                    int count = headerAndFooterAdapter.getInnerAdapter().getItemCount();
                    if (count == 0) {
                        mEmptyView.setVisibility(View.VISIBLE);
                        ActivityRecycler.this.setVisibility(View.GONE);
                    } else {
                        mEmptyView.setVisibility(View.GONE);
                        ActivityRecycler.this.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (adapter != null && mEmptyView != null) {
                    if (adapter.getItemCount() == 0) {
                        mEmptyView.setVisibility(View.VISIBLE);
                        ActivityRecycler.this.setVisibility(View.GONE);
                    } else {
                        mEmptyView.setVisibility(View.GONE);
                        ActivityRecycler.this.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            mLastY = ev.getRawY();
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();

                if (isOnTop() && pullRefreshEnabled) {
                    mRefreshHeader.onMove(deltaY / DRAG_RATE);
                    if (mRefreshHeader.getVisibleHeight() > 0 &&
                            mRefreshHeader.getState() < RecyclerRefreshHeader.STATE_REFRESHING) {

                        return false;
                    }
                }

                break;
            default:
                mLastY = -1; // reset
                if (isOnTop() && pullRefreshEnabled) {
                    if (mRefreshHeader.releaseAction()) {
                        if (mRecyclerScrollListener != null) {
                            mRecyclerScrollListener.onRefresh();
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private int findMin(int[] firstPositions) {
        int min = firstPositions[0];
        for (int value : firstPositions) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    private boolean isOnTop() {
        if (mRefreshHeader.getParent() != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * set view when no content item
     *
     * @param emptyView visiable view when items is empty
     */
    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
    }

    public void refreshComplete() {
        mRefreshHeader.refreshComplete();
        setNoMore(false);
    }

    public void setNoMore(boolean noMore) {
        isNoMore = noMore;
    }

    public void setPullRefreshEnabled(boolean enabled) {
        pullRefreshEnabled = enabled;
    }

    public void setRefreshProgressStyle(int style) {
        if (mRefreshHeader != null) {
            mRefreshHeader.setProgressStyle(style);
        }
    }

    public void setArrowImageView(int resId) {
        if (mRefreshHeader != null) {
            mRefreshHeader.setArrowImageView(resId);
        }
    }

    public void setRecyclerScrollListener(RecyclerScrollListener listener) {
        mRecyclerScrollListener = listener;
    }

    /**
     * 滑动监听
     */
    public interface RecyclerScrollListener {

        void onRefresh();//pull down to refresh

        void onScrollUp();//scroll down to up

        void onScrollDown();//scroll from up to down

        void onBottom();//load next page

        void onScrolled(int distanceX, int distanceY);// moving state,you can get the move distance
    }

    /**
     * 点击监听
     */
    public interface OnItemClickLitener {

        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    /**
     * ActivityRecycler的核心Adapter
     */
    public abstract static class BaseRecyclerAdapter<T extends ItemModel>
            extends RecyclerView.Adapter {
        protected Context mContext;

        protected ArrayList<T> mDataList = new ArrayList<>();

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        public List<T> getDataList() {
            return mDataList;
        }

        public void setDataList(Collection<T> list) {
            this.mDataList.clear();
            this.mDataList.addAll(list);
            notifyDataSetChanged();
        }

        public void addAll(Collection<T> list) {
            int lastIndex = this.mDataList.size();
            if (this.mDataList.addAll(list)) {
                notifyItemRangeInserted(lastIndex, list.size());
            }
        }

        public void delete(int position) {
            mDataList.remove(position);
            notifyItemRemoved(position);
        }

        public void clear() {
            mDataList.clear();
            notifyDataSetChanged();
        }
    }

    private int mRefreshHeaderHeight;

    public void setRefreshing(boolean refreshing) {
        if (refreshing && pullRefreshEnabled && mRecyclerScrollListener != null) {
            mRefreshHeader.setRefreshState(RecyclerRefreshHeader.STATE_REFRESHING);
            mRefreshHeaderHeight = mRefreshHeader.getMeasuredHeight();
            mRefreshHeader.onMove(mRefreshHeaderHeight);
            mRecyclerScrollListener.onRefresh();
        }
    }

    public void forceToRefresh() {
        if (pullRefreshEnabled && mRecyclerScrollListener != null) {
            mRefreshHeader.setRefreshState(RecyclerRefreshHeader.STATE_REFRESHING);
            mRefreshHeader.onMove(mRefreshHeaderHeight);
            mRecyclerScrollListener.onRefresh();
        }
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        if (null != mRecyclerScrollListener) {
            int firstVisibleItemPosition = 0;
            RecyclerView.LayoutManager layoutManager = getLayoutManager();

            if (layoutManagerType == null) {
                if (layoutManager instanceof LinearLayoutManager) {
                    layoutManagerType = LayoutManagerType.LINEAR_LAYOUT;
                } else if (layoutManager instanceof GridLayoutManager) {
                    layoutManagerType = LayoutManagerType.GRID_LAYOUT;
                } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                    layoutManagerType = LayoutManagerType.STAGGERED_GRID_LAYOUT;
                } else {
                    throw new RuntimeException(
                            "Unsupported LayoutManager used. Valid ones are LinearLayoutManager, " +
                                    "GridLayoutManager and StaggeredGridLayoutManager");
                }
            }

            switch (layoutManagerType) {
                case LINEAR_LAYOUT:
                    firstVisibleItemPosition = ((LinearLayoutManager) layoutManager)
                            .findFirstVisibleItemPosition();
                    lastVisibleItemPosition = ((LinearLayoutManager) layoutManager)
                            .findLastVisibleItemPosition();
                    break;
                case GRID_LAYOUT:
                    firstVisibleItemPosition = ((GridLayoutManager) layoutManager)
                            .findFirstVisibleItemPosition();
                    lastVisibleItemPosition = ((GridLayoutManager) layoutManager)
                            .findLastVisibleItemPosition();
                    break;
                case STAGGERED_GRID_LAYOUT:
                    StaggeredGridLayoutManager staggeredGridLayoutManager =
                            (StaggeredGridLayoutManager) layoutManager;
                    if (lastPositions == null) {
                        lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                    }
                    staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                    lastVisibleItemPosition = findMax(lastPositions);
                    staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions
                            (lastPositions);
                    firstVisibleItemPosition = findMax(lastPositions);
                    break;
            }

            // 根据类型来计算出第一个可见的item的位置，由此判断是否触发到底部的监听器
            // 计算并判断当前是向上滑动还是向下滑动
            calculateScrollUpOrDown(firstVisibleItemPosition, dy);

            // 移动距离超过一定的范围，我们监听就没有啥实际的意义了
            mScrolledXDistance += dx;
            mScrolledYDistance += dy;
            mScrolledXDistance = (mScrolledXDistance < 0) ? 0 : mScrolledXDistance;
            mScrolledYDistance = (mScrolledYDistance < 0) ? 0 : mScrolledYDistance;
            if (mIsScrollDown && (dy == 0)) {
                mScrolledYDistance = 0;
            }
            //Be careful in here
            mRecyclerScrollListener.onScrolled(mScrolledXDistance, mScrolledYDistance);
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        currentScrollState = state;
        if (currentScrollState == RecyclerView.SCROLL_STATE_IDLE && mRecyclerScrollListener !=
                null) {
            RecyclerView.LayoutManager layoutManager = getLayoutManager();
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();

            if (visibleItemCount > 0
                    && lastVisibleItemPosition >= totalItemCount - 1
                    && totalItemCount > visibleItemCount
                    && !isNoMore
                    && !mIsScrollDown
                    && mRefreshHeader.getState() < RecyclerRefreshHeader.STATE_REFRESHING) {
                mRecyclerScrollListener.onBottom();
            }
        }
    }

    /**
     * 计算当前是向上滑动还是向下滑动
     */
    private void calculateScrollUpOrDown(int firstVisibleItemPosition, int dy) {
        if (firstVisibleItemPosition == 0) {
            if (!mIsScrollDown) {
                mIsScrollDown = true;
                mRecyclerScrollListener.onScrollDown();
            }
        } else {
            if (mDistance > HIDE_THRESHOLD && mIsScrollDown) {
                mIsScrollDown = false;
                mRecyclerScrollListener.onScrollUp();
                mDistance = 0;
            } else if (mDistance < -HIDE_THRESHOLD && !mIsScrollDown) {
                mIsScrollDown = true;
                mRecyclerScrollListener.onScrollDown();
                mDistance = 0;
            }
        }
        if ((mIsScrollDown && dy > 0) || (!mIsScrollDown && dy < 0)) {
            mDistance += dy;
        }
    }

    public enum LayoutManagerType {
        LINEAR_LAYOUT, STAGGERED_GRID_LAYOUT, GRID_LAYOUT
    }

    /**
     * 分页展示数据时，RecyclerView的FooterView State 操作工具类
     * RecyclerView一共有几种State：Normal/Loading/Error/TheEnd
     */
    public static class RecyclerFooterContror {

        /**
         * 设置headerAndFooterAdapter的FooterView State
         *
         * @param instance      context
         * @param recyclerView  recyclerView
         * @param pageSize      分页展示时，recyclerView每一页的数量
         * @param state         FooterView State
         * @param errorListener FooterView处于Error状态时的点击事件
         */
        public static void setFooterViewState(Activity instance, RecyclerView recyclerView, int
                pageSize, RecyclerLoadingFooter.State state, View.OnClickListener errorListener) {
            if (instance == null || instance.isFinishing()) {
                return;
            }

            RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();

            if (outerAdapter == null || !(outerAdapter instanceof
                    RecyclerHeaderFooterAdapterWrapper)) {
                return;
            }

            RecyclerHeaderFooterAdapterWrapper headerAndFooterAdapter =
                    (RecyclerHeaderFooterAdapterWrapper) outerAdapter;

            //只有一页的时候，就别加什么FooterView了
            if (headerAndFooterAdapter.getInnerAdapter().getItemCount() < pageSize) {
                return;
            }

            RecyclerLoadingFooter footerView;

            //已经有footerView了
            if (headerAndFooterAdapter.getFooterViewsCount() > 0) {
                footerView = (RecyclerLoadingFooter) headerAndFooterAdapter.getFooterView();
                footerView.setState(state);

                if (state == RecyclerLoadingFooter.State.NetWorkError) {
                    footerView.setOnClickListener(errorListener);
                } else if (state == RecyclerLoadingFooter.State.TheEnd) {
                    ((ActivityRecycler) recyclerView).setNoMore(true);
                }

                recyclerView.scrollToPosition(headerAndFooterAdapter.getItemCount() - 1);
            } else {
                footerView = new RecyclerLoadingFooter(instance);
                footerView.setState(state);

                if (state == RecyclerLoadingFooter.State.NetWorkError) {
                    footerView.setOnClickListener(errorListener);
                } else if (state == RecyclerLoadingFooter.State.TheEnd) {
                    ((ActivityRecycler) recyclerView).setNoMore(true);
                }

                headerAndFooterAdapter.addFooterView(footerView);
                recyclerView.scrollToPosition(headerAndFooterAdapter.getItemCount() - 1);
            }
        }

        /**
         * 设置headerAndFooterAdapter的FooterView State
         *
         * @param instance      context
         * @param recyclerView  recyclerView
         * @param pageSize      分页展示时，recyclerView每一页的数量
         * @param state         FooterView State
         * @param errorListener FooterView处于Error状态时的点击事件
         */
        public static void setFooterViewState2(Activity instance, RecyclerView recyclerView, int
                pageSize, RecyclerLoadingFooter.State state, View.OnClickListener errorListener) {

            if (instance == null || instance.isFinishing()) {
                return;
            }

            RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();

            if (outerAdapter == null || !(outerAdapter instanceof
                    RecyclerHeaderFooterAdapterWrapper)) {
                return;
            }

            RecyclerHeaderFooterAdapterWrapper headerAndFooterAdapter =
                    (RecyclerHeaderFooterAdapterWrapper) outerAdapter;

            RecyclerLoadingFooter footerView;

            //已经有footerView了
            if (headerAndFooterAdapter.getFooterViewsCount() > 0) {
                footerView = (RecyclerLoadingFooter) headerAndFooterAdapter.getFooterView();
                footerView.setState(state);

                if (state == RecyclerLoadingFooter.State.NetWorkError) {
                    footerView.setOnClickListener(errorListener);
                }
                recyclerView.scrollToPosition(0);
            } else {
                footerView = new RecyclerLoadingFooter(instance);
                footerView.setState(state);

                if (state == RecyclerLoadingFooter.State.NetWorkError) {
                    footerView.setOnClickListener(errorListener);
                }

                headerAndFooterAdapter.addFooterView(footerView);
                recyclerView.scrollToPosition(0);
            }
        }

        /**
         * 获取当前RecyclerView.FooterView的状态
         *
         * @param recyclerView
         */
        public static RecyclerLoadingFooter.State getFooterViewState(RecyclerView recyclerView) {

            RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();
            if (outerAdapter != null && outerAdapter instanceof
                    RecyclerHeaderFooterAdapterWrapper) {
                if (((RecyclerHeaderFooterAdapterWrapper) outerAdapter).getFooterViewsCount() > 0) {
                    RecyclerLoadingFooter footerView = (RecyclerLoadingFooter) (
                            (RecyclerHeaderFooterAdapterWrapper) outerAdapter).getFooterView();
                    return footerView.getState();
                }
            }

            return RecyclerLoadingFooter.State.Normal;
        }

        /**
         * 设置当前RecyclerView.FooterView的状态
         *
         * @param recyclerView
         * @param state
         */
        public static void setFooterViewState(RecyclerView recyclerView, RecyclerLoadingFooter.State
                state) {
            RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();
            if (outerAdapter != null && outerAdapter instanceof
                    RecyclerHeaderFooterAdapterWrapper) {
                if (((RecyclerHeaderFooterAdapterWrapper) outerAdapter).getFooterViewsCount() > 0) {
                    RecyclerLoadingFooter footerView = (RecyclerLoadingFooter) (
                            (RecyclerHeaderFooterAdapterWrapper) outerAdapter).getFooterView();
                    footerView.setState(state);
                }
            }
        }
    }

    /**
     * RecyclerView设置Header/Footer
     */
    public static class RecyclerHeadFooter {

        /**
         * 设置HeaderView
         *
         * @param recyclerView
         * @param view
         */
        public static void setHeaderView(RecyclerView recyclerView, View view) {
            RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();

            if (outerAdapter == null || !(outerAdapter instanceof
                    RecyclerHeaderFooterAdapterWrapper)) {
                return;
            }

            RecyclerHeaderFooterAdapterWrapper headerAndFooterAdapter =
                    (RecyclerHeaderFooterAdapterWrapper) outerAdapter;
        /*if (headerAndFooterAdapter.getHeaderViewsCount() == 0) {
            headerAndFooterAdapter.addHeaderView(view);
        }*/
            headerAndFooterAdapter.addHeaderView(view);
        }

        /**
         * 设置FooterView
         *
         * @param recyclerView
         * @param view
         */
        public static void setFooterView(RecyclerView recyclerView, View view) {
            RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();

            if (outerAdapter == null || !(outerAdapter instanceof
                    RecyclerHeaderFooterAdapterWrapper)) {
                return;
            }

            RecyclerHeaderFooterAdapterWrapper headerAndFooterAdapter =
                    (RecyclerHeaderFooterAdapterWrapper) outerAdapter;
            if (headerAndFooterAdapter.getFooterViewsCount() == 0) {
                headerAndFooterAdapter.addFooterView(view);
            }
        }

        /**
         * 移除FooterView
         *
         * @param recyclerView
         */
        public static void removeFooterView(RecyclerView recyclerView) {

            RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();

            if (outerAdapter != null && outerAdapter instanceof
                    RecyclerHeaderFooterAdapterWrapper) {

                int footerViewCounter = ((RecyclerHeaderFooterAdapterWrapper) outerAdapter)
                        .getFooterViewsCount();
                if (footerViewCounter > 0) {
                    View footerView = ((RecyclerHeaderFooterAdapterWrapper) outerAdapter)
                            .getFooterView();
                    ((RecyclerHeaderFooterAdapterWrapper) outerAdapter).removeFooterView
                            (footerView);
                }
            }
        }

        /**
         * 移除HeaderView
         *
         * @param recyclerView
         */
        public static void removeHeaderView(RecyclerView recyclerView) {

            RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();

            if (outerAdapter != null && outerAdapter instanceof
                    RecyclerHeaderFooterAdapterWrapper) {

                int headerViewCounter = ((RecyclerHeaderFooterAdapterWrapper) outerAdapter)
                        .getHeaderViewsCount();
                if (headerViewCounter > 0) {
                    View headerView = ((RecyclerHeaderFooterAdapterWrapper) outerAdapter)
                            .getHeaderView();
                    ((RecyclerHeaderFooterAdapterWrapper) outerAdapter).removeFooterView
                            (headerView);
                }
            }
        }

        /**
         * 请使用本方法替代RecyclerView.ViewHolder的getLayoutPosition()方法
         *
         * @param recyclerView
         * @param holder
         * @return
         */
        public static int getLayoutPosition(RecyclerView recyclerView, RecyclerView.ViewHolder
                holder) {
            RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();
            if (outerAdapter != null && outerAdapter instanceof
                    RecyclerHeaderFooterAdapterWrapper) {

                int headerViewCounter = ((RecyclerHeaderFooterAdapterWrapper) outerAdapter)
                        .getHeaderViewsCount();
                if (headerViewCounter > 0) {
                    return holder.getLayoutPosition() - headerViewCounter;
                }
            }

            return holder.getLayoutPosition();
        }

        /**
         * 请使用本方法替代RecyclerView.ViewHolder的getAdapterPosition()方法
         *
         * @param recyclerView
         * @param holder
         * @return
         */
        public static int getAdapterPosition(RecyclerView recyclerView, RecyclerView.ViewHolder
                holder) {
            RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();
            if (outerAdapter != null && outerAdapter instanceof
                    RecyclerHeaderFooterAdapterWrapper) {

                int headerViewCounter = ((RecyclerHeaderFooterAdapterWrapper) outerAdapter)
                        .getHeaderViewsCount();
                if (headerViewCounter > 0) {
                    return holder.getAdapterPosition() - headerViewCounter;
                }
            }

            return holder.getAdapterPosition();
        }
    }

    /**
     * RecyclerView的CommonHeader
     */
    public static class RecyclerCommonHeader extends RelativeLayout {

        public RecyclerCommonHeader(Context context) {
            super(context);
            init(context);
        }

        public RecyclerCommonHeader(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }

        public RecyclerCommonHeader(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init(context);
        }

        public void init(Context context) {

            inflate(context, R.layout.sample_header, this);
        }
    }

    public static class RecyclerRefreshHeader extends LinearLayout
            implements RecyclerHeaderFooterAdapterWrapper.BaseRefreshHeader {

        private LinearLayout mContainer;
        private ImageView mArrowImageView;
        private RecyclerProgress mProgressBar;
        private TextView mStatusTextView;
        private int mState = STATE_NORMAL;

        private TextView mHeaderTimeView;

        private Animation mRotateUpAnim;
        private Animation mRotateDownAnim;

        private static final int ROTATE_ANIM_DURATION = 180;

        public int mMeasuredHeight;
        private static Context mContext;

        public RecyclerRefreshHeader(Context context) {
            super(context);
            initView();
        }

        /**
         * @param context
         * @param attrs
         */
        public RecyclerRefreshHeader(Context context, AttributeSet attrs) {
            super(context, attrs);
            initView();
        }

        private void initView() {
            mContext = getContext();

            // 初始情况，设置下拉刷新view高度为0
            mContainer = (LinearLayout) LayoutInflater.from(getContext())
                    .inflate(R.layout.listview_header, null);

            LayoutParams lp = new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);

            lp.setMargins(0, 0, 0, 0);
            this.setLayoutParams(lp);
            this.setPadding(0, 0, 0, 0);

            addView(mContainer, new LayoutParams(LayoutParams.MATCH_PARENT, 0));
            setGravity(Gravity.BOTTOM);

            mArrowImageView = (ImageView) findViewById(R.id.listview_header_arrow);
            mStatusTextView = (TextView) findViewById(R.id.refresh_status_textview);

            //init the progress view
            mProgressBar = (RecyclerProgress) findViewById(R.id.listview_header_progressbar);
            LoadingIndicatorView progressView = new LoadingIndicatorView(getContext());
            progressView.setIndicatorColor(0xffB5B5B5);
            progressView.setIndicatorId(LoadingIndicatorView.RecyclerLoaderAnimator.PROGRESSSTYLE
                    .BallSpinFadeLoader);
            mProgressBar.setView(progressView);


            mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
            mRotateUpAnim.setFillAfter(true);

            mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
            mRotateDownAnim.setFillAfter(true);

            mHeaderTimeView = (TextView) findViewById(R.id.last_refresh_time);
            measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mMeasuredHeight = getMeasuredHeight();
        }

        public void setProgressStyle(int style) {
            if (style == LoadingIndicatorView.RecyclerLoaderAnimator.PROGRESSSTYLE.SysProgress) {
                mProgressBar.setView(
                        new ProgressBar(getContext(), null, android.R.attr.progressBarStyle));
            } else {
                LoadingIndicatorView progressView = new LoadingIndicatorView(this.getContext());
                progressView.setIndicatorColor(0xffB5B5B5);
                progressView.setIndicatorId(style);
                mProgressBar.setView(progressView);
            }
        }

        public void setArrowImageView(int resid) {
            mArrowImageView.setImageResource(resid);
        }

        public void setRefreshState(int state) {
            if (state == mState)
                return;

            if (state == STATE_REFRESHING) {    // 显示进度
                mArrowImageView.clearAnimation();
                mArrowImageView.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);

            } else if (state == STATE_DONE) {

                mArrowImageView.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);

            } else {    // 显示箭头图片
                mArrowImageView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            switch (state) {
                case STATE_NORMAL:
                    if (mState == STATE_RELEASE_TO_REFRESH) {
                        mArrowImageView.startAnimation(mRotateDownAnim);
                    }
                    if (mState == STATE_REFRESHING) {
                        mArrowImageView.clearAnimation();
                    }
                    mStatusTextView.setText(R.string.listview_header_hint_normal);
                    break;
                case STATE_RELEASE_TO_REFRESH:
                    if (mState != STATE_RELEASE_TO_REFRESH) {
                        mArrowImageView.clearAnimation();
                        mArrowImageView.startAnimation(mRotateUpAnim);
                        mStatusTextView.setText(R.string.listview_header_hint_release);
                    }
                    break;
                case STATE_REFRESHING:
                    mStatusTextView.setText(R.string.refreshing);
                    break;
                case STATE_DONE:
                    mStatusTextView.setText(R.string.refresh_done);
                    break;
                default:
            }

            mState = state;
        }

        public int getState() {
            return mState;
        }

        @Override
        public void refreshComplete() {
            mHeaderTimeView.setText(friendlyTime(new Date()));
            setRefreshState(STATE_DONE);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    reset();
                }
            }, 200);
        }

        public void setVisibleHeight(int height) {
            if (height < 0)
                height = 0;
            LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
            lp.height = height;
            mContainer.setLayoutParams(lp);
        }

        public int getVisibleHeight() {
            LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
            return lp.height;
        }

        @Override
        public void onMove(float delta) {
            if (getVisibleHeight() > 0 || delta > 0) {
                setVisibleHeight((int) delta + getVisibleHeight());
                if (mState <= STATE_RELEASE_TO_REFRESH) { // 未处于刷新状态，更新箭头
                    if (getVisibleHeight() > mMeasuredHeight) {
                        setRefreshState(STATE_RELEASE_TO_REFRESH);
                    } else {
                        setRefreshState(STATE_NORMAL);
                    }
                }
            }
        }

        @Override
        public boolean releaseAction() {
            boolean isOnRefresh = false;
            int height = getVisibleHeight();
            if (height == 0) // not visible.
                isOnRefresh = false;

            if (getVisibleHeight() > mMeasuredHeight && mState < STATE_REFRESHING) {
                setRefreshState(STATE_REFRESHING);
                isOnRefresh = true;
            }
            // refreshing and header isn't shown fully. do nothing.
            if (mState == STATE_REFRESHING && height <= mMeasuredHeight) {
                //return;
            }
            int destHeight = 0; // default: scroll back to dismiss header.
            // is refreshing, just scroll back to show all the header.
            if (mState == STATE_REFRESHING) {
                destHeight = mMeasuredHeight;
            }
            smoothScrollTo(destHeight);

            return isOnRefresh;
        }

        public void reset() {
            smoothScrollTo(0);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    setRefreshState(STATE_NORMAL);
                }
            }, 500);
        }

        private void smoothScrollTo(int destHeight) {
            ValueAnimator animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight);
            animator.setDuration(300).start();
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setVisibleHeight((int) animation.getAnimatedValue());
                }
            });
            animator.start();
        }

        public static String friendlyTime(Date time) {
            int ct = (int) ((System.currentTimeMillis() - time.getTime()) / 1000);

            if (ct == 0) {
                return mContext.getResources().getString(R.string.text_just);
            }

            if (ct > 0 && ct < 60) {
                return ct + mContext.getResources().getString(R.string.text_seconds_ago);
            }

            if (ct >= 60 && ct < 3600) {
                return Math.max(ct / 60, 1) + mContext.getResources().getString(R.string
                        .text_minute_ago);
            }
            if (ct >= 3600 && ct < 86400)
                return ct / 3600 + mContext.getResources().getString(R.string.text_hour_ago);
            if (ct >= 86400 && ct < 2592000) { //86400 * 30
                int day = ct / 86400;
                return day + mContext.getResources().getString(R.string.text_day_ago);
            }
            if (ct >= 2592000 && ct < 31104000) { //86400 * 30
                return ct / 2592000 + mContext.getResources().getString(R.string.text_month_ago);
            }
            return ct / 31104000 + mContext.getResources().getString(R.string.text_year_ago);
        }
    }

    /**
     * ListView/GridView/RecyclerView 分页加载时使用到的FooterView
     */
    public static class RecyclerLoadingFooter extends RelativeLayout {

        protected State mState = State.Normal;
        private View mLoadingView;
        private View mNetworkErrorView;
        private View mTheEndView;
        private LoadingIndicatorView mLoadingProgress;
        private TextView mLoadingText;

        public RecyclerLoadingFooter(Context context) {
            super(context);
            init(context);
        }

        public RecyclerLoadingFooter(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }

        public RecyclerLoadingFooter(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init(context);
        }

        public void init(Context context) {

            inflate(context, R.layout.layout_recyclerview_list_footer, this);
            setOnClickListener(null);

            setState(State.Normal, true);
        }

        public State getState() {
            return mState;
        }

        public void setState(State status) {
            setState(status, true);
        }

        /**
         * 设置状态
         *
         * @param status
         * @param showView 是否展示当前View
         */
        public void setState(State status, boolean showView) {
            if (mState == status) {
                return;
            }
            mState = status;

            switch (status) {

                case Normal:
                    setOnClickListener(null);
                    if (mLoadingView != null) {
                        mLoadingView.setVisibility(GONE);
                    }

                    if (mTheEndView != null) {
                        mTheEndView.setVisibility(GONE);
                    }

                    if (mNetworkErrorView != null) {
                        mNetworkErrorView.setVisibility(GONE);
                    }

                    break;
                case Loading:
                    setOnClickListener(null);
                    if (mTheEndView != null) {
                        mTheEndView.setVisibility(GONE);
                    }

                    if (mNetworkErrorView != null) {
                        mNetworkErrorView.setVisibility(GONE);
                    }

                    if (mLoadingView == null) {
                        ViewStub viewStub = (ViewStub) findViewById(R.id.loading_viewstub);
                        mLoadingView = viewStub.inflate();

                        mLoadingProgress = (LoadingIndicatorView) mLoadingView.findViewById(R.id
                                .loading_progress);
                        mLoadingText = (TextView) mLoadingView.findViewById(R.id.loading_text);
                    } else {
                        mLoadingView.setVisibility(VISIBLE);
                    }

                    mLoadingView.setVisibility(showView ? VISIBLE : GONE);

                    mLoadingProgress.setVisibility(View.VISIBLE);
                    mLoadingText.setText(R.string.list_footer_loading);
                    break;
                case TheEnd:
                    setOnClickListener(null);
                    if (mLoadingView != null) {
                        mLoadingView.setVisibility(GONE);
                    }

                    if (mNetworkErrorView != null) {
                        mNetworkErrorView.setVisibility(GONE);
                    }

                    if (mTheEndView == null) {
                        ViewStub viewStub = (ViewStub) findViewById(R.id.end_viewstub);
                        mTheEndView = viewStub.inflate();
                    } else {
                        mTheEndView.setVisibility(VISIBLE);
                    }

                    mTheEndView.setVisibility(showView ? VISIBLE : GONE);
                    break;
                case NetWorkError:

                    if (mLoadingView != null) {
                        mLoadingView.setVisibility(GONE);
                    }

                    if (mTheEndView != null) {
                        mTheEndView.setVisibility(GONE);
                    }

                    if (mNetworkErrorView == null) {
                        ViewStub viewStub = (ViewStub) findViewById(R.id.network_error_viewstub);
                        mNetworkErrorView = viewStub.inflate();
                    } else {
                        mNetworkErrorView.setVisibility(VISIBLE);
                    }

                    mNetworkErrorView.setVisibility(showView ? VISIBLE : GONE);
                    break;
                default:

                    break;
            }
        }

        public enum State {
            Normal/**正常*/
            , TheEnd/**加载到最底了*/
            , Loading/**加载中..*/
            , NetWorkError/**网络异常*/
        }
    }

}
