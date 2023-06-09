package giang.truong.scanpdf.dao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import giang.truong.scanpdf.model.Document;

@Database(entities = {Document.class}, version = 1, exportSchema = false)
public abstract class DocumentDatabase extends RoomDatabase {
    private static DocumentDatabase INSTANCE;

    public abstract DocumentDAO documentDAO();

    private static final Object sLock = new Object();

    public static DocumentDatabase getInstance( Context context ) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                                context.getApplicationContext(),
                                DocumentDatabase.class,
                                "documents.db"
                        )
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build();
            }
            return INSTANCE;
        }
    }
}
