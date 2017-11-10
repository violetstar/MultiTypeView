/*
Copyright 2016 shizhefei（LuckyJayce）https://github.com/LuckyJayce

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.shizhefei.view.multitype.provider;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shizhefei.view.multitype.ItemViewProvider;

/**
 * View提供者
 */
public class ViewProvider extends ItemViewProvider<View> {
    private final int layoutWidth;
    private final int layoutHeight;

    public ViewProvider() {
        this.layoutWidth = ViewUtils.UNSET_LAYOUT_SIZE;
        this.layoutHeight = ViewUtils.UNSET_LAYOUT_SIZE;
    }

    public ViewProvider(int layoutWidth, int layoutHeight) {
        this.layoutWidth = layoutWidth;
        this.layoutHeight = layoutHeight;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int providerType) {
        ChildViewHeightLayout layout = new ChildViewHeightLayout(parent.getContext());
        layout.setLayoutParams(ViewUtils.getRightLayoutParams(parent, layoutWidth, layoutHeight));
        return new RecyclerView.ViewHolder(layout) {
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams != null) {
            view.setTag(layoutParams);
        } else {
            layoutParams = view.getLayoutParams();
        }
        ChildViewHeightLayout layout = (ChildViewHeightLayout) viewHolder.itemView;
        layout.removeAllViews();
        if (view.getParent() != null) {
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            viewGroup.removeView(view);
        }
        if (layoutParams != null) {
            layout.addView(view, layoutParams);
        } else {
            layout.addView(view);
        }
    }
}