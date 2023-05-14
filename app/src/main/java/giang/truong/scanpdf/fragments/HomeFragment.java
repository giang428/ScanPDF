package giang.truong.scanpdf.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Comparator;
import java.util.List;

import giang.truong.scanpdf.R;
import giang.truong.scanpdf.adapter.DocumentItemAdapter;
import giang.truong.scanpdf.adapter.IOnDocumentClick;
import giang.truong.scanpdf.databinding.FragmentHomeBinding;
import giang.truong.scanpdf.databinding.SortDialogBinding;
import giang.truong.scanpdf.model.Document;
import giang.truong.scanpdf.utils.FileUtils;
import giang.truong.scanpdf.viewmodel.DocumentViewModel;


public class HomeFragment extends Fragment implements IOnDocumentClick {
    private FragmentHomeBinding f;
    private DocumentViewModel viewModel;
    LiveData<List<Document>> listDocument;
    LiveData<List<Document>> resultDoc;
    private DocumentItemAdapter documentItemAdapter;
    private MaterialAlertDialogBuilder sortDialogBuilder;
    private SortDialogBinding sdb;
    public HomeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(DocumentViewModel.class);
        listDocument = viewModel.getAllDocuments();
        resultDoc = listDocument;
        documentItemAdapter = new DocumentItemAdapter(requireContext(), this, viewModel);
    }

    private void setAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        f.recyclerView.setLayoutManager(linearLayoutManager);
        f.recyclerView.setAdapter(documentItemAdapter);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        f = FragmentHomeBinding.inflate(inflater, container, false);
        setAdapter();

        sortDialogBuilder = new MaterialAlertDialogBuilder(requireContext());
        sdb = SortDialogBinding.inflate(getLayoutInflater());

        listDocument.observe(getViewLifecycleOwner(), documents -> {
            Log.i("DATA", String.valueOf(documents.size()));
            if (documents.size() > 0)
                f.empty.setVisibility(View.GONE);
            else
                f.empty.setVisibility(View.VISIBLE);
            documentItemAdapter.setData(documents);
        });
        f.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchFor(newText);
                return false;
            }
        });
        AlertDialog dig = sortDialogInit();
        f.sort.setOnClickListener(v -> dig.show());
        return f.getRoot();

    }

    private AlertDialog sortDialogInit() {
        sortDialogBuilder.setView(sdb.getRoot())
                .setTitle(R.string.sort_by)
                .setPositiveButton("OK", (dialog, which) -> {
                            sortingItem(
                                    sdb.sortingDialogRadioSorting.getCheckedRadioButtonId(),
                                    sdb.sortingDialogRadioOrder.getCheckedRadioButtonId());
                            dialog.dismiss();
                        }
                ).setNegativeButton(R.string.cancel, ((dialog, which) -> dialog.dismiss()));
        return sortDialogBuilder.create();
    }

    private void sortingItem(int type, int order) {
        int name = R.id.sorting_dialog_radio_name;
        int date = R.id.sorting_dialog_radio_date_created;
        int page = R.id.sorting_dialog_page_count;
        boolean isAscending = (order == R.id.sorting_dialog_radio_ascending);

        listDocument.observe(getViewLifecycleOwner(), documents -> {
                    if (type == name)
                        documents.sort(isAscending ?
                                Comparator.comparing(Document::getName) :
                                Comparator.comparing(Document::getName).reversed());
                    else if (type == date && !isAscending)
                        documents.sort(Comparator.comparing(Document::getCreatedDate));
                    else if (type == page) documents.sort(isAscending ?
                            Comparator.comparingInt(Document::getPages) :
                            Comparator.comparingInt(Document::getPages).reversed());
                    documentItemAdapter.setData(documents);
                }
        );
    }

    private void searchFor(String newText) {
        viewModel
                .search('%' + newText + '%')
                .observe(this, documents -> documentItemAdapter.setData(documents));
    }

    @Override
    public void onResume() {
        super.onResume();
        sdb.sortingDialogRadioDateCreated.setChecked(true);
        sdb.sortingDialogRadioAscending.setChecked(true);
    }

    @Override
    public void onClick(int pos) {
        String path = listDocument.getValue().get(pos).getPath();
        FileUtils.viewFile(requireContext(), path);
    }

    @Override
    public void onLongClick(int pos) {
        PopupMenu popup = new PopupMenu(requireActivity(), f.recyclerView.getChildAt(pos), Gravity.END);
        popup.setForceShowIcon(true);
        popup.inflate(R.menu.document_options_menu);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.doc_view) {
                onClick(pos);
                return true;
            } else if (item.getItemId() == R.id.doc_delete) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete).setMessage(R.string.are_you_sure)
                        .setPositiveButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                        .setNegativeButton(R.string.delete, (dialog, which) -> {
                            viewModel.deleteDocument(listDocument.getValue().get(pos));
                            try {
                                FileUtils.removeFile(listDocument.getValue().get(pos).getPath());
                            } catch (Exception er){er.printStackTrace();}
                        })
                        .show();
                return true;
            } else {
                String path = listDocument.getValue().get(pos).getPath();
                FileUtils.shareFile(requireContext(), path);
                return true;
            }
        });
        popup.show();
    }
}