package giang.truong.scanpdf.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import giang.truong.scanpdf.R
import giang.truong.scanpdf.activity.PDFActivity
import giang.truong.scanpdf.adapter.ReorderAdapter
import giang.truong.scanpdf.databinding.FragmentReorderBinding

class ReorderFragment : Fragment() {
    private lateinit var listImg: ArrayList<Uri>
    private lateinit var listImgTmp: ArrayList<Uri>
    private lateinit var adapter: ReorderAdapter
    private lateinit var binding: FragmentReorderBinding
    private lateinit var containerActivity: PDFActivity
    private val numberOfColumns = 3
    private var isModified: Boolean = false
    private lateinit var recyclerView: DragDropSwipeRecyclerView


    private val onItemDragListener = object : OnItemDragListener<Uri> {
        override fun onItemDragged(previousPosition: Int, newPosition: Int, item: Uri) {
            Log.i(
                "ITEM_DRAGGING",
                "$item is being dragged from position $previousPosition to position $newPosition"
            )

        }

        override fun onItemDropped(initialPosition: Int, finalPosition: Int, item: Uri) {
            if (initialPosition != finalPosition) {
                Log.i(
                    "ITEM_DROP",
                    "$item moved (dragged from position $initialPosition and dropped in position $finalPosition)"
                )
                listImgTmp.remove(item)
                listImgTmp.add(finalPosition, item)
                adapter.notifyDataSetChanged()
                recyclerView.adapter = adapter
                isModified = true
            } else {
                Log.i(
                    "ITEM_DROP",
                    "$item dragged from (and also dropped in) the position $initialPosition"
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        containerActivity = activity as PDFActivity
        listImg = containerActivity.imgs
        listImgTmp = ArrayList(listImg)
        adapter = ReorderAdapter(requireContext(), listImgTmp)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReorderBinding.inflate(layoutInflater)
        recyclerView = binding.list
        recyclerView.layoutManager = GridLayoutManager(activity, numberOfColumns)
        recyclerView.orientation =
            DragDropSwipeRecyclerView.ListOrientation.GRID_LIST_WITH_HORIZONTAL_SWIPING
        recyclerView.numOfColumnsPerRowInGridList = numberOfColumns
        recyclerView.itemLayoutId = R.layout.reorder_item
        recyclerView.dividerDrawableId = null
        recyclerView.adapter = adapter
        recyclerView.dragListener = onItemDragListener

        binding.reorderCancel.setOnClickListener {
            if (isModified) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.warning))
                    .setMessage(getString(R.string.changes_not_saved))
                    .setPositiveButton(getString(R.string.keep_editing)) { dialog, _ -> dialog.dismiss() }
                    .setNegativeButton(R.string.cancel) { dialog, _ ->
                        requireActivity().supportFragmentManager.beginTransaction().remove(this)
                            .commit()
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        binding.reorderSave.setOnClickListener {
            containerActivity.imgs = listImgTmp
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        }

        return binding.root


    }
}