package com.danidhsm.anime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Created by danidhsm on 3/06/13.
 */
public class EditSerieActivity extends Activity {

    private EditText editNombre;
    private EditText editCurrentEpisode;
    private EditText editEpisodes;
    private EditText editAnio;
    private Spinner editEstado;
    private int id=-1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_serie);

        editNombre = (EditText)findViewById(R.id.editNombre);
        editCurrentEpisode = (EditText)findViewById(R.id.editCurrentEpisode);
        editEpisodes = (EditText)findViewById(R.id.editEpisodes);
        editAnio = (EditText)findViewById(R.id.editAnio);
        editEstado = (Spinner)findViewById(R.id.editEstado);
        Button guardar = (Button)findViewById(R.id.guardar);
        Button eliminar = (Button)findViewById(R.id.eliminar);
        editEstado.setAdapter(new ArrayAdapter<Estado>(this, android.R.layout.simple_spinner_dropdown_item, Estado.values()));



        Bundle reicieveParams = getIntent().getExtras();

        if (reicieveParams != null) {
            id= reicieveParams.getInt("id");
            String nombre = reicieveParams.getString("nombre");
            int currentEpisode = reicieveParams.getInt("currentEpisode");
            int episodes = reicieveParams.getInt("episodes");
            int anio = reicieveParams.getInt("anio");
            String estado = reicieveParams.getString("estado");

            editNombre.setText(nombre);
            editCurrentEpisode.setText(Integer.toString(currentEpisode));
            editEpisodes.setText(Integer.toString(episodes));
            editAnio.setText(Integer.toString(anio));
            editEstado.setSelection(Estado.valueOf(estado).ordinal());

        }

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnParams();
            }
        });

        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("id",EditSerieActivity.this.id);
                setResult(2, intent);
                finish();
            }
        });




    }

    protected void returnParams() {

        Intent intent = new Intent();
        intent.putExtra("id",this.id);
        intent.putExtra("nombre", this.editNombre.getText().toString());
        intent.putExtra("currentEpisode", this.editCurrentEpisode.getText().toString());
        intent.putExtra("episodes", this.editEpisodes.getText().toString());
        intent.putExtra("anio", this.editAnio.getText().toString());
        intent.putExtra("estado", ((Estado)this.editEstado.getSelectedItem()).ordinal());

        setResult(1, intent);
        finish();
    }
}