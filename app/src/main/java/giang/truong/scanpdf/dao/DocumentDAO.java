package giang.truong.scanpdf.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import giang.truong.scanpdf.model.Document;

@Dao
public interface DocumentDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveAll(List<Document> documentList);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(Document document);

    @Query("SELECT * FROM Document ORDER BY _id DESC")
    LiveData<List<Document>> findAll();

    @Query("SELECT * FROM Document WHERE name like :text OR createdDate like :text ORDER BY _id DESC")
    LiveData<List<Document>> search( String text );

    @Update
    void update( Document document );

    @Delete
    void delete( Document document );
}
