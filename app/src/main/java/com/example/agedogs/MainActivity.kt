package com.example.agedogs;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText etEdadPerro;
    Button btnCalcular, btnLimpiar;
    TextView tvResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEdadPerro = findViewById(R.id.etEdadPerro);
        btnCalcular = findViewById(R.id.btnCalcular);
        btnLimpiar = findViewById(R.id.btnLimpiar);
        tvResultado = findViewById(R.id.tvResultado);

        btnCalcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calcularEdad();
            }
        });

        btnLimpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etEdadPerro.setText("");
                tvResultado.setText("");
            }
        });
    }

    private void calcularEdad() {
        String texto = etEdadPerro.getText().toString().trim();

        if (texto.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa la edad del perro", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double edadPerro = Double.parseDouble(texto);

            if (edadPerro <= 0) {
                Toast.makeText(this, "La edad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return;
            }

            double edadHumana = convertirEdad(edadPerro);
            tvResultado.setText("Edad en años humanos: " + String.format("%.1f", edadHumana) + " años");

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingresa un número válido", Toast.LENGTH_SHORT).show();
        }
    }

    private double convertirEdad(double edadPerro) {
        if (edadPerro <= 2) {
            return edadPerro * 12.5;
        } else {
            return 25 + (edadPerro - 2) * 4;
        }
    }
}