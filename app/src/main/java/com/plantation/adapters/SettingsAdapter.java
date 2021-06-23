package com.plantation.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.plantation.R;
import com.plantation.data.SettingsItem;

import java.util.Collections;
import java.util.List;

/**
 * Created by Michael on 25/06/2015.
 */
public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 2;
    private static final int TYPE_ITEM = 1;
    private final Activity mActivity;
    public LayoutInflater inflater;
    List<SettingsItem> settingsItems = Collections.emptyList();

    public SettingsAdapter(Activity mActivity, List<SettingsItem> settingsItems) {
        this.mActivity = mActivity;
        inflater = LayoutInflater.from(mActivity);
        this.settingsItems = settingsItems;

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_HEADER) {

            View view = inflater.inflate(R.layout.ads_header, parent, false);
            HeaderViewHolder holder = new HeaderViewHolder(view);
            return holder;
        } else {
            View view = inflater.inflate(R.layout.settings_item, parent, false);
            ItemHolder holder = new ItemHolder(view);
            return holder;
        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemHolder) {
            ItemHolder itemHolder = (ItemHolder) holder;
            SettingsItem settingsItem = settingsItems.get(position - 1);
            itemHolder.title.setText(settingsItem.title);
            itemHolder.icon.setImageResource(settingsItem.iconId);
        } else if (holder instanceof HeaderViewHolder) {

            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;


        }
    }

    @Override
    public int getItemCount() {

        if (settingsItems != null) {
            return settingsItems.size() + 1;
        } else {
            return 1;
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon;

        public ItemHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.listText);
            icon = itemView.findViewById(R.id.listIcon);

        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {


        public HeaderViewHolder(View itemView) {
            super(itemView);

        }
    }
}
