package com.danidhsm.anime;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerieLoader {

    public static ArrayList<Tipo> load(InputStream file,Activity context) throws IOException {

        BufferedReader buffer = null;
        buffer = new BufferedReader(new InputStreamReader(file));
        String line = null;
        ArrayList<Tipo> tipos = new ArrayList<Tipo>();
        while ((line = buffer.readLine()) != null) {
            tipos.add(SerieLoader.parseLine(line,context));
        }
        Log.e("","carga acabada");
        return tipos;
    }

    private static Tipo parseLine(String line,Activity context) {

        //http://myanimelist.net/modules.php?go=api#animemangasearch
        //curl -u user:password -d http://myanimelist.net/api/anime/search.xml?q=naruto




        //nombre[23/55] 2002 ACABADA genero1,genero2 17/05/2001 23:00:00 lunes

        Pattern p=Pattern.compile("^(.+)\\[(\\d+)/(\\d+|\\?\\?)]\\s+(\\d+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+((0?[1-9]|[12][0-9]|3[01])[-/.](0?[1-9]|1[012])[- /.](19|20)\\d\\d\\s([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9])\\s+(lunes|martes|miercoles|jueves|viernes|none|null)");
        Matcher m= p.matcher(line);
        if(m.matches()){
            final Tipo tipo = new Tipo();
            String titulo=m.group(1);
            String currentEpisode=m.group(2);
            String episodes=m.group(3);
            String year=m.group(4);
            String state=m.group(5);
            String generos=m.group(6);
            String update=m.group(7);
            String day=m.group(12);

            tipo.setTitle(SerieLoader.Capitalize(titulo));

            //((TextView)context.findViewById(R.id.estado)).setText("Cargando "+tipo.getTitle());

            int cap=0;
            try{
                cap=Integer.parseInt(currentEpisode);
            } catch (Exception e){
                //
            }
            int tcap=0;
            try{
                tcap=Integer.parseInt(episodes);
            } catch (Exception e){
                //
            }

            tipo.setCurrentEpisode(cap);
            tipo.setEpisodes(tcap);
            tipo.setAnio(Integer.parseInt(year));
            if(state.equalsIgnoreCase("null")){
                tipo.setEstado(Estado.VIENDO);
            } else {
                tipo.setEstado(Estado.valueOf(state));
            }


            if(day==null){
                tipo.setDay("none");
            } else {
                tipo.setDay(day);
            }

            if(update==null){
                tipo.setUpdate(new Date());
            } else {
                tipo.setUpdate(update);
            }

            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/anime_images");

            String fname = Uri.encode(tipo.getTitle())+".jpg";
            final File file = new File (myDir, fname);
            if(!file.exists() || tipo.getAnio()==0 || tipo.getEpisodes()==0){
                //new NetworkTask(tipo).execute("http://myanimelist.net/api/anime/search.xml?q="+ Uri.encode(tipo.getTitle()));
                int modes[]= new int[3];
                if(!file.exists()){
                    modes[0]=NetworkTask.IMAGE_MODE;
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            tipo.setImageBitmap(decodeSampledBitmapFromResource(file.getAbsolutePath(),120,120));
                        }
                    }).start();
                }
                if(tipo.getAnio()==0){
                    modes[1]=NetworkTask.YEAR_MODE;
                }
                if(tipo.getEpisodes()==0){
                    modes[2]=NetworkTask.EPISODES_MODE;
                }
                SerieLoader.getURLData(tipo,modes);

            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        tipo.setImageBitmap(decodeSampledBitmapFromResource(file.getAbsolutePath(),120,120));
                    }
                }).start();
            }
            return tipo;
        }
        return null;
    }

    public static void getURLData(Tipo tipo,int[] modes){
        new NetworkTask(tipo,modes).execute("http://myanimelist.net/api/anime/search.xml?q="+ Uri.encode(tipo.getTitle()));
    }

    public static void getURLData(Tipo tipo){
        new NetworkTask(tipo).execute("http://myanimelist.net/api/anime/search.xml?q="+ Uri.encode(tipo.getTitle()));
    }

    public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 2;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    private static String Capitalize(String str){

        String[] words = str.toUpperCase().split("\\s+");
        String string = "";
        for(String word: words){
            if(!word.matches("\\d+") && word.length()>1){
                string += " "+word.replace(word.substring(1), word.substring(1).toLowerCase());
            } else {
                string += " "+word;
            }
        }
        return string.trim();
    }

    public static class NetworkTask extends AsyncTask<String, Void, Boolean> {

        private Tipo serie;
        private ArrayList<Integer> modes= new ArrayList<Integer>();
        final static int IMAGE_MODE=1;
        final static int YEAR_MODE=2;
        final static int EPISODES_MODE=3;
        TextView estado;

        public NetworkTask(Tipo serie,int[] modes){
            this.serie=serie;
            for (int mode : modes){
                this.modes.add(mode);
            }
            //Log.e("","constructor de task");
        }

        public NetworkTask(Tipo serie){
            this.serie=serie;
            this.modes.add(NetworkTask.IMAGE_MODE);
            this.modes.add(NetworkTask.EPISODES_MODE);
            this.modes.add(NetworkTask.YEAR_MODE);
            //Log.e("","constructor de task");
        }

        @Override
        protected void onPreExecute() {
            //estado = .findViewById(R.id.estado);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            Thread.currentThread().setName("myanimelist_connect");

            boolean actualizar=false;

            try {
                String link = params[0];
                //Log.e("url:",link);
                HttpGet request = new HttpGet(link);
                request.setHeader("Authorization", "Basic " + Base64.encodeToString("danidhsm:1711dani".getBytes(), Base64.NO_WRAP));
                AndroidHttpClient client = AndroidHttpClient.newInstance(link);

                HttpResponse result;
                result = client.execute(request);

                if (result != null) {
                    String respStr = null;
                    try {
                        Log.e("", "busca patron: " + serie.getTitle());

                        if(result.getEntity()!=null){
                            respStr = EntityUtils.toString(result.getEntity());
                            //Log.e("respuesta",respStr);

                            if (modes.contains(NetworkTask.IMAGE_MODE)) {
                                //Log.e("","busca imagenes");
                                Pattern p = Pattern.compile("<image>(.+)</image>");
                                //Pattern p=Pattern.compile("(.+)");
                                Matcher m = p.matcher(respStr);
                                if (m.find()) {
                                    //Log.e("encontrado",m.group(1));
                                    serie.setUrlImage(m.group(1));
                                    actualizar = true;
                                }
                            }

                            if (modes.contains(NetworkTask.EPISODES_MODE)) {
                                //Log.e("","busca episodios");
                                Pattern p2 = Pattern.compile("<episodes>(.+)</episodes>");
                                //Pattern p=Pattern.compile("(.+)");
                                Matcher m2 = p2.matcher(respStr);
                                if (m2.find()) {
                                    if (serie.getEpisodes() != Integer.parseInt(m2.group(1))) {
                                        serie.setEpisodes(Integer.parseInt(m2.group(1)));
                                        actualizar=true;
                                    }
                                }
                            }

                            if (modes.contains(NetworkTask.YEAR_MODE)) {
                                //Log.e("","busca anios");
                                Pattern p3 = Pattern.compile("<start_date>(\\d{4})-\\d{2}-\\d{2}</start_date>");
                                //Pattern p=Pattern.compile("(.+)");
                                Matcher m3 = p3.matcher(respStr);
                                if (m3.find()) {
                                    if (serie.getAnio() != Integer.parseInt(m3.group(1))) {
                                        serie.setAnio(Integer.parseInt(m3.group(1)));
                                        actualizar=true;
                                    }
                                }
                            }
                        } else {
                            Log.e("", "No hay respuesta de la URL: No se ha encontrado la serie "+ serie.getTitle());
                            Log.e("", "URL: "+link);
                        }
                    } catch (IOException e) {
                        Log.e("", "no encuentra nada");
                        cancel(true);
                    } catch (NumberFormatException e) {
                        Log.e("", "el numero de capitulo o año no cumple con el formato de numero");
                        cancel(true);
                    } catch (ParseException e) {
                        Log.e("", "no se puede parsear el numero de episodio o año");
                        cancel(true);
                    } finally {
                        client.close();
                    }
                }

            } catch (IOException e) {
                Log.e("", "url no valida");
            }
            return actualizar;
        }

        @Override
        protected void onPostExecute(Boolean actualizar) {
            //Do something with result
            if (actualizar){
                serie.actualizar();
            }
        }
    }
}
