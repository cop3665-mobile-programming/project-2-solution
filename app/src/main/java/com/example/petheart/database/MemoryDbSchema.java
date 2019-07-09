package com.example.petheart.database;

public class MemoryDbSchema {
    public static final class MemoryTable {
        public static final String NAME = "memories";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String FAVORITED = "favorited";
            public static final String DESCRIPTION = "description";
        }
    }
}
