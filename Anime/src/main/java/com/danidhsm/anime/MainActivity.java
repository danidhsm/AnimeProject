package com.danidhsm.anime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends Activity implements Observer {

    private Almacen almacen;
    private ListView listviewViendo;
    private ListView listviewQuiero_ver;
    private ListView listviewTodas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //File root = Environment.getExternalStorageDirectory();
        /*if(root.canWrite()){
            File dir = new File(root + "/ejemplo");
            File datafile = new File(dir,"AnimeList.txt");
            try {
                FileWriter datawriter = new FileWriter(datafile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        Log.e("Anime","pinta el main");
        //InputStream input = getResources().openRawResource(R.raw.series);


        try {

            if(almacen==null){
                InputStream input=null;
                try{
                    input = openFileInput("series.txt");
                } catch (FileNotFoundException e){
                    Toast toast = Toast.makeText(this, "no se ha encontrado el archivo interno", Toast.LENGTH_SHORT);
                }

                ArrayList<Tipo> series;
                series= SerieLoader.load(input);

                almacen= new Almacen(series);
            }

            ArrayList<Tipo> seriesViendo= almacen.getViendo();
            ArrayList<Tipo> seriesQuiero_ver= almacen.getQuieroVer();
            ArrayList<Tipo> seriesTodas= almacen.getAll();



            Button nuevo = (Button) findViewById(R.id.nuevaSerie);
            nuevo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, EditSerieActivity.class);
                    startActivityForResult(intent, 1);
                }
            });

            listviewViendo = (ListView) findViewById(R.id.listaViendo);
            AnimeRowAdapter adapterViendo = new AnimeRowAdapter(this, seriesViendo);
            listviewViendo.setAdapter(adapterViendo);


            listviewQuiero_ver = (ListView) findViewById(R.id.listaQuiero_ver);
            AnimeRowAdapter adapterQuiero_ver = new AnimeRowAdapter(this, seriesQuiero_ver);
            listviewQuiero_ver.setAdapter(adapterQuiero_ver);


            listviewTodas = (ListView) findViewById(R.id.listaTodas);
            AnimeRowAdapter adapterTodas = new AnimeRowAdapter(this, seriesTodas);
            listviewTodas.setAdapter(adapterTodas);


            TabHost tab =(TabHost)findViewById(R.id.tabHost);
            tab.setup();
            tab.addTab(tab.newTabSpec("tab1").setIndicator("Viendo ("+seriesViendo.size()+")", null).setContent(R.id.listaViendo));
            tab.addTab(tab.newTabSpec("tab2").setIndicator("Quiero ver ("+seriesQuiero_ver.size()+")", null).setContent(R.id.listaQuiero_ver));
            tab.addTab(tab.newTabSpec("tab3").setIndicator("todas ("+seriesTodas.size()+")", null).setContent(R.id.listaTodas));

            almacen.addObserver(this);

        } catch (FileNotFoundException e) {
            Toast toast = Toast.makeText(this, "no se ha encontrado el archivo", Toast.LENGTH_SHORT);
            toast.show();
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "fallo al leer el fichero el archivo", Toast.LENGTH_SHORT);
            toast.show();
        } catch (Exception e){
            Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void recargarListas(){
        Log.i("Anime","actualizando listas ...");

        AnimeRowAdapter adapterViendo = ((AnimeRowAdapter)listviewViendo.getAdapter());
        adapterViendo.notifyDataSetChanged();
        //listviewViendo.setSelection(listviewViendo.getCount());

        AnimeRowAdapter adapterQuiero_ver = ((AnimeRowAdapter)listviewQuiero_ver.getAdapter());
        adapterQuiero_ver.notifyDataSetChanged();
        //listviewQuiero_ver.setSelection(listviewQuiero_ver.getCount());

        AnimeRowAdapter adapterTodas = ((AnimeRowAdapter)listviewTodas.getAdapter());
        adapterTodas.notifyDataSetChanged();
        //listviewTodas.setSelection(listviewTodas.getCount());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("Anime","-------------");
        if (requestCode == 1 && data!=null) {
            // cogemos el valor devuelto por la otra actividad

            if(resultCode==1){
                Log.i("Anime","nueva o editar");
                int id = data.getIntExtra("id",-1);
                String nombre = data.getStringExtra("nombre");
                String currentEpisode = data.getStringExtra("currentEpisode");
                String episodes = data.getStringExtra("episodes");
                String anio = data.getStringExtra("anio");
                int estado = data.getIntExtra("estado",-1);
                // enseñamos al usuario el resultado

                Tipo serienueva;
                if(id>-1){
                    Log.i("Anime","editar ...");
                    if((serienueva =almacen.getId(id))!=null){
                        Log.i("Anime","existe el id");
                        serienueva.setTitle(nombre);
                        serienueva.setCurrentEpisode(Integer.parseInt(currentEpisode));
                        serienueva.setEpisodes(Integer.parseInt(episodes));
                        serienueva.setAnio(Integer.parseInt(anio));
                        serienueva.setEstado(Estado.values()[estado]);
                        serienueva.actualizar();

                        Log.i("Anime","actualizada la serie");
                        Toast toast = Toast.makeText(this, "Actualizada la serie "+serienueva.getTitle(), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else {
                    Log.i("Anime","nueva ...");
                    serienueva= new Tipo();
                    serienueva.setTitle(nombre);
                    serienueva.setCurrentEpisode(Integer.parseInt(currentEpisode));
                    serienueva.setEpisodes(Integer.parseInt(episodes));
                    serienueva.setAnio(Integer.parseInt(anio));
                    serienueva.setEstado(Estado.values()[estado]);

                    this.almacen.add(serienueva);
                    Log.i("Anime","creada la serie y añadida");
                    Toast toast = Toast.makeText(this, "Creada la serie "+serienueva.getTitle(), Toast.LENGTH_SHORT);
                    toast.show();
                }

            } else if(resultCode==2){
                Log.i("Anime","eliminar ...");
                int id = data.getIntExtra("id",-1);
                Tipo borrar;
                if((borrar=this.almacen.getId(id))!=null){
                    this.almacen.remove(borrar);
                    Log.i("Anime","eliminada");
                    Toast toast = Toast.makeText(this, "Eliminada la serie "+borrar.getTitle(), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            //
        }
    }


    public void exportar(){

        try {
            OutputStreamWriter fout=new OutputStreamWriter(this.openFileOutput("series.txt", Context.MODE_PRIVATE));

            for (Tipo tipo : almacen.getAll()) {
                fout.write(tipo.toString()+System.getProperty("line.separator"));
            }
            fout.close();
            Log.i("Anime","exportando ...");
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "no se puede escribir", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    @Override
    public void update(Observable observable, Object o) {
        recargarListas();
        exportar();
    }
}
