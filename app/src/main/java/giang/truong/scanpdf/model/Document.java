package giang.truong.scanpdf.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Document {

    @PrimaryKey(autoGenerate = true)
    private int _id;

    @NonNull
    private String name;

    @NonNull
    private String path;

    @NonNull
    private String createdDate;
    @NonNull
    private int pages;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String path) {
        this.path = path;
    }

    @NonNull
    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(@NonNull String createdDate) {
        this.createdDate = createdDate;
    }
    @NonNull
    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}
