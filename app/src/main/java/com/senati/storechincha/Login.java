package com.senati.storechincha;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Login extends AppCompatActivity {
    private EditText etNombreUsuario, etPassword;
    private Button btnLogin, btnRegistro; // Agregado btnRegistro

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etNombreUsuario = findViewById(R.id.etNombreUsuario);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegistro = findViewById(R.id.btnRegistro); // Referencia al botón de registro

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombreUsuario = etNombreUsuario.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (nombreUsuario.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Login.this, "Por favor, complete todos los campos", Toast.LENGTH_LONG).show();
                } else {
                    new LoginTask(nombreUsuario, password).execute();
                }
            }
        });

        // Acción para el botón de Registro
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Registro.class);
                startActivity(intent);
            }
        });
    }

    private class LoginTask extends AsyncTask<Void, Void, String> {
        private String nombreUsuario, password;

        public LoginTask(String nombreUsuario, String password) {
            this.nombreUsuario = nombreUsuario;
            this.password = password;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("http://192.168.1.16/wstienda/app/services/service-usuario.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("nombre_usuario", nombreUsuario);
                jsonParam.put("contraseña", password);

                OutputStream os = connection.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.close();

                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                BufferedReader br = new BufferedReader(reader);
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                br.close();
                return response.toString();

            } catch (Exception e) {
                Log.e("LOGIN_ERROR", "Error de conexión", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null || result.isEmpty()) {
                Log.e("LOGIN_ERROR", "No hay respuesta del servidor");
                Toast.makeText(Login.this, "Error de conexión con el servidor", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                JSONObject jsonResponse = new JSONObject(result);
                Log.d("JSON_VALIDO", "Respuesta JSON: " + jsonResponse.toString(2));

                boolean success = jsonResponse.optBoolean("status", false);

                if (success) {
                    Toast.makeText(Login.this, "Inicio de sesión exitoso", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String mensajeError = jsonResponse.optString("msg", "Credenciales incorrectas");
                    Toast.makeText(Login.this, mensajeError, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Log.e("LOGIN_ERROR", "Error en el JSON de respuesta: " + result);
                Toast.makeText(Login.this, "Error en el formato de la respuesta", Toast.LENGTH_LONG).show();
            }
        }
    }
}
