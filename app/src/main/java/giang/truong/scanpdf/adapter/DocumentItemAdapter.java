package giang.truong.scanpdf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import giang.truong.scanpdf.R;
import giang.truong.scanpdf.databinding.DocumentItemBinding;
import giang.truong.scanpdf.model.Document;
import giang.truong.scanpdf.viewmodel.DocumentViewModel;

public class DocumentItemAdapter extends RecyclerView.Adapter<DocumentItemAdapter.ViewHolder> {
    private final IOnDocumentClick documentClick;
    private final Context context;
    private final List<Document> documentList = new ArrayList<>();

    private final DocumentViewModel viewModel;

    public DocumentItemAdapter(Context context, IOnDocumentClick documentClick, DocumentViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
        this.documentClick = documentClick;
    }

    @NonNull
    @Override
    public DocumentItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DocumentItemBinding b = DocumentItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentItemAdapter.ViewHolder holder, int position) {
        Document i = documentList.get(position);
        holder.name.setText(i.getName());
        holder.dateCreated.setText(i.getCreatedDate());
        if(i.getPages() > 0) holder.page.setText(
                context.getString(R.string.number_of_pages,i.getPages())
        );
        else holder.page.setText("Empty");
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    public void setData(List<Document> documents) {
        this.documentList.clear();
        this.documentList.addAll(documents);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        private final MaterialTextView name;
        private final MaterialTextView dateCreated;
        private final MaterialTextView page;

        public ViewHolder(@NonNull DocumentItemBinding b) {
            super(b.getRoot());
            name = b.fileName;
            dateCreated = b.dateCreated;
            page = b.pageCount;
            b.getRoot().setOnLongClickListener(this);
            b.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            documentClick.onClick(getAbsoluteAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            documentClick.onLongClick(getAbsoluteAdapterPosition());
            return false;
        }
    }
}
