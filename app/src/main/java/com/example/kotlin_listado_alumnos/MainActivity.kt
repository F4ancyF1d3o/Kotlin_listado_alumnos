package com.example.kotlin_listado_alumnos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelperAlumno
    private lateinit var rvAlumnos: RecyclerView
    private lateinit var layoutSinAlumnos: View
    private lateinit var fabAgregar: FloatingActionButton
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var adapter: AlumnoAdapter
    private var listaAlumnos = ArrayList<Alumno>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DBHelperAlumno(this)

        rvAlumnos = findViewById(R.id.rvAlumnos)
        layoutSinAlumnos = findViewById(R.id.layoutSinAlumnos)
        fabAgregar = findViewById(R.id.fabAgregar)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        setupRecyclerView()
        setupBottomNavigation()

        fabAgregar.setOnClickListener {
            mostrarDialogoAlumno(null)
        }

        cargarAlumnos()
    }

    private fun setupRecyclerView() {
        adapter = AlumnoAdapter(
            listaAlumnos = listaAlumnos,
            onEditClick = { alumno -> mostrarDialogoAlumno(alumno) },
            onDeleteClick = { alumno -> mostrarDialogoEliminar(alumno) }
        )
        rvAlumnos.layoutManager = LinearLayoutManager(this)
        rvAlumnos.adapter = adapter
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_alumnos -> {
                    rvAlumnos.smoothScrollToPosition(0)
                    true
                }
                R.id.nav_acerca_de -> {
                    mostrarDialogoAcercaDe()
                    true
                }
                else -> false
            }
        }
    }

    private fun cargarAlumnos() {
        listaAlumnos = dbHelper.obtenerAlumnos()
        adapter.actualizarLista(listaAlumnos)

        if (listaAlumnos.isEmpty()) {
            layoutSinAlumnos.visibility = View.VISIBLE
            rvAlumnos.visibility = View.GONE
        } else {
            layoutSinAlumnos.visibility = View.GONE
            rvAlumnos.visibility = View.VISIBLE
        }
    }

    private fun mostrarDialogoAlumno(alumnoExistente: Alumno?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_alumno, null)
        val etNombre = view.findViewById<TextInputEditText>(R.id.etNombre)
        val etCuenta = view.findViewById<TextInputEditText>(R.id.etCuenta)
        val etCorreo = view.findViewById<TextInputEditText>(R.id.etCorreo)
        val etImagen = view.findViewById<TextInputEditText>(R.id.etImagen)

        val esEdicion = alumnoExistente != null

        if (alumnoExistente != null) {
            etNombre.setText(alumnoExistente.nombre)
            etCuenta.setText(alumnoExistente.cuenta)
            etCuenta.isEnabled = false // La cuenta sirve como clave primaria única
            etCorreo.setText(alumnoExistente.correo)
            etImagen.setText(alumnoExistente.imagen)
        }

        val titulo = if (esEdicion) getString(R.string.editar_alumno) else getString(R.string.agregar_alumno)

        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(view)
            .setPositiveButton(R.string.guardar) { _, _ ->
                val nombre = etNombre.text.toString().trim()
                val cuenta = etCuenta.text.toString().trim()
                val correo = etCorreo.text.toString().trim()
                val imagen = etImagen.text.toString().trim()

                if (nombre.isEmpty() || cuenta.isEmpty() || correo.isEmpty()) {
                    Toast.makeText(this, "Por favor llena los campos requeridos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val imagenFinal = if (imagen.isEmpty()) {
                    "https://picsum.photos/id/1025/200/200"
                } else {
                    imagen
                }

                val alumno = Alumno(nombre, cuenta, correo, imagenFinal)

                if (esEdicion) {
                    val resultado = dbHelper.actualizarAlumno(alumno)
                    if (resultado > 0) {
                        Toast.makeText(this, "Alumno actualizado correctamente", Toast.LENGTH_SHORT).show()
                        cargarAlumnos()
                    } else {
                        Toast.makeText(this, "Error al actualizar alumno", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val resultado = dbHelper.insertarAlumno(alumno)
                    if (resultado != -1L) {
                        Toast.makeText(this, "Alumno agregado correctamente", Toast.LENGTH_SHORT).show()
                        cargarAlumnos()
                    } else {
                        Toast.makeText(this, "Error: la cuenta ya está registrada", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun mostrarDialogoEliminar(alumno: Alumno) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.eliminar_alumno))
            .setMessage(getString(R.string.confirmar_eliminacion))
            .setPositiveButton(R.string.si) { _, _ ->
                val resultado = dbHelper.eliminarAlumno(alumno.cuenta)
                if (resultado > 0) {
                    Toast.makeText(this, "Alumno eliminado correctamente", Toast.LENGTH_SHORT).show()
                    cargarAlumnos()
                } else {
                    Toast.makeText(this, "Error al eliminar alumno", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun mostrarDialogoAcercaDe() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.acerca_de_titulo))
            .setMessage(getString(R.string.acerca_de_mensaje))
            .setPositiveButton("Aceptar", null)
            .show()
    }
}
