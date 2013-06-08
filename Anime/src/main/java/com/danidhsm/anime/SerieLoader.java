package com.danidhsm.anime;

import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerieLoader {

    public static ArrayList<Tipo> load(InputStream file) throws IOException {

        BufferedReader buffer = null;
        buffer = new BufferedReader(new InputStreamReader(file));
        String line = null;
        ArrayList<Tipo> tipos = new ArrayList<Tipo>();
        while ((line = buffer.readLine()) != null) {
            tipos.add(SerieLoader.parseLine(line));
        }
        Log.e("","carga acabada");
        return tipos;
    }

    private static Tipo parseLine(String line) {

        //http://myanimelist.net/modules.php?go=api#animemangasearch
        //curl -u user:password -d http://myanimelist.net/api/anime/search.xml?q=naruto




        Pattern p=Pattern.compile("^(.+)\\[(\\d+)\\/(\\d+|\\?\\?)\\]\\s+(\\d+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+((0?[1-9]|[12][0-9]|3[01])[-/.](0?[1-9]|1[012])[- /.](19|20)?\\d\\d)\\s+(lunes|martes|miercoles|jueves|viernes|none|null)");
        Matcher m= p.matcher(line);
        if(m.matches()){
            Tipo tipo = new Tipo();
            String titulo=m.group(1);
            String currentEpisode=m.group(2);
            String episodes=m.group(3);
            String year=m.group(4);
            String state=m.group(5);
            String generos=m.group(6);
            String update=m.group(7);
            String day=m.group(11);

            tipo.setTitle(SerieLoader.Capitalize(titulo));
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


            new NetworkTask(tipo).execute("http://myanimelist.net/api/anime/search.xml?q="+ Uri.encode(tipo.getTitle()));

            return tipo;
        }
        return null;
    }

    private static String Capitalize(String str){

        String[] words = str.toUpperCase().split("\\s+");
        String string = "";
        for(String word: words){
            string += " "+word.replace(word.substring(1), word.substring(1).toLowerCase());
        }
        return string.trim();
    }

    private static class NetworkTask extends AsyncTask<String, Void, HttpResponse> {

        private Tipo serie;

        public NetworkTask(Tipo serie){
            this.serie=serie;
            //Log.e("","constructor de task");
        }

        @Override
        protected HttpResponse doInBackground(String... params) {

            try{
                String link = params[0];
                //Log.e("url:",link);
                HttpGet request = new HttpGet(link);
                request.setHeader("Authorization", "Basic " + Base64.encodeToString("danidhsm:1711dani".getBytes(),Base64.NO_WRAP));
                AndroidHttpClient client = AndroidHttpClient.newInstance(link);
                try {
                    return client.execute(request);
                } catch (Exception e) {
                    Log.e("","no encuentra nada");
                    return null;
                } finally {
                    client.close();
                }
            } catch(Exception e){
                Log.e("","url no valida");
                return null;
            }
        }

        @Override
        protected void onPostExecute(HttpResponse result) {
            //Do something with result
            if (result != null){
                String respStr = null;
                try {
                    //Log.e("","busca patron");
                    respStr = EntityUtils.toString(result.getEntity());
                    //Log.e("respuesta",respStr);
                    Pattern p=Pattern.compile("<image>(.+)</image>");
                    //Pattern p=Pattern.compile("(.+)");
                    Matcher m= p.matcher(respStr);
                    if(m.find()){
                        //Log.e("encontrado",m.group(1));
                        serie.setUrlImage(m.group(1));
                    }
                } catch (Exception e) {
                    //Log.e("","la peticion da error");
                }
            }
        }
    }
}
