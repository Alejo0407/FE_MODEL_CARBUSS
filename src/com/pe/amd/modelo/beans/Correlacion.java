package com.pe.amd.modelo.beans;

import java.io.Serializable;

public class Correlacion implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer tipo_doc;
	private String serie;
	private Integer correlativo;
	
	
	public Integer getTipo_doc() {
		return tipo_doc;
	}
	public void setTipo_doc(Integer tipo_doc) {
		this.tipo_doc = tipo_doc;
	}
	public String getSerie() {
		return serie;
	}
	public void setSerie(String serie) {
		this.serie = serie;
	}
	public Integer getCorrelativo() {
		return correlativo;
	}
	public void setCorrelativo(Integer correlativo) {
		this.correlativo = correlativo;
	}
	
	public void aumentarCorrelacion()  {
		if(correlativo.intValue() == 99999999) {
			char c[] = getSerie().toCharArray();
			
			for(int i = serie.length() -1 ; i >= 0 ; i-- ) {
				int val =  (int)serie.charAt( i );
				if( (val >='0' && val < '9') || (val >= 'A' && val < 'Z') ) 
					val++;
				else if( val == '9') 
					val = 'A';
				else 
					val = '0';
				c[i] = (char)val;
				if(val == '0' && i == 0)
					System.err.println("ERROR");
				if( val != '0')
					break;
			}
			setCorrelativo(1);
			setSerie(String.valueOf(c[0]) + String.valueOf(c[1])  + String.valueOf(c[2]));
		}else
			setCorrelativo(correlativo.intValue()+1);		
	}
	public void disminuirCorrelacion()  {
		if(correlativo.intValue() == 1) {
			char c[] = getSerie().toCharArray();
			
			for(int i = serie.length() -1 ; i >= 0 ; i-- ) {
				int val =  (int)serie.charAt( i );
				if( (val > '0' && val <= '9') || (val > 'A' && val <= 'Z') ) 
					val--;
				else if( val == 'A') 
					val = '9';
				else 
					val = '9';
				c[i] = (char)val;
				if(val == '9' && i == 0)
					System.err.println("ERROR");
				if( val != '9')
					break;
			}
			setCorrelativo(99999999);
			setSerie(String.valueOf(c[0]) + String.valueOf(c[1])  + String.valueOf(c[2]));
		}else
			setCorrelativo(correlativo.intValue()-1);		
	}
	
	public static void main(String...strings) throws InterruptedException {
		Correlacion cor = new Correlacion();
		cor.setCorrelativo(523);
		cor.setSerie("000");
		
		while(true) {
			System.out.println(cor.getSerie()+"-"+cor.getCorrelativo());
			cor.disminuirCorrelacion();
			Thread.sleep(500);
		}
	}
}
