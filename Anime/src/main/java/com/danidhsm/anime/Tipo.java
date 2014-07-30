package com.danidhsm.anime;

import android.graphics.Bitmap;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Observable;


public class Tipo extends Observable {

	private String title;
	private Estado estado;
	private int anio;
	private Genero[] generos;
	private int episodes;
	private int currentEpisode;
    private String day;
    private Date date;
    private int id;
    private String urlImage;
    private Bitmap imageBitmap;
    private static int tipos=0;

    public Tipo(){
        this.id=tipos++;
    }

    public int getId(){
        return this.id;
    }

	public Estado getEstado() {
		return estado;
	}
	public void setEstado(Estado estado) {
		this.estado = estado;
        this.setUpdate(new Date());
	}

    public void setUrlImage(String url){
        this.urlImage=url;
    }

    public String getUrlImage(){
        return this.urlImage;
    }

    public void setImageBitmap(Bitmap bitmap){
        this.imageBitmap=bitmap;
    }

    public Bitmap getImageBitmap(){
        return this.imageBitmap;
    }

    public void setUpdate(Date date){
        this.date=date;
    }

    public void setUpdate(String date){
        SimpleDateFormat formatoDelTexto = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        try {
            this.date = formatoDelTexto.parse(date);
        } catch (ParseException ex) {
            this.date=new Date();
        }
    }

    public void setDay(String day){
        this.day=day;
        this.setUpdate(new Date());
    }

    public void setDay(Date day){
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(day);
        String dayString;
        switch (cal.get(Calendar.DAY_OF_WEEK)){
            case 1:
                dayString="lunes";
                break;
            case 2:
                dayString="martes";
                break;
            case 3:
                dayString="miercoles";
                break;
            case 4:
                dayString="jueves";
                break;
            case 5:
                dayString="viernes";
                break;
            case 6:
                dayString="sabado";
                break;
            case 7:
                dayString="domingo";
                break;
            default:
                dayString="none";
        }

        this.day=dayString;
        this.setUpdate(new Date());
    }

	public int getAnio() {
		return anio;
	}
	public void setAnio(int anio) {
		this.anio = anio;
        this.setUpdate(new Date());
	}

    public Date getDate(){
        return this.date;
    }
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
        this.setUpdate(new Date());
	}
	public int getCurrentEpisode() {
        return currentEpisode;
	}
	public void setCurrentEpisode(int currentEpisode) {
		this.currentEpisode = currentEpisode;
        this.setUpdate(new Date());
	}
	public int getEpisodes() {
		return episodes;
	}
	public void setEpisodes(int episodes) {
		this.episodes = episodes;
        this.setUpdate(new Date());
	}
	
	public String toString(){
		return this.title+"["+this.currentEpisode+"/"+((this.episodes==0)?"??":this.episodes)+"]"+" "+this.anio+" "+this.estado.name()+" "+this.generos+" "+new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(this.date)+" "+this.day;
	}

    public String toJSON(){

        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("title", this.title);
            jsonObject.put("episodes", this.episodes);
            jsonObject.put("currentEpisode", this.currentEpisode);
            jsonObject.put("year", this.anio);
            jsonObject.put("genders", this.generos);
            jsonObject.put("updated_at", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(this.date));
            jsonObject.put("newEpisodeDay", this.day);
            //jsonObject.put("rank", this.rank);


            return jsonObject.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }

	public void addEpisode(){
		this.currentEpisode++;
        if(this.currentEpisode==this.episodes){
            this.estado=Estado.ACABADA;
        }
        this.setUpdate(new Date());
	}

    public void actualizar(){
        setChanged();
        notifyObservers();
    }
}
