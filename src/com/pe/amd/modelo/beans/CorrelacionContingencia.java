package com.pe.amd.modelo.beans;

import java.io.Serializable;
import java.sql.Blob;

public class CorrelacionContingencia implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2470947288156432965L;
	
	
	private String fecha;
	private Integer correlativo;
	private Blob archivo;
	private String nom_archivo;
	
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public Integer getCorrelativo() {
		return correlativo;
	}
	public void setCorrelativo(Integer correlativo) {
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
}
