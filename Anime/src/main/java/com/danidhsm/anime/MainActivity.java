package com.danidhsm.anime;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    private TabHost tab;
    AnimeRowAdapter adapterViendo;
    AnimeRowAdapter adapterQuiero_ver;
    AnimeRowAdapter adapterTodas;

    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //setContentView(R.layout.activity_main);
    }NO FUNCIONA, SE SIGUE PINTANDO AL CAMBIAR LA ORINETACION*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e("Anime","pinta el main");
        //InputStream input = getResources().openRawResource(R.raw.series);

        tab =(TabHost)findViewById(R.id.tabHost);
        tab.setup();

        listviewViendo = (ListView) findViewById(R.id.listaViendo);
        listviewQuiero_ver = (ListView) findViewById(R.id.listaQuiero_ver);
        listviewTodas = (ListView) findViewById(R.id.listaTodas);

        new SeriesParser().execute();

    }

    private void recargarListas(){
        Log.i("Anime","actualizando listas ...");


        //actualizar texto tabs, da warning pero funciona bien
        ViewGroup tabspec1 = (ViewGroup) tab.getTabWidget().getChildTabViewAt(0);
        TextView tabIndicator1 = (TextView) tabspec1.getChildAt(1);
        tabIndicator1.setText("Viendo ("+almacen.getViendo().size()+")");

        ViewGroup tabspec2 = (ViewGroup) tab.getTabWidget().getChildTabViewAt(1);
        TextView tabIndicator2 = (TextView) tabspec2.getChildAt(1);
        tabIndicator2.setText("Quiero ver ("+almacen.getQuieroVer().size()+")");

        ViewGroup tabspec3 = (ViewGroup) tab.getTabWidget().getChildTabViewAt(2);
        TextView tabIndicator3 = (TextView) tabspec3.getChildAt(1);
        tabIndicator3.setText("todas ("+almacen.getAll().size()+")");


        //AnimeRowAdapter adapterViendo = ((AnimeRowAdapter)listviewViendo.getAdapter());
        adapterViendo.notifyDataSetChanged();
        //listviewViendo.getFirstVisiblePosition();
        //listviewViendo.setSelection(listviewViendo.getCount());

        //AnimeRowAdapter adapterQuiero_ver = ((AnimeRowAdapter)listviewQuiero_ver.getAdapter());
        adapterQuiero_ver.notifyDataSetChanged();
        //listviewQuiero_ver.setSelection(listviewQuiero_ver.getCount());

        //AnimeRowAdapter adapterTodas = ((AnimeRowAdapter)listviewTodas.getAdapter());
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
    protected void onDestroy() {
        super.onDestroy();
        this.listviewViendo=null;
        this.listviewQuiero_ver=null;
        this.listviewTodas=null;
    }

    /*private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }*/

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
                    SerieLoader.getURLData(serienueva);
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

    public void SDexport(){

        new ExportSD().execute();

    }


    @Override
    public void update(Observable observable, Object o) {
        recargarListas();
        exportar();
    }


    class SeriesParser extends AsyncTask<Void, Void, Almacen> {

        private TextView estado;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            estado = (TextView) findViewById(R.id.estado);
            estado.setText("Leyendo Fichero...");
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Leyendo fichero ...");
            pDialog.setCancelable(true);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.show();
        }


        @Override
        protected Almacen doInBackground(Void... voids) {

            Thread.currentThread().setName("Parseador");
            Log.e("Anime","se carga el archivo: "+Thread.currentThread().getName());
            InputStream input=null;

            try{
                input = openFileInput("series.txt");
            } catch (FileNotFoundException e){
                Toast toast = Toast.makeText(MainActivity.this, "no se ha encontrado el archivo interno", Toast.LENGTH_SHORT);
                toast.show();
                cancel(true);
            }

            ArrayList<Tipo> series = new ArrayList<Tipo>();
            try {
                series= SerieLoader.load(input,MainActivity.this);

            } catch (IOException e) {
                Toast toast = Toast.makeText(MainActivity.this, "fallo al leer el fichero", Toast.LENGTH_SHORT);
                toast.show();
                cancel(true);
            }

            return new Almacen(series);

        }

        @Override
        protected void onPostExecute(Almacen almacen) {

            MainActivity.this.almacen= almacen;
            Log.e("Anime","se añaden los adaptadores y eventos: "+Thread.currentThread().getName());
            estado.setText("Estableciendo Listas");

            ArrayList<Tipo> seriesViendo= almacen.getViendo();
            ArrayList<Tipo> seriesQuiero_ver= almacen.getQuieroVer();
            ArrayList<Tipo> seriesTodas= almacen.getAll();


            adapterViendo = new AnimeRowAdapter(MainActivity.this, seriesViendo);
            listviewViendo.setAdapter(adapterViendo);


            adapterQuiero_ver = new AnimeRowAdapter(MainActivity.this, seriesQuiero_ver);
            listviewQuiero_ver.setAdapter(adapterQuiero_ver);


            adapterTodas = new AnimeRowAdapter(MainActivity.this, seriesTodas);
            listviewTodas.setAdapter(adapterTodas);


            tab.addTab(tab.newTabSpec("tab1").setIndicator("Viendo ("+seriesViendo.size()+")", null).setContent(R.id.listaViendo));
            tab.addTab(tab.newTabSpec("tab2").setIndicator("Quiero ver ("+seriesQuiero_ver.size()+")", null).setContent(R.id.listaQuiero_ver));
            tab.addTab(tab.newTabSpec("tab3").setIndicator("todas ("+seriesTodas.size()+")", null).setContent(R.id.listaTodas));

            almacen.addObserver(MainActivity.this);


            //botones
            Button nuevo = (Button) findViewById(R.id.nuevaSerie);
            nuevo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, EditSerieActivity.class);
                    startActivityForResult(intent, 1);
                }
            });

            ImageButton refresh= (ImageButton) findViewById(R.id.actualizar);
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recargarListas();
                    Log.e("Anime","se recargan las listas");
                }
            });

            ImageButton export = (ImageButton) findViewById(R.id.exportar);
            export.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SDexport();
                }
            });

            //-----------------------------------


            pDialog.dismiss();
            estado.setText("Carga Completada");

        }

        @Override
        protected void onCancelled(Almacen almacen){
            Toast toast = Toast.makeText(MainActivity.this, "se han leido "+almacen.getAll().size()+" series", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    class ExportSD extends AsyncTask<Void, Integer, Void> {

        private ProgressBar progress;
        private TextView estado;

        @Override
        protected void onPreExecute() {
            progress = (ProgressBar) findViewById(R.id.generalLoad);
            progress.setMax(almacen.getAll().size());
            progress.setProgress(0);
            estado = (TextView) findViewById(R.id.estado);
            estado.setText("Exportando a SD");
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Thread.currentThread().setName("exportadorSD");

            Log.e("Anime", "Exportando a SD");

            boolean sdDisponible = false;
            boolean sdAccesoEscritura = false;

            //Comprobamos el estado de la memoria externa (tarjeta SD)
            String estado = Environment.getExternalStorageState();


            if (estado.equals(Environment.MEDIA_MOUNTED))
            {
                sdDisponible = true;
                sdAccesoEscritura = true;
            }
            else if (estado.equals(Environment.MEDIA_MOUNTED_READ_ONLY))
            {
                sdDisponible = true;
                sdAccesoEscritura = false;
            }
            else
            {
                sdDisponible = false;
                sdAccesoEscritura = false;
            }


            try
            {
                int i=0;
                File ruta_sd = Environment.getExternalStorageDirectory();

                File f = new File(ruta_sd.getAbsolutePath(), "series.txt");

                OutputStreamWriter fout =
                        new OutputStreamWriter(
                                new FileOutputStream(f));

                int total= almacen.getAll().size();
                for (Tipo tipo : almacen.getAll()) {
                    fout.write(tipo.toString()+System.getProperty("line.separator"));
                    publishProgress(++i);
                }
                fout.close();
            }
            catch (Exception e)
            {
                Log.e("Ficheros", "Error al escribir fichero a tarjeta SD");
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... numero){
            progress.setProgress(numero[0]);
            estado.setText(Math.round((float)(numero[0]*100)/almacen.getAll().size())+" %");
        }

        @Override
        protected void onPostExecute(Void vacio) {
            estado.setText("Exportado con exito a SD");
            //progress.setProgress(0);
        }

    }


}
