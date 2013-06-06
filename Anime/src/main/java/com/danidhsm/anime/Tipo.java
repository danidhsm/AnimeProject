package com.danidhsm.anime;

import java.util.Observable;


public class Tipo extends Observable {

	private String title;
	private Estado estado;
	private int anio;
	private Genero[] generos;
	private int episodes;
	private int currentEpisode;
    private int id;
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
	}
	
	public int getAnio() {
		return anio;
	}
	public void setAnio(int anio) {
		this.anio = anio;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getCurrentEpisode() {
        return currentEpisode;
	}
	public void setCurrentEpisode(int currentEpisode) {
		this.currentEpisode = currentEpisode;
	}
	public int getEpisodes() {
		return episodes;
	}
	public void setEpisodes(int episodes) {
		this.episodes = episodes;
	}
	
	public String toString(){
		return this.title+"["+this.currentEpisode+"/"+((this.episodes==0)?"??":this.episodes)+"]"+" "+this.anio+" "+this.estado.name()+" "+this.generos;
	}

	public void addEpisode(){
		this.currentEpisode++;
	}

    public void actualizar(){
        setChanged();
        notifyObservers();
    }
}
