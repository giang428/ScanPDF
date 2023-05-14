package giang.truong.scanpdf.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import giang.truong.scanpdf.R;

public class ListImageAdapter extends PagerAdapter {
    private final Context mContext;
    private final ArrayList<Uri> mEditImages;
    private final LayoutInflater mLayoutInflater;

    public ListImageAdapter(Context mContext, ArrayList<Uri> mEditImages) {
        this.mContext = mContext;
        this.mEditImages = mEditImages;
        this.mLayoutInflater = LayoutInflater.from(mContext);
    }

    public ListImageAdapter(Context mContext, ArrayList<Uri> mEditImages, LayoutInflater mLayoutInflater) {
        this.mContext = mContext;
        this.mEditImages = mEditImages;
        this.mLayoutInflater = mLayoutInflater;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup view, int position) {
        View imgview = mLayoutInflater.inflate(R.layout.image_item,view,false);
        ImageView img = imgview.findViewById(R.id.edit_img);
        Glide.with(mContext)
                .load(mEditImages.get(position))
                .into(img);
        view.addView(imgview, 0);
        return imgview;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
        container.removeView((View) view);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return String.format("%d / %d", position + 1, getCount());
    }

    @Override
    public int getCount() {
        return mEditImages.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
