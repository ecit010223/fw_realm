package com.huier.fw_realm.adapter;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.huier.fw_realm.R;
import com.huier.fw_realm.model.Counter;

import java.util.HashSet;
import java.util.Set;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * 作者：张玉辉
 * 时间：2017/8/17.
 */

public class ListViewAdapter extends RealmBaseAdapter<Counter> implements ListAdapter {
    private static class ViewHolder {
        TextView countText;
        CheckBox deleteCheckBox;
    }

    private boolean inDeletionMode = false;
    private Set<Integer> countersToDelete = new HashSet<Integer>();

    public ListViewAdapter(@Nullable OrderedRealmCollection<Counter> data) {
        super(data);
    }

    public void enableDeletionMode(boolean enabled) {
        inDeletionMode = enabled;
        if (!enabled) {
            countersToDelete.clear();
        }
        notifyDataSetChanged();
    }

    public Set<Integer> getCountersToDelete() {
        return countersToDelete;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.countText = (TextView) convertView.findViewById(R.id.textview);
            viewHolder.deleteCheckBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (adapterData != null) {
            final Counter item = adapterData.get(position);
            viewHolder.countText.setText(item.getCountString());
            if (inDeletionMode) {
                viewHolder.deleteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        countersToDelete.add(item.getCount());
                    }
                });
            } else {
                viewHolder.deleteCheckBox.setOnCheckedChangeListener(null);
            }
            viewHolder.deleteCheckBox.setChecked(countersToDelete.contains(item.getCount()));
            viewHolder.deleteCheckBox.setVisibility(inDeletionMode ? View.VISIBLE : View.GONE);
        }
        return convertView;
    }
}
