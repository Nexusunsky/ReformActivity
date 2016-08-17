package com.nexusunsky.liuhao.reformtheactivity.util;

import android.util.SparseArray;
import android.view.View;

/**
 * @author liuhao
 * @time 16/8/13 下午2:39
 * @des ${TODO}
 */
public class VHContainer {

    public static <T extends View> T get(View rootView, int id) {

        SparseArray<View> viewContainer = (SparseArray<View>) rootView.getTag();

        if (viewContainer == null) {
            viewContainer = new SparseArray<View>();
            rootView.setTag(viewContainer);
        }
        View childView = viewContainer.get(id);
        if (childView == null) {
            childView = rootView.findViewById(id);
            viewContainer.put(id, childView);
        }

        return (T) childView;

    }

}
