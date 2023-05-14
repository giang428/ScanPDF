package giang.truong.scanpdf.adapter

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import giang.truong.scanpdf.R

class ReorderAdapter(val mContext : Context, dataset : ArrayList<Uri>) : DragDropSwipeAdapter<Uri,ReorderAdapter.ViewHolder>(dataset){



    class ViewHolder(itemLayout : View) : DragDropSwipeAdapter.ViewHolder(itemLayout){
        val pageNum: TextView = itemLayout.findViewById(R.id.page_num)
        val img: ImageView = itemLayout.findViewById(R.id.each_file_screenshot)
    }


    override fun getViewHolder(itemView: View): ViewHolder  = ViewHolder(itemView)


    override fun onBindViewHolder(item: Uri, viewHolder: ViewHolder, position: Int) {
        Glide.with(mContext).load(item).into(viewHolder.img)
        viewHolder.pageNum.text = (position+1).toString()
    }

    override fun getViewToTouchToStartDraggingItem(
        item: Uri,
        viewHolder: ViewHolder,
        position: Int
    ): View {
        return viewHolder.img
    }

}