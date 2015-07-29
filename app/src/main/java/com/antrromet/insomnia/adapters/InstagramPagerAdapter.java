package com.antrromet.insomnia.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.view.PagerAdapter;
import android.view.GestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.antrromet.insomnia.R;
import com.antrromet.insomnia.provider.DBOpenHelper;
import com.antrromet.insomnia.widgets.TouchImageView;
import com.squareup.picasso.Picasso;

public class InstagramPagerAdapter extends PagerAdapter {


    private final Context mContext;
    private Cursor mCursor;
    private GestureDetector.OnDoubleTapListener mDoubleTapListener;
    private View.OnLongClickListener mLongClickListener;
    private boolean mIsTitleVisible;
    private View.OnClickListener mClickListener;

    public InstagramPagerAdapter(Object object) {
        mContext = (Context) object;
        mDoubleTapListener = (GestureDetector.OnDoubleTapListener) object;
        mLongClickListener = (View.OnLongClickListener) object;
        mClickListener = (View.OnClickListener) object;
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = View.inflate(container.getContext(), R.layout
                .pager_item_nine_gag_full_screen, null);
        mCursor.moveToPosition(position);
        ((TextView) view.findViewById(R.id.title_text_view)).setText(mCursor.getString(mCursor
                .getColumnIndex(DBOpenHelper.COLUMN_CAPTION)));
        view.findViewById(R.id.title_text_view).setOnClickListener(mClickListener);
        TouchImageView contentImageView = (TouchImageView) view.findViewById(R.id
                .content_image_view);
        contentImageView.setOnDoubleTapListener(mDoubleTapListener);
        Picasso.with(mContext).load(mCursor.getString(mCursor.getColumnIndex(DBOpenHelper
                .COLUMN_STANDARD_RES_IMAGE))).into(contentImageView);
        View textViewContent = view.findViewById(R.id.text_view_content);
        textViewContent.bringToFront();
        if (mIsTitleVisible) {
            textViewContent.setVisibility(View.VISIBLE);
        } else {
            textViewContent.setVisibility(View.GONE);
        }
        textViewContent.setOnClickListener(mClickListener);
        view.setTag(position);
        contentImageView.setOnLongClickListener(mLongClickListener);
        contentImageView.setTag(R.id.key_link, mCursor.getString(mCursor.getColumnIndex(DBOpenHelper
                .COLUMN_LINK)));
        container.addView(view, 0);
        return view;
    }

    @Override
    public int getCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public void setTitleVisibility(boolean isTitleVisible) {
        mIsTitleVisible = isTitleVisible;
        notifyDataSetChanged();
    }

    public String getShareText(int pos) {
        mCursor.moveToPosition(pos);
        return mCursor.getString(mCursor.getColumnIndex(DBOpenHelper.COLUMN_LINK));
    }
}
