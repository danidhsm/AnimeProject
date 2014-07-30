package com.danidhsm.anime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by danidhsm on 3/06/13.
 */
public class Almacen extends Observable implements Observer {

    private ArrayList<Tipo> series;
    private ArrayList<Tipo> cacheViendo=new ArrayList<Tipo>();
    private ArrayList<Tipo> cacheQuiero_ver=new ArrayList<Tipo>();

    private boolean changedViendo;
    private boolean changedQuiero_ver;

    public Almacen(ArrayList<Tipo> series){
        this.series=series;
        for (Tipo serie : this.series){
            serie.addObserver(this);
        }
        titleSort();
        changed();
    }


    public void add(Tipo serie){
        series.add(serie);
        serie.addObserver(this);
        changed();
        setChanged();
        notifyObservers();
    }

    public void remove(Tipo borrar){
        this.series.remove(borrar);
        changed();
        setChanged();
        notifyObservers();
    }

    public void add(Tipo[] series){
        for (Tipo serie : this.series){
            if(serie.getEstado().equals(Estado.VIENDO)){
                this.series.add(serie);
                serie.addObserver(this);
            }
        }
        changed();
        setChanged();
        notifyObservers();
    }

    public ArrayList<Tipo> getViendo(){
        if(changedViendo){
            cacheViendo.clear();
            for (Tipo serie : this.series){
                if(serie.getEstado().equals(Estado.VIENDO)){
                    cacheViendo.add(serie);
                }
            }
            this.dateSort();
            changedViendo=false;
        }

        return cacheViendo;
    }

    public ArrayList<Tipo> getQuieroVer(){
        if(changedQuiero_ver){
            cacheQuiero_ver.clear();
            for (Tipo serie : this.series){
                if(serie.getEstado().equals(Estado.QUIERO_VER)){
                    cacheQuiero_ver.add(serie);
                }
            }
            changedQuiero_ver=false;
        }

        return cacheQuiero_ver;
    }

    public ArrayList<Tipo> getAll(){
        return this.series;
    }

    public Tipo getId(int id){
        for (Tipo serie : this.series){
            if(serie.getId()==id){
                return serie;
            }
        }
        return null;
    }

    public void changed(){
        changedViendo=true;
        changedQuiero_ver=true;
        getViendo();
        getQuieroVer();
    }

    @Override
    public void update(Observable observable, Object o) {
        changed();
        setChanged();
        notifyObservers();
    }

    class TitleComparator implements Comparator<Tipo>{

        @Override
        public int compare(Tipo tipo, Tipo tipo2) {
            return tipo.getTitle().compareToIgnoreCase(tipo2.getTitle());
        }
    }

    public void  titleSort(){
        Collections.sort(this.series, new TitleComparator());
    }

    class YearComparator implements Comparator<Tipo>{

        @Override
        public int compare(Tipo tipo, Tipo tipo2) {
            if(tipo.getAnio()>tipo2.getAnio()){
                return 1;
            } else if(tipo.getAnio()==tipo2.getAnio()){
                return 0;
            } else return -1;
        }
    }

    public void dateSort(){
        Collections.sort(this.cacheViendo, new DateComparator());
    }

    class DateComparator implements Comparator<Tipo>{

        @Override
        public int compare(Tipo tipo, Tipo tipo2) {
            //Log.e("",tipo.getTitle()+" "+tipo.getDate().toString()+" "+tipo2.getDate().toString()+" => "+tipo.getDate().compareTo(tipo2.getDate())+"");
            return tipo.getDate().compareTo(tipo2.getDate())*-1;
        }
    }

    public void  yearSort(){
        Collections.sort(this.series, new YearComparator());
    }

    class diffComparator implements Comparator<Tipo>{

        @Override
        public int compare(Tipo tipo, Tipo tipo2) {

            int diff1=tipo.getEpisodes()-tipo.getCurrentEpisode();
            int diff2= tipo2.getEpisodes()-tipo2.getCurrentEpisode();

            if(diff2<diff1){
                return 1;
            } else if(diff1==diff2){
                return 0;
            } else return -1;

        }
    }

    public void  diffSort(){
        Collections.sort(this.series, new diffComparator());
    }

}
