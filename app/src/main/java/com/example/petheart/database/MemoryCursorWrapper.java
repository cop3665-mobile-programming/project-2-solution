package com.example.petheart.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

import com.example.petheart.models.Memory;

import java.util.Date;
import java.util.UUID;


public class MemoryCursorWrapper extends CursorWrapper {

    public MemoryCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Memory getMemory()
    {
        String uuidString = getString(getColumnIndex(MemoryDbSchema.MemoryTable.Cols.UUID));
        String title = getString(getColumnIndex(MemoryDbSchema.MemoryTable.Cols.TITLE));
        long date = getLong(getColumnIndex(MemoryDbSchema.MemoryTable.Cols.DATE));
        int isFavorited = getInt(getColumnIndex(MemoryDbSchema.MemoryTable.Cols.FAVORITED));
        String description = getString(getColumnIndex(MemoryDbSchema.MemoryTable.Cols.DESCRIPTION));

        Memory memory = new Memory(UUID.fromString(uuidString));
        memory.setTitle(title);
        memory.setDate(new Date(date));
        memory.setFavorited(isFavorited != 0);
        memory.setDescription(description);

        return memory;

    }
}
