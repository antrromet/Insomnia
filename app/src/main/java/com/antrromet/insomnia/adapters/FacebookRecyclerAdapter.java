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

public class FacebookRecyclerAdapter extends RecyclerView.Adapter<FacebookRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private Cursor mCursor;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    public FacebookRecyclerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_facebook, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mCursor.moveToPosition(position)) {
            String from = mCursor.getString(mCursor.getColumnIndex(DBOpenHelper
                    .COLUMN_FROM));
            StringBuilder owner = new StringBuilder(from);
            String to = mCursor.getString(mCursor.getColumnIndex(DBOpenHelper
                    .COLUMN_TO));
            if (!TextUtils.isEmpty(to)) {
                owner.append(" -> ");
                owner.append(to);
            }
            holder.ownerTextView.setText(owner.toString());
            String message = mCursor.getString(mCursor.getColumnIndex(DBOpenHelper
                    .COLUMN_MESSAGE));
            if (TextUtils.isEmpty(message)) {
                holder.captionTextView.setVisibility(View.GONE);
            } else {
                holder.captionTextView.setVisibility(View.VISIBLE);
                holder.captionTextView.setText(message);
            }

            String pictureUrl = mCursor.getString(mCursor.getColumnIndex(DBOpenHelper
                    .COLUMN_PICTURE));
            if (TextUtils.isEmpty(pictureUrl)) {
                holder.imageView.setVisibility(View.GONE);
            } else {
                holder.imageView.setVisibility(View.VISIBLE);
                Picasso.with(mContext).load(pictureUrl).into(holder.imageView);
            }
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
        void onItemLongClick(View view, int position, String id);
    }

    // Provide a reference to the views for each data item
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        private TextView ownerTextView;
        private TextView captionTextView;
        private ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            ownerTextView = (TextView) view.findViewById(R.id.root_layout).findViewById(R.id
                    .owner_text_view);
            captionTextView = (TextView) view.findViewById(R.id.root_layout).findViewById(R.id.caption_text_view);
            imageView = (ImageView) view.findViewById(R.id.root_layout).findViewById(R.id.content_image_view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
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
                    String id = mCursor.getString(mCursor.getColumnIndex(DBOpenHelper.COLUMN_ID));
                    mItemLongClickListener.onItemLongClick(v, getAdapterPosition(), id);
                }
            }
            return true;
        }
    }


}