package br.com.phoebus.payments.demo.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by joao.gabriel on 12/05/2017.
 */

public class DividerItemDecoration extends RecyclerView.ItemDecoration {
    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};
    private Drawable divider;

    public DividerItemDecoration(Context context) {
        try {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            divider = a.getDrawable(0);
            a.recycle();
        } catch (Resources.NotFoundException e) {
            /**/
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (divider == null) return;
        if (parent.getChildAdapterPosition(view) < 1) return;

        if (getOrientation(parent) == LinearLayoutManager.VERTICAL)
            outRect.top = divider.getIntrinsicHeight();
        else
            throw new IllegalArgumentException("Only usable with vertical lists");
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (divider == null) {
            super.onDrawOver(c, parent, state);
            return;
        }

        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; ++i) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int size = divider.getIntrinsicHeight();
            final int top = (int) (child.getTop() - params.topMargin - size + child.getTranslationY());
            final int bottom = top + size;
            divider.setBounds(left, top, right, bottom);
            divider.draw(c);

            if (i == childCount - 1) {
                final int newTop = (int) (child.getBottom() + params.bottomMargin + child.getTranslationY());
                final int newBottom = newTop + size;
                divider.setBounds(left, newTop, right, newBottom);
                divider.draw(c);
            }
        }
    }

    private int getOrientation(RecyclerView parent) {
        if (!(parent.getLayoutManager() instanceof LinearLayoutManager))
            throw new IllegalStateException("Layout manager must be an instance of LinearLayoutManager");
        return ((LinearLayoutManager) parent.getLayoutManager()).getOrientation();
    }
}
