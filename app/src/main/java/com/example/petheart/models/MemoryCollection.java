package com.example.petheart.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.petheart.database.MemoryCollectionHelper;
import com.example.petheart.database.MemoryCursorWrapper;
import com.example.petheart.database.MemoryDbSchema;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MemoryCollection {
    private static MemoryCollection sMemoryCollection;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static MemoryCollection get(Context context)
    {
        if(sMemoryCollection == null)
        {
            sMemoryCollection = new MemoryCollection(context);
        }

        return sMemoryCollection;
    }

    private MemoryCollection(Context context)
    {
        mContext = context.getApplicationContext();
        mDatabase = new MemoryCollectionHelper(mContext).getWritableDatabase();
    }

    public List<Memory> getMemories()
    {
        List<Memory> memories = new ArrayList<>();
        MemoryCursorWrapper cursor = queryMemories(null, null);

        try
        {
            cursor.moveToFirst();
            while(!cursor.isAfterLast())
            {
                memories.add(cursor.getMemory());
                cursor.moveToNext();
            }
        }
        finally {
            cursor.close();
        }

        return memories;
    }

    public List<Memory> getFavorited()
    {
        List<Memory> memories = new ArrayList<>();
        MemoryCursorWrapper cursor = queryMemories("favorited=CAST(? AS INTEGER)",
                new String [] {String.valueOf(1)});

        try
        {
            cursor.moveToFirst();
            while(!cursor.isAfterLast())
            {
                memories.add(cursor.getMemory());
                cursor.moveToNext();
            }
        }
        finally {
            cursor.close();
        }

        return memories;
    }

    public void addMemory(Memory m)
    {
        ContentValues values = getContentValues(m);
        mDatabase.insert(MemoryDbSchema.MemoryTable.NAME, null, values);
    }

    public void deleteMemory(Memory m)
    {
        ContentValues values = getContentValues(m);
        mDatabase.delete(MemoryDbSchema.MemoryTable.NAME, MemoryDbSchema.MemoryTable.Cols.UUID + "=?", new String[] {m.getId().toString()});
    }

    public Memory getMemory(UUID id)
    {
        MemoryCursorWrapper cursor = queryMemories(MemoryDbSchema.MemoryTable.Cols.UUID + "=?",
                new String[] { id.toString()});
        try{
            if(cursor.getCount() == 0)
            {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getMemory();
        }finally {
            cursor.close();
        }
    }

    public void updateMemory(Memory memory)
    {
        String uuidString = memory.getId().toString();
        ContentValues values = getContentValues(memory);

        mDatabase.update(MemoryDbSchema.MemoryTable.NAME, values,
                MemoryDbSchema.MemoryTable.Cols.UUID + " =?",
                new String[] { uuidString });
    }

    private static ContentValues getContentValues(Memory memory)
    {
        ContentValues values = new ContentValues();
        values.put(MemoryDbSchema.MemoryTable.Cols.UUID, memory.getId().toString());
        values.put(MemoryDbSchema.MemoryTable.Cols.TITLE, memory.getTitle());
        values.put(MemoryDbSchema.MemoryTable.Cols.DATE, memory.getDate().getTime());
        values.put(MemoryDbSchema.MemoryTable.Cols.FAVORITED, memory.isFavorited() ? 1 : 0);
        values.put(MemoryDbSchema.MemoryTable.Cols.DESCRIPTION, memory.getDescription());

        return values;
    }

    private MemoryCursorWrapper queryMemories(String whereClause, String[] whereArgs)
    {
        Cursor cursor = mDatabase.query(MemoryDbSchema.MemoryTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null);

        return new MemoryCursorWrapper(cursor);
    }

    public File getPhotoFile(Memory memory) {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, memory.getPhotoFilename());
    }
}
