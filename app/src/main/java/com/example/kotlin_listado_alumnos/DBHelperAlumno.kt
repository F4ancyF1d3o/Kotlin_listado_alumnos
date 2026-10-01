package com.example.kotlin_listado_alumnos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelperAlumno(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Alumnos.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "Alumnos"
        const val COLUMN_CUENTA = "cuenta"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_CORREO = "correo"
        const val COLUMN_IMAGEN = "imagen"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_CUENTA TEXT PRIMARY KEY,
                $COLUMN_NOMBRE TEXT NOT NULL,
                $COLUMN_CORREO TEXT NOT NULL,
                $COLUMN_IMAGEN TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)

        // Insertar datos iniciales de prueba
        insertarDatosIniciales(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    private fun insertarDatosIniciales(db: SQLiteDatabase) {
        val alumnosIniciales = listOf(
            Alumno("Juan Pérez", "20210001", "juan.perez@email.com", "https://picsum.photos/id/1005/200/200"),
            Alumno("María López", "20210002", "maria.lopez@email.com", "https://picsum.photos/id/1027/200/200"),
            Alumno("Carlos Gómez", "20210003", "carlos.gomez@email.com", "https://picsum.photos/id/1012/200/200")
        )

        for (alumno in alumnosIniciales) {
            val values = ContentValues().apply {
                put(COLUMN_CUENTA, alumno.cuenta)
                put(COLUMN_NOMBRE, alumno.nombre)
                put(COLUMN_CORREO, alumno.correo)
                put(COLUMN_IMAGEN, alumno.imagen)
            }
            db.insert(TABLE_NAME, null, values)
        }
    }

    fun insertarAlumno(alumno: Alumno): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CUENTA, alumno.cuenta)
            put(COLUMN_NOMBRE, alumno.nombre)
            put(COLUMN_CORREO, alumno.correo)
            put(COLUMN_IMAGEN, alumno.imagen)
        }
        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result
    }

    fun obtenerAlumnos(): ArrayList<Alumno> {
        val lista = ArrayList<Alumno>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            val idxCuenta = cursor.getColumnIndex(COLUMN_CUENTA)
            val idxNombre = cursor.getColumnIndex(COLUMN_NOMBRE)
            val idxCorreo = cursor.getColumnIndex(COLUMN_CORREO)
            val idxImagen = cursor.getColumnIndex(COLUMN_IMAGEN)

            do {
                val cuenta = if (idxCuenta >= 0) cursor.getString(idxCuenta) else ""
                val nombre = if (idxNombre >= 0) cursor.getString(idxNombre) else ""
                val correo = if (idxCorreo >= 0) cursor.getString(idxCorreo) else ""
                val imagen = if (idxImagen >= 0) cursor.getString(idxImagen) else ""

                lista.add(Alumno(nombre, cuenta, correo, imagen))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun actualizarAlumno(alumno: Alumno): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, alumno.nombre)
            put(COLUMN_CORREO, alumno.correo)
            put(COLUMN_IMAGEN, alumno.imagen)
        }
        val result = db.update(TABLE_NAME, values, "$COLUMN_CUENTA = ?", arrayOf(alumno.cuenta))
        db.close()
        return result
    }

    fun eliminarAlumno(cuenta: String): Int {
        val db = writableDatabase
        val result = db.delete(TABLE_NAME, "$COLUMN_CUENTA = ?", arrayOf(cuenta))
        db.close()
        return result
    }
}
