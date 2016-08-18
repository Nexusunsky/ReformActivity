package com.nexusunsky.liuhao.reformtheactivity.activities.item;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nexusunsky.liuhao.reformtheactivity.R;
import com.nexusunsky.liuhao.reformtheactivity.model.ItemModel;
import com.nexusunsky.liuhao.reformtheactivity.tab.ViewHolderPagerAdapter;

/**
 * @author LiuHao
 * @time 16/8/16 上午10:50
 * @des ${TODO}
 */
public class ActivitiesPrizeItemAdapter extends ViewHolderPagerAdapter.DataRecyclerAdapter {

    public ActivitiesPrizeItemAdapter(Context context, int resLayoutID) {
        super(context, resLayoutID);
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder getViewHolder(View inflate, int viewType) {
        ActivitiesListItemHolder holderTab = new ActivitiesListItemHolder(inflate);

        return holderTab;
    }

    @Override
    protected void initDataOfHolder(ItemModel item, BaseRecylerItemHolder baseRecylerItemHolder) {

    }


    final class ActivitiesListItemHolder extends BaseRecylerItemHolder {

        ImageView prize_Icon;
        TextView prize_Title;
        TextView prize_SubTitle;
        TextView prize_TimeTip;
        TextView getPrize_Btn;

        public ActivitiesListItemHolder(View itemView) {
            super(itemView);
            prize_Icon = (ImageView) itemView.findViewById(R.id.ar_prize_icon);
            prize_Title = (TextView) itemView.findViewById(R.id.ar_prize_title);
            prize_SubTitle = (TextView) itemView.findViewById(R.id.ar_prize_subtitle);
            prize_TimeTip = (TextView) itemView.findViewById(R.id.ar_prize_timetip);
            getPrize_Btn = (TextView) itemView.findViewById(R.id.ar_prize_btn);
        }
    }


}
