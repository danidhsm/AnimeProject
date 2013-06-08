package com.danidhsm.anime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by danidhsm on 1/06/13.
 */
public class AnimeRowAdapter extends ArrayAdapter<Tipo> {

    private ArrayList<Tipo> series;
    private Context context;

    public void updateData(ArrayList<Tipo> series){
        this.series=series;
        this.notifyDataSetChanged();
    }
    static class ViewHolder {
        public TextView nombre;
        public TextView currentEpisode;
        public TextView episodes;
        public Button add;
        public ImageView imagen;
    }

    public AnimeRowAdapter(Context context, ArrayList<Tipo> series) {
        super(context,R.layout.anime_row,series);
        this.context = context;
        this.series = series;
        /*for (Tipo serie : this.series){
            serie.addObserver(this);
        }*/
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        View rowView = convertView;

        final Tipo serie=series.get(position);

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.anime_row, parent, false);

            ViewHolder viewHolder = new ViewHolder();

            viewHolder.imagen           = (ImageView) rowView.findViewById(R.id.imageSerie);
            viewHolder.nombre           = (TextView) rowView.findViewById(R.id.nameSerie);
            viewHolder.currentEpisode   = (TextView) rowView.findViewById(R.id.currentEpisode);
            viewHolder.episodes         = (TextView) rowView.findViewById(R.id.episodes);
            viewHolder.add              = (Button) rowView.findViewById(R.id.plus);

            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();


        if(serie.getUrlImage()!=null && serie.getImageBitmap()==null){
            new DownloadImageTask(serie).execute(serie.getUrlImage());
            //Log.e("row","se ha iniciado el hilo de la imagen");
        }else if (serie.getUrlImage()==null){
            holder.imagen.setImageResource(R.drawable.ic_launcher);
        } else {
            holder.imagen.setImageBitmap(serie.getImageBitmap());
        }

        holder.nombre.setText(serie.getTitle());
        holder.nombre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditSerieActivity.class);

                // damos valor al parámetro a pasar
                //intent.putExtra("image",serie.getImageBitmap().getNinePatchChunk());
                intent.putExtra("id",serie.getId());
                intent.putExtra("nombre", serie.getTitle());
                intent.putExtra("currentEpisode", serie.getCurrentEpisode());
                intent.putExtra("episodes", serie.getEpisodes());
                intent.putExtra("anio", serie.getAnio());
                intent.putExtra("estado", serie.getEstado().name());

                //context.startActivity(intent);
                //((Activity)context).finish();
                ((Activity)context).startActivityForResult(intent,1);
            }
        });
        holder.currentEpisode.setText(Integer.toString(serie.getCurrentEpisode()));
        holder.episodes.setText(Integer.toString(serie.getEpisodes()));
        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serie.addEpisode();
                serie.actualizar();
            }
        });

        return rowView;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        Tipo serie;

        public DownloadImageTask(Tipo serie) {
            this.serie = serie;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Log.e("",urldisplay);
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            serie.setImageBitmap(result);
            AnimeRowAdapter.this.notifyDataSetChanged();
            Log.e("","metida");
        }
    }
}
