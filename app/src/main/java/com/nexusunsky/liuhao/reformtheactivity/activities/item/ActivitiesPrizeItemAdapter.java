package com.nexusunsky.liuhao.reformtheactivity.activities.item;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nexusunsky.liuhao.reformtheactivity.model.ItemModel;
import com.nexusunsky.liuhao.reformtheactivity.tab.ViewHolderPagerAdapter;

/**
 * @author Administrator
 * @time 16/8/16 上午10:50
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class ActivitiesPrizeItemAdapter extends ViewHolderPagerAdapter.DataRecyclerAdapter {

    public ActivitiesPrizeItemAdapter(Context context, int resLayoutID) {
        super(context, resLayoutID);
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder getViewHolder(View inflate, int viewType) {

        return null;
    }

    @Override
    protected void initDataOfHolder(ItemModel item, BaseRecylerItemHolder baseRecylerItemHolder) {

    }

    final class ViewHolderTab extends BaseRecylerItemHolder {

        ImageView prize_Icon;
        TextView prize_Title;
        TextView prize_SubTitle;
        TextView prize_TimeTip;
        TextView getPrize_Btn;

        public ViewHolderTab(View itemView) {
            super(itemView);
        }
    }


}
