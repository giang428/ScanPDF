package giang.truong.scanpdf.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import giang.truong.scanpdf.dao.DocumentDAO;
import giang.truong.scanpdf.dao.DocumentDatabase;
import giang.truong.scanpdf.model.Document;

public class DocumentViewModel extends AndroidViewModel {
    private final DocumentDAO dao;
    private final ExecutorService executorService;

    public DocumentViewModel(@NonNull Application application) {
        super(application);
        dao = DocumentDatabase.getInstance(application).documentDAO();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Document>> getAllDocuments() {
        return dao.findAll();
    }

    public LiveData<List<Document>> search( String text ) {
        return dao.search(text);
    }

    public void saveDocument(final Document document) {
        executorService.execute(() -> dao.save(document));
    }

    public void updateDocument(final Document document) {
        executorService.execute(() -> dao.update(document));
    }

    public void deleteDocument(final Document document) {
        executorService.execute(() -> dao.delete(document));
    }
}
