package com.antrromet.insomnia.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.antrromet.insomnia.R;
import com.antrromet.insomnia.fragments.BaseFragment;
import com.antrromet.insomnia.fragments.InstagramFragment;
import com.antrromet.insomnia.fragments.NineGagFragment;
import com.antrromet.insomnia.fragments.TwitterFragment;


public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

    private final Context mContext;
    private SparseArray<BaseFragment> fragments;
    private int[] imageResId = {
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher
    };

    public MainViewPagerAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        fragments = new SparseArray<>(3);
        mContext = context;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(int position) {
        // Create a new fragment only when the fragment is not present in the fragments sparse
        // array
        BaseFragment fragment = fragments.get(position);
        if (fragment == null) {
            switch (position) {
                case 0: {
                    fragment = new NineGagFragment();
                    fragments.put(0, fragment);
                    break;
                }

//                case 1: {
//                    fragment = new FacebookFragment();
//                    fragments.put(1, fragment);
//                    break;
//                }

                case 1: {
                    fragment = new TwitterFragment();
                    fragments.put(1, fragment);
                    break;
                }

                case 2: {
                    fragment = new InstagramFragment();
                    fragments.put(2, fragment);
                    break;
                }

                default:
                    fragment = null;
            }
        }
        return fragment;
    }

    public BaseFragment getFragmentAt(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Using spannable string to add image to the title bar instead of text
        Drawable image = ContextCompat.getDrawable(mContext, imageResId[position]);
        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
        SpannableString sb = new SpannableString(" ");
        ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}
