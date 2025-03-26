package com.senati.storechincha;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.senati.storechincha.adaptadores.ProductoAdapter;
import com.senati.storechincha.entidades.Producto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Listar extends AppCompatActivity {

    private final String URLWS = "http://192.168.2.79/wstienda/app/services/service-producto.php";

    RequestQueue requestQueue;

    ListView lstProductos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            //v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadUI();

        obtenerRegistros();
    }

    private void obtenerRegistros(){
        requestQueue = Volley.newRequestQueue(this);

        final String URL_LISTAR = URLWS + "?q=showAll";

        JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL_LISTAR,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        procesarDatos(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ErrorWS", error.toString());
                    }
                }
        );

        requestQueue.add(jsonRequest);
    }

    private void procesarDatos(JSONArray response){

        try {
            Producto producto;
            ArrayList<Producto> listaProductos = new ArrayList<>();
            //ArrayList<String> listaSimple = new ArrayList<>();

            for (int i = 0; i < response.length(); i++){
                JSONObject jsonObject = response.getJSONObject(i);
                //listaSimple.add(jsonObject.getString("tipo") + " " + jsonObject.getString("genero"));

                producto = new Producto();
                producto.setId(jsonObject.getInt("id"));
                producto.setTipo(jsonObject.getString("tipo"));
                producto.setGenero(jsonObject.getString("genero"));
                producto.setTalla(jsonObject.getString("talla"));
                producto.setPrecio(jsonObject.getString("precio"));

                listaProductos.add(producto);
            }

            ProductoAdapter adaptador = new ProductoAdapter(this, listaProductos);
            lstProductos.setAdapter(adaptador);

        } catch (Exception e) {
            Log.e("ErrorResponse", e.toString());
        }
    }

    private void renderBasico(JSONArray response){
        try{
            //1. Contenedor de datos a mostrar en ListView
            ArrayList<String> listaSimple = new ArrayList<>();

            //2. Objeto para almacenar 1 JSON
            JSONObject jsonObject;

            //3. Recorremos el response(JSONArray)
            for (int i = 0; i < response.length(); i++){

                //4. Cada JSOM encontrado sera almacenado temporalmente en jsonObject
                jsonObject = new JSONObject(response.get(i).toString());

                //5. Cargando datos en la lista...
                listaSimple.add(jsonObject.getString("tipo") + " " + jsonObject.getString("genero"));
            }

            //6.Adaptador (medio para enviar los datos al ListView)
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listaSimple);

            //7. ListView ewcibw los datos
            lstProductos.setAdapter(adapter);
        } catch (Exception e) {
            Log.e("ErrorResponse", e.toString());
        }
    }
    private void loadUI(){
        lstProductos = findViewById(R.id.lstProductos);
    }
}