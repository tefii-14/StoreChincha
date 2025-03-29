package com.senati.storechincha;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Buscar extends AppCompatActivity {

    private final String URLWS = "http://192.168.1.16/wstienda/app/services/service-producto.php";
    RequestQueue requestQueue;

    EditText edtID, edtTipo, edtGenero, edtTalla, edtPrecio;
    Button btnBuscarProducto, btnActualizarProducto, btnEliminarProducto, btnCancelarProducto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buscar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            return insets;
        });

        loadUI();

        btnBuscarProducto.setOnClickListener(v -> buscarProducto());
        btnActualizarProducto.setOnClickListener(v -> actualizarProducto());
        btnEliminarProducto.setOnClickListener(v -> confirmarEliminacion());

        btnCancelarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                limpiarCampos();
                Toast.makeText(Buscar.this, "Formulario limpiado", Toast.LENGTH_SHORT).show();
            }
        });

        adBotones(false);
    }

    private void adBotones(boolean sw) {
        btnEliminarProducto.setEnabled(sw);
        btnActualizarProducto.setEnabled(sw);
        btnCancelarProducto.setEnabled(sw);
    }

    private void buscarProducto() {
        requestQueue = Volley.newRequestQueue(this);
        String idProducto = edtID.getText().toString().trim();
        if (idProducto.isEmpty()) {
            showToast("Ingrese un ID para buscar");
            return;
        }

        String URLparams = Uri.parse(URLWS)
                .buildUpon()
                .appendQueryParameter("q", "findById")
                .appendQueryParameter("id", idProducto)
                .build()
                .toString();

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                URLparams,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            if (response.length() > 0) {
                                JSONObject jsonObject = response.getJSONObject(0);
                                edtTipo.setText(jsonObject.getString("tipo"));
                                edtGenero.setText(jsonObject.getString("genero"));
                                edtTalla.setText(jsonObject.getString("talla"));
                                edtPrecio.setText(jsonObject.getString("precio"));
                                adBotones(true);
                            } else {
                                showToast("Producto no encontrado");
                                resetUI();
                            }
                        } catch (JSONException e) {
                            Log.e("ErrorJSON", "Error al parsear JSON: " + e.getMessage());
                            showToast("Error en la respuesta del servidor");
                        }
                    }
                },
                error -> {
                    Log.e("ErrorWS", "Error en la solicitud: " + error.toString());
                    showToast("Error al conectar con el servidor");
                }
        );
        requestQueue.add(request);
    }

    private void actualizarProducto() {
        requestQueue = Volley.newRequestQueue(this);
        String idProducto = edtID.getText().toString().trim();
        if (idProducto.isEmpty()) {
            showToast("Ingrese un ID para actualizar");
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("id", idProducto);
            jsonBody.put("tipo", edtTipo.getText().toString().trim());
            jsonBody.put("genero", edtGenero.getText().toString().trim());
            jsonBody.put("talla", edtTalla.getText().toString().trim());
            jsonBody.put("precio", edtPrecio.getText().toString().trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.PUT, URLWS, jsonBody,
                response -> showToast("Producto actualizado correctamente"),
                error -> {
                    Log.e("ErrorWS", "Error al actualizar: " + error.toString());
                    showToast("Error al actualizar producto");
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        requestQueue.add(jsonRequest);
    }

    private void limpiarCampos() {
        edtID.setText("");
        edtTipo.setText("");
        edtGenero.setText("");
        edtTalla.setText("");
        edtPrecio.setText("");
    }


    private void eliminarProducto() {
        requestQueue = Volley.newRequestQueue(this);
        String idProducto = edtID.getText().toString().trim();
        if (idProducto.isEmpty()) {
            showToast("Ingrese un ID para eliminar");
            return;
        }

        String URLDELETE = URLWS + "/" + idProducto; // Agregar ID al final de la URL

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, URLDELETE,
                response -> {
                    showToast("Producto eliminado correctamente");
                    resetUI();
                },
                error -> {
                    Log.e("ErrorWS", "Error al eliminar: " + error.toString());
                    showToast("Error al eliminar producto");
                });

        requestQueue.add(stringRequest);
    }

    private void confirmarEliminacion() {
        AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
        dialogo.setTitle("StoreChincha");
        dialogo.setMessage("¿Seguro de eliminar?");
        dialogo.setCancelable(false);
        dialogo.setNegativeButton("No", null);
        dialogo.setPositiveButton("Sí", (dialog, which) -> eliminarProducto());
        dialogo.create().show();
    }

    private void resetUI() {
        edtTipo.setText(null);
        edtGenero.setText(null);
        edtTalla.setText(null);
        edtPrecio.setText(null);
        adBotones(false);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loadUI() {
        edtID = findViewById(R.id.edtID);
        edtTipo = findViewById(R.id.edtTipoEdit);
        edtGenero = findViewById(R.id.edtGeneroEdit);
        edtTalla = findViewById(R.id.edtTallaEdit);
        edtPrecio = findViewById(R.id.edtPrecioEdit);

        btnBuscarProducto = findViewById(R.id.btnBuscarProducto);
        btnActualizarProducto = findViewById(R.id.btnActualizarProducto);
        btnEliminarProducto = findViewById(R.id.btnEliminarProducto);
        btnCancelarProducto = findViewById(R.id.btnCancelarProducto);
    }
}