package com.danidhsm.anime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

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

        holder.nombre.setText(serie.getTitle());
        holder.nombre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditSerieActivity.class);

                // damos valor al parámetro a pasar
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

        // Change the icon for Windows and iPhone
        /*String s = values[position];
        if (s.startsWith("iPhone")) {
            imageView.setImageResource(R.drawable.no);
        } else {
            imageView.setImageResource(R.drawable.ok);
        }*/

        return rowView;
    }
}
