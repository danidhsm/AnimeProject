package com.danidhsm.anime;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
		return tipos;
	}

	private static Tipo parseLine(String line) {
		
		//http://myanimelist.net/modules.php?go=api#animemangasearch
		//curl -u user:password -d http://myanimelist.net/api/anime/search.xml?q=naruto

		Pattern p=Pattern.compile("^(.+)\\[(\\d+)\\/(\\d+|\\?\\?)\\]\\s+(\\d+)\\s+(.+)\\s+(.+,?)");
		Matcher m= p.matcher(line);
		
		if(m.matches()){
			Tipo tipo = new Tipo();
			String titulo=m.group(1);
			String currentEpisode=m.group(2);
			String episodes=m.group(3);
			String year=m.group(4);
			String state=m.group(5);
			String generos=m.group(6);
		
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
			return tipo;
		}
		return null;
	}

	private static String Capitalize(String str){

		String[] words = str.toUpperCase().split(" +");
		String string = "";
		for(String word: words){
		    string += " "+word.replace(word.substring(1), word.substring(1).toLowerCase());
		}
		return string.trim();
	}
}
