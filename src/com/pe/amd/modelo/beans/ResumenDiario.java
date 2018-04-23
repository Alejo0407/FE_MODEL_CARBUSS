package com.pe.amd.modelo.beans;

import java.sql.Blob;

public class ResumenDiario {
	private String fecha;
	private String correlativo;
	private Blob archivo;
	private String nom_archivo;
	private String ticket;
	private Blob arhivo_sunat;
	private String nom_archivo_sunat;
	private String fecha_referencia;
	private String tipo;
	
	
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public String getCorrelativo() {
		return correlativo;
	}
	public void setCorrelativo(String correlativo) {
		this.correlativo = correlativo;
	}
	public Blob getArchivo() {
		return archivo;
	}
	public void setArchivo(Blob archivo) {
		this.archivo = archivo;
	}
	public String getNom_archivo() {
		return nom_archivo;
	}
	public void setNom_archivo(String nom_archivo) {
		this.nom_archivo = nom_archivo;
	}
	public String getTicket() {
		return ticket;
	}
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}
	public Blob getArhivo_sunat() {
		return arhivo_sunat;
	}
	public void setArhivo_sunat(Blob arhivo_sunat) {
		this.arhivo_sunat = arhivo_sunat;
	}
	public String getNom_archivo_sunat() {
		return nom_archivo_sunat;
	}
	public void setNom_archivo_sunat(String nom_archivo_sunat) {
		this.nom_archivo_sunat = nom_archivo_sunat;
	}
	public String getFecha_referencia() {
		return fecha_referencia;
	}
	public void setFecha_referencia(String fecha_referencia) {
		this.fecha_referencia = fecha_referencia;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
}
