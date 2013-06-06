package com.danidhsm.anime;

public enum Estado {

    ACABADA("La he ACABADO"),VIENDO("La estoy VIENDO"),ABANDONAD("La he ABANDONADO"),ESPERANDO("Estoy ESPERANDO una nueva temporada"),QUIERO_VER("QUIERO VERLA cuando pueda");

    private String friendlyName;

    private Estado(String friendlyName){
        this.friendlyName= friendlyName;
    }

    public String toString(){
        return friendlyName;
    }
	
}
