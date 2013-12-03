package com.danidhsm.anime;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.EventListener;

/**
 * Created by danidhsm on 1/06/13.
 */
public class AnimeRowAdapter extends ArrayAdapter<Tipo> {

    private LruCache<String, Bitmap> mMemoryCache;

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

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        /*mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };*/

        /*for (Tipo serie : this.series){
            serie.addObserver(this);
        }*/
    }

    /*public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void loadBitmap(String key, ImageView mImageView) {

        final Bitmap bitmap = getBitmapFromMemCache(key);
        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        } else {
            mImageView.setImageResource(R.drawable.ic_launcher);
            BitmapWorkerTask task = new BitmapWorkerTask(mImageView);
            task.execute(resId);
        }
    }*/

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
        }else if (serie.getUrlImage()==null && serie.getImageBitmap()==null){
            holder.imagen.setImageResource(R.drawable.ic_launcher);
        } else {
            holder.imagen.setImageBitmap(serie.getImageBitmap());
        }

        holder.nombre.setText(serie.getTitle());

        View.OnClickListener editar = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditSerieActivity.class);

                // damos valor al parámetro a pasar
                intent.putExtra("image",serie.getImageBitmap());
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
        };

        holder.nombre.setOnClickListener(editar);
        holder.imagen.setOnClickListener(editar);

        holder.currentEpisode.setText(Integer.toString(serie.getCurrentEpisode()));
        holder.episodes.setText(Integer.toString(serie.getEpisodes()));
        if(!serie.getEstado().equals(Estado.ACABADA)){
            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(serie.getCurrentEpisode()<serie.getEpisodes() || serie.getEpisodes()==0){
                        serie.addEpisode();
                        serie.actualizar();
                        if(serie.getEstado().equals(Estado.ACABADA)){
                            ((Button)view.findViewById(R.id.plus)).setClickable(false);
                        }
                    }
                }
            });
        }

        return rowView;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        Tipo serie;
        String url;
        TextView estado;

        public DownloadImageTask(Tipo serie) {
            this.serie = serie;
            this.estado=((TextView)((Activity)context).findViewById(R.id.estado));
        }

        @Override
        protected void onPreExecute(){
            estado.setText("Descargado imagen de "+serie.getTitle());
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            url=urls[0];
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

        @Override
        protected void onPostExecute(Bitmap result) {
            serie.setImageBitmap(result);
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/anime_images");
            myDir.mkdirs();

            String fname = Uri.encode(serie.getTitle())+".jpg";
            File file = new File (myDir, fname);
            if (file.exists ()) file.delete ();
            try {
                FileOutputStream out = new FileOutputStream(file);
                result.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            AnimeRowAdapter.this.notifyDataSetChanged();
            estado.setText("Imagen de "+serie.getTitle()+" DESCARGADA");
            Log.e("","metida");
        }
    }

    /*class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private int data = 0;

    public BitmapWorkerTask(ImageView imageView) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

    // Decode image in background.
    @Override
    @Override
    protected Bitmap doInBackground(Integer... params) {
        final Bitmap bitmap = decodeSampledBitmapFromResource(
                getResources(), params[0], 100, 100));
        addBitmapToMemoryCache(String.valueOf(params[0]), bitmap);
        return bitmap;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}*/
}
