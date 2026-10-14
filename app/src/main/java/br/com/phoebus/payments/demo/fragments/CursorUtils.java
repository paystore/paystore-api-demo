package br.com.phoebus.payments.demo.fragments;

import android.database.Cursor;
import android.database.MatrixCursor;

import androidx.core.util.Consumer;


/**
 * @author rauny.souza
 */
public final class CursorUtils {
    private CursorUtils() {}

    public static Cursor emptyCursor() {
        return new MatrixCursor(new String[0], 1);
    }

    public static boolean isEmpty(Cursor cursor) {
        return cursor == null || cursor.getCount() == 0;
    }

    public static int getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    public static boolean getBoolean(Cursor cursor, String columnName) {
        return getInt(cursor, columnName) == 1;
    }

    public static long getLong(Cursor cursor, String columnName) {
        return cursor.getLong(cursor.getColumnIndex(columnName));
    }

    public static float getFloat(Cursor cursor, String columnName) {
        return cursor.getFloat(cursor.getColumnIndex(columnName));
    }

    public static String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    public static short getShort(Cursor cursor, String columnName) {
        return cursor.getShort(cursor.getColumnIndex(columnName));
    }

    public static byte[] getBlob(Cursor cursor, String columnName) {
        return cursor.getBlob(cursor.getColumnIndex(columnName));
    }

    public static double getDouble(Cursor cursor, String columnName) {
        return cursor.getDouble(cursor.getColumnIndex(columnName));
    }

    public static <E extends Enum<E>> E getEnum(Cursor cursor, String columnName, Class<E> clazz) {
        if (clazz.isEnum()) {
            return Enum.valueOf(clazz, getString(cursor, columnName));
        }
        return null;
    }

    public static Object getBooleanToAdd(Boolean value) {
        return (value != null && value) ? 1: 0;
    }

    public static void forEach(Cursor cursor, Consumer<Cursor> cursorConsumer, boolean closeWhenDone) {
        if (cursor != null && cursor.getCount() > 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                cursorConsumer.accept(cursor);
            }

            if (closeWhenDone) {
                cursor.close();
            }
        }
    }

    public static boolean hasColumn(Cursor cursor, String columnName) {
        return cursor.getColumnIndex(columnName) >= 0;
    }
}
