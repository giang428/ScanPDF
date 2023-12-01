package giang.truong.scanpdf.adapter

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import giang.truong.scanpdf.R
import giang.truong.scanpdf.databinding.ThumbnailItemBinding

class ThumbnailPreviewAdapter(
    val mContext : Context,
    val imgList : ArrayList<Uri>,
    val onThumbnailClick: (Int) -> Unit
    ): RecyclerView.Adapter<ThumbnailPreviewAdapter.ViewHolder>() {
    private var selectedPosition: Int = 0

    fun updateSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ThumbnailItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(b)
    }

    override fun getItemCount() = imgList.size

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        Glide.with(mContext).load(imgList[pos]).into(holder.thumb)
        holder.card.setOnClickListener {
            onThumbnailClick(pos)
        }
        val initBackground = holder.card.background
        if(selectedPosition == pos) {
            holder.card.setBackgroundResource(R.drawable.selected_border)
        } else{
            holder.card.setBackgroundResource(R.drawable.default_border)
        }
    }
    class ViewHolder (b: ThumbnailItemBinding): RecyclerView.ViewHolder(b.root) {
        val thumb : ImageView = b.thumbnail
        val card : CardView = b.thumbCard
    }
}