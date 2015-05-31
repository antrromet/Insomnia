package com.antrromet.insomnia.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.antrromet.insomnia.R;
import com.antrromet.insomnia.provider.DBOpenHelper;
import com.squareup.picasso.Picasso;

public class NineGagRecyclerAdapter extends RecyclerView.Adapter<NineGagRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private Cursor mCursor;

    public NineGagRecyclerAdapter(Context context){
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_nine_gag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(mCursor.moveToPosition(position)){
            holder.captionTextView.setText(mCursor.getString(mCursor.getColumnIndex(DBOpenHelper.COLUMN_CAPTION)));
            Picasso.with(mContext).load(mCursor.getString(mCursor.getColumnIndex(DBOpenHelper.COLUMN_IMAGE_NORMAL))).into(holder.imageView);
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

    // Provide a reference to the views for each data item
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView captionTextView;
        private ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            captionTextView = (TextView) view.findViewById(R.id.root_layout).findViewById(R.id.caption_text_view);
            imageView = (ImageView) view.findViewById(R.id.root_layout).findViewById(R.id.image_view);
        }
    }

}
