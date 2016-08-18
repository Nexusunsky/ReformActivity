package com.nexusunsky.liuhao.reformtheactivity.tab;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nexusunsky.liuhao.reformtheactivity.MainActivity;
import com.nexusunsky.liuhao.reformtheactivity.R;
import com.nexusunsky.liuhao.reformtheactivity.commonlist.ActivityRecycler;
import com.nexusunsky.liuhao.reformtheactivity.commonlist.LoadingIndicatorView;
import com.nexusunsky.liuhao.reformtheactivity.commonlist.RecyclerHeaderFooterAdapterWrapper;
import com.nexusunsky.liuhao.reformtheactivity.model.ItemModel;
import com.nexusunsky.liuhao.reformtheactivity.util.AppToast;
import com.nexusunsky.liuhao.reformtheactivity.util.NetworkUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author liuhao
 * @time 16/8/11 下午7:06
 * @des ${TODO}
 */
public class ViewHolderPagerAdapter extends PagerAdapter {

    private List<String> datas = null; //显示的数据
    private LinkedList<View> mViewCache = null;
    private Context mContext;
    private LayoutInflater mLayoutInflater = null;

    private static final String TAG = "LiuHao";

    /**
     * 服务器端一共多少条数据
     */
    private static final int TOTAL_COUNTER = 34;

    /**
     * 每一页展示多少条数据
     */
    private static final int REQUEST_COUNT = 10;

    /**
     * 已经获取到多少条数据了
     */
    private static int mCurrentCounter = 0;

    private DataRecyclerAdapter mDataAdapter = null;
    private PreviewHandler mHandler;
    private RecyclerHeaderFooterAdapterWrapper mRecyclerHeaderFooterAdapterWrapper = null;

    private boolean isRefresh = false;
    private ViewHolderContent mHolderContent;

    public ViewHolderPagerAdapter(
            List<String> datas,
            Context context,
            DataRecyclerAdapter dataAdapter) {
        super();
        this.datas = datas;
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(mContext);
        this.mViewCache = new LinkedList<>();
        this.mHandler = new PreviewHandler((MainActivity) mContext);
        this.mDataAdapter = dataAdapter;
    }

    @Override
    public int getCount() {
        Log.e("test", "getCount ");
        return this.datas.size();
    }

    @Override
    public int getItemPosition(Object object) {
        Log.e("test", "getItemPosition ");
        return super.getItemPosition(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.e("test", "instantiateItem " + position);

        View convertView = null;
        if (mViewCache.size() == 0) {
            convertView = mLayoutInflater.inflate(R.layout.viewadapter_item_layout, null, false);

            TextView textView = (TextView) convertView.findViewById(R.id.view_pager_item_tv_tab);
            ActivityRecycler recyclerView = (ActivityRecycler)
                    convertView.findViewById(R.id.view_pager_item_rv_list);

            mHolderContent = new ViewHolderContent();
            mHolderContent.mTextView = textView;
            mHolderContent.mRecyclerView = recyclerView;

            convertView.setTag(mHolderContent);
        } else {
            convertView = mViewCache.removeFirst();
            mHolderContent = (ViewHolderContent) convertView.getTag();
        }

        initListContent();

        mHolderContent.mTextView.setText(datas.get(position));
        mHolderContent.mTextView.setTextColor(Color.YELLOW);
        mHolderContent.mTextView.setBackgroundColor(Color.GRAY);


        container.addView(
                convertView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        return convertView;
    }

    /**
     * 初始化列表视图界面的数据内容
     */
    private void initListContent() {

        //init data
        ArrayList<ItemModel> dataList = new ArrayList<>();

        for (int i = 0; i < 11; i++) {
            ItemModel item = new ItemModel();
            item.id = i;
            item.title = "item" + i;
            //            dataList.add(item);
        }

        mCurrentCounter = dataList.size();

        mDataAdapter.addAll(dataList);

        mRecyclerHeaderFooterAdapterWrapper =
                new RecyclerHeaderFooterAdapterWrapper(mContext, mDataAdapter);

        mHolderContent.mRecyclerView.setAdapter(mRecyclerHeaderFooterAdapterWrapper);
        mHolderContent.mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mHolderContent.mRecyclerView.setRefreshProgressStyle(
                LoadingIndicatorView.RecyclerLoaderAnimator.PROGRESSSTYLE.BallSpinFadeLoader);
        mHolderContent.mRecyclerView.setArrowImageView(R.drawable.iconfont_downgrey);

        //add a HeaderView
        ActivityRecycler.RecyclerHeadFooter.setHeaderView(mHolderContent.mRecyclerView, new
                ActivityRecycler.RecyclerCommonHeader(mContext));

        //add a FooterView
        //        RecyclerHeadFooter.setFooterView(mHolderContent.mRecyclerView, new SampleFooter
        // (mContext));

        mHolderContent.mRecyclerView.setRecyclerScrollListener(new ActivityRecycler
                .RecyclerScrollListener() {
            @Override
            public void onRefresh() {
                isRefresh = true;
                requestData();
            }

            @Override
            public void onScrollUp() {
            }

            @Override
            public void onScrollDown() {
            }

            @Override
            public void onBottom() {
                ActivityRecycler.RecyclerLoadingFooter.State state =
                        ActivityRecycler.RecyclerFooterContror.getFooterViewState(mHolderContent
                                .mRecyclerView);

                if (state == ActivityRecycler.RecyclerLoadingFooter.State.Loading) {
                    Log.d(TAG, "the state is Loading, just wait..");
                    return;
                }

                if (mCurrentCounter < TOTAL_COUNTER) {
                    // 底部加载更多数据
                    ActivityRecycler.RecyclerFooterContror.setFooterViewState(
                            (MainActivity) mContext,
                            mHolderContent.mRecyclerView,
                            REQUEST_COUNT,
                            ActivityRecycler.RecyclerLoadingFooter.State.Loading,
                            null);
                    requestData();

                } else {
                    //the end
                    ActivityRecycler.RecyclerFooterContror.setFooterViewState((MainActivity)
                                    mContext,
                            mHolderContent.mRecyclerView, REQUEST_COUNT, ActivityRecycler
                                    .RecyclerLoadingFooter.State
                                    .TheEnd, null);
                }
            }

            @Override
            public void onScrolled(int distanceX, int distanceY) {
            }

        });

        mHolderContent.mRecyclerView.setRefreshing(true);

        mRecyclerHeaderFooterAdapterWrapper.setOnItemClickLitener(
                new ActivityRecycler.OnItemClickLitener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        ItemModel item = mDataAdapter.getDataList().get(position);
                        AppToast.showShortText(mContext, item.title);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        ItemModel item = mDataAdapter.getDataList().get(position);
                        AppToast.showShortText(mContext, "onItemLongClick - " + item.title);
                    }
                });
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.e("test", "destroyItem " + position);
        View contentView = (View) object;
        container.removeView(contentView);
        this.mViewCache.add(contentView);
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        Log.e("test", "isViewFromObject ");
        if (view == null || o == null)
            return false;
        return view == o;
    }

    /**
     * Tab中内容页面需要显示的ViewHolder
     */
    public class ViewHolderContent {
        public TextView mTextView;
        public ActivityRecycler mRecyclerView;
    }

    /**
     * Tab中Tab栏目的标题
     */
    final class ViewHolderTab {

        TextView tv;
        View reminder;
        ImageView img;

    }

    ViewHolderTab mVHTab;
    View mTabItem;
    private String tabTitles[] = new String[]{"热门", "深圳", "专属", "抽奖", "健康", "专属"};

    public View getTabView(int position) {

        if (mVHTab == null || mVHTab != mTabItem.getTag(R.layout.activities_tab + position)) {

            mTabItem = LayoutInflater.from(mContext).inflate(R.layout.activities_tab, null);

            mVHTab = new ViewHolderTab();
            mVHTab.tv = (TextView) mTabItem.findViewById(R.id.textView);
            mVHTab.reminder = mTabItem.findViewById(R.id.reminder);
            mVHTab.img = (ImageView) mTabItem.findViewById(R.id.imageView);

            mTabItem.setTag(R.layout.activities_tab + position, mVHTab);
        }

        mVHTab.tv.setText(tabTitles[position]);

        return mTabItem;
    }

    public void setIndicVisibility(boolean visibility) {

        mVHTab.img.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
        mVHTab.img.invalidate();

    }

    private void notifyDataChanged() {
        mRecyclerHeaderFooterAdapterWrapper.notifyDataSetChanged();
    }

    private void addItems(ArrayList<ItemModel> list) {

        mDataAdapter.addAll(list);
        mCurrentCounter += list.size();

    }

    private View.OnClickListener mFooterClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ActivityRecycler.RecyclerFooterContror.setFooterViewState(
                    (MainActivity) mContext,
                    mHolderContent.mRecyclerView,
                    REQUEST_COUNT,
                    ActivityRecycler.RecyclerLoadingFooter.State.Loading,
                    null);
            requestData();
        }
    };

    /**
     * 模拟请求网络
     */
    private void requestData() {
        Log.d(TAG, "requestData");
        new Thread() {

            @Override
            public void run() {
                super.run();

                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //模拟一下网络请求失败的情况
                if (NetworkUtils.isNetAvailable(mContext)) {
                    mHandler.sendEmptyMessage(-1);
                } else {
                    mHandler.sendEmptyMessage(-3);
                }
            }
        }.start();
    }

    public static abstract class DataRecyclerAdapter
            extends ActivityRecycler.BaseRecyclerAdapter<ItemModel> {

        protected LayoutInflater mLayoutInflater;
        protected int mResLayoutID;

        /**
         * RecylerView中显示内容的ItemHolder
         */
        public static class BaseRecylerItemHolder extends RecyclerView.ViewHolder {
            public BaseRecylerItemHolder(View itemView) {
                super(itemView);
            }
        }

        public DataRecyclerAdapter(Context context, int resLayoutID) {
            mLayoutInflater = LayoutInflater.from(context);
            mContext = context;
            mResLayoutID = resLayoutID;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = mLayoutInflater.inflate(mResLayoutID, parent, false);
            return getViewHolder(inflate, viewType);
        }

        /**
         * @param inflate  初始化出来的根布局
         * @param viewType 根据{@link RecyclerView# getItemViewType(int)}中的Item类型返回相应的Item
         * @see RecyclerView# getItemViewType(int)
         */
        @NonNull
        protected abstract RecyclerView.ViewHolder getViewHolder(View inflate, int viewType);

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ItemModel item = mDataList.get(position);
            BaseRecylerItemHolder baseRecylerItemHolder = (BaseRecylerItemHolder) holder;
            initDataOfHolder(item, baseRecylerItemHolder);
        }

        /**
         * 实现数据适配给Viewholder
         *
         * @param item                  获取到的每个Model数据模型
         * @param baseRecylerItemHolder 用于显示数据的ViewHolder
         */
        protected abstract void initDataOfHolder(
                ItemModel item,
                BaseRecylerItemHolder baseRecylerItemHolder);
    }

    /**
     * 处理业务逻辑和界面的Handler
     */
    private class PreviewHandler extends Handler {

        private WeakReference<MainActivity> ref;

        PreviewHandler(MainActivity activity) {
            ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MainActivity activity = ref.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            switch (msg.what) {

                case -1:
                    if (isRefresh) {
                        mDataAdapter.clear();
                        mCurrentCounter = 0;
                    }

                    int currentSize = mDataAdapter.getItemCount();

                    //模拟组装10个数据
                    ArrayList<ItemModel> newList = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        if (newList.size() + currentSize >= TOTAL_COUNTER) {
                            break;
                        }

                        ItemModel item = new ItemModel();
                        item.id = currentSize + i;
                        item.title = "item" + (item.id);

                        newList.add(item);
                    }


                    addItems(newList);

                    if (isRefresh) {
                        isRefresh = false;
                        mHolderContent.mRecyclerView.refreshComplete();
                        notifyDataChanged();
                    } else {
                        ActivityRecycler.RecyclerFooterContror.setFooterViewState(
                                mHolderContent.mRecyclerView,
                                ActivityRecycler.RecyclerLoadingFooter.State.Normal);
                    }

                    break;
                case -2:
                    notifyDataChanged();
                    break;
                case -3:
                    if (isRefresh) {
                        isRefresh = false;
                        mHolderContent.mRecyclerView.refreshComplete();
                        notifyDataChanged();
                    } else {
                        ActivityRecycler.RecyclerFooterContror.setFooterViewState(
                                activity,
                                mHolderContent.mRecyclerView, REQUEST_COUNT,
                                ActivityRecycler.RecyclerLoadingFooter.State.NetWorkError,
                                mFooterClick);
                    }
                    break;
                case -4:
                    int index = mDataAdapter.getDataList().size();
                    mDataAdapter.getDataList().remove(0);
                    mDataAdapter.getDataList().remove(1);
                    mDataAdapter.notifyItemRangeRemoved(0, 2);

                    break;
                default:
                    break;
            }
        }
    }

}
