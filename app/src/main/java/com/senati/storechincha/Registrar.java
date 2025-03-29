package com.senati.storechincha;

import android.content.DialogInterface;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class Registrar extends AppCompatActivity {

    private final String URLWS = "http://192.168.1.16/wstienda/app/services/service-producto.php";

    RequestQueue requestQueue;

    EditText edtTipo, edtGenero, edtTalla, edtPrecio;
    Button btnRegistrarProducto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            //v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadUI();

        btnRegistrarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmar();
            }
        });
    }

    private void registrarDatos(){
        requestQueue = Volley.newRequestQueue(this);

        JSONObject datos = new JSONObject();

        try{
            datos.put("tipo", edtTipo.getText().toString());
            datos.put("genero", edtGenero.getText().toString());
            datos.put("talla", edtTalla.getText().toString());
            datos.put("precio", edtPrecio.getText().toString());

        }catch (Exception e){
            Log.e("ErrorJSON", e.toString());
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.POST,
                URLWS,
                datos,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Guardado", response.toString());

                        try{
                            boolean guardado = response.getBoolean("status");
                            if (guardado)
                                showToast("Proceso ejecutado correctamente");
                            else
                                showToast("No se pudo registrar");
                        }
                        catch (Exception e) {
                            Log.e("ErrorWS", e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showToast("Error en la comunicacion WS");
                    }
                }
        );

        requestQueue.add(jsonRequest);
    }

    private void confirmar(){
        AlertDialog.Builder dialogo = new AlertDialog.Builder(this);

        dialogo.setTitle("STORECHINCHA");
        dialogo.setMessage("¿Registramos al producto?");
        dialogo.setCancelable(true);

        dialogo.setNegativeButton("No", null);
        dialogo.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                registrarDatos();   //JSON envio
            }
        });
        dialogo.create().show();
    }

    private void  showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loadUI(){
        edtTipo = findViewById(R.id.edtTipo);
        edtGenero = findViewById(R.id.edtGenero);
        edtTalla = findViewById(R.id.edtTalla);
        edtPrecio = findViewById(R.id.edtPrecio);

        btnRegistrarProducto = findViewById(R.id.btnRegistrarProducto);
    }
}