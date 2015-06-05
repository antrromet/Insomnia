package com.antrromet.insomnia.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.antrromet.insomnia.R;
import com.antrromet.insomnia.provider.DBOpenHelper;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoView;

public class NineGagPagerAdapter extends PagerAdapter {


    private final Context mContext;
    private Cursor mCursor;

    public NineGagPagerAdapter(Object object) {
        mContext = (Context)object;
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
        TextView captionTextView = (TextView) view.findViewById(R.id.title_text_view);
            captionTextView.setText(mCursor.getString(mCursor
                    .getColumnIndex(DBOpenHelper.COLUMN_CAPTION)));
        ImageView contentImageView = (PhotoView) view.findViewById(R.id.content_image_view);
        Picasso.with(mContext).load(mCursor.getString(mCursor.getColumnIndex(DBOpenHelper
                .COLUMN_IMAGE_NORMAL))).into(contentImageView);
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

    public String getShareText(int pos) {
        mCursor.moveToPosition(pos);
        StringBuilder builder = new StringBuilder();
        builder.append(mCursor.getString(mCursor.getColumnIndex(DBOpenHelper.COLUMN_CAPTION)));
        builder.append(" ");
        builder.append(mCursor.getString(mCursor.getColumnIndex(DBOpenHelper.COLUMN_LINK)));
        return builder.toString();
    }

}
