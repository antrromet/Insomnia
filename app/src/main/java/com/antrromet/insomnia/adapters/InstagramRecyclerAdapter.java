package com.antrromet.insomnia.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.antrromet.insomnia.R;
import com.antrromet.insomnia.provider.DBOpenHelper;
import com.squareup.picasso.Picasso;

public class InstagramRecyclerAdapter extends RecyclerView.Adapter<InstagramRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private Cursor mCursor;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    public InstagramRecyclerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_instagram, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mCursor.moveToPosition(position)) {
            StringBuilder builder = new StringBuilder(mCursor.getString(mCursor.getColumnIndex
                    (DBOpenHelper.COLUMN_USERNAME)));
            String caption = mCursor.getString(mCursor.getColumnIndex(DBOpenHelper.COLUMN_CAPTION));
            if (!TextUtils.isEmpty(caption)) {
                builder.append(" : ");
                builder.append(caption);
            }
            holder.captionTextView.setText(builder.toString());
            Picasso.with(mContext).load(mCursor.getString(mCursor.getColumnIndex(DBOpenHelper
                    .COLUMN_STANDARD_RES_IMAGE))).into(holder.imageView);
        }
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public void setOnItemLongClickListener(final OnItemLongClickListener itemLongClickListener) {
        mItemLongClickListener = itemLongClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, String id);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, String link);
    }

    // Provide a reference to the views for each data item
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View
            .OnLongClickListener {

        private TextView captionTextView;
        private ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            captionTextView = (TextView) view.findViewById(R.id.root_layout).findViewById(R.id
                    .caption_text_view);
            imageView = (ImageView) view.findViewById(R.id.root_layout).findViewById(R.id
                    .content_image_view);
            view.setOnLongClickListener(this);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                if (mCursor.moveToPosition(getAdapterPosition())) {
                    String id = mCursor.getString(mCursor.getColumnIndex(DBOpenHelper.COLUMN_ID));
                    mItemClickListener.onItemClick(v, getAdapterPosition(), id);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mItemLongClickListener != null) {
                if (mCursor.moveToPosition(getAdapterPosition())) {
                    String link = mCursor.getString(mCursor.getColumnIndex(DBOpenHelper
                            .COLUMN_LINK));
                    mItemLongClickListener.onItemLongClick(v, link);
                }
            }
            return true;
        }
    }

}
