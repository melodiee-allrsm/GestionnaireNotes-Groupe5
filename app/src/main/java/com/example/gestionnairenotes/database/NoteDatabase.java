package com.example.gestionnairenotes.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.gestionnairenotes.dao.NoteDao;
import com.example.gestionnairenotes.model.Note;

@Database(entities = {Note.class}, version = 1, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {

    private static NoteDatabase instance;

    public abstract NoteDao noteDao();

    public static synchronized NoteDatabase getInstance(Context context) {

        if (instance == null) {

            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            NoteDatabase.class,
                            "notes_db"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }

        return instance;
    }
}
