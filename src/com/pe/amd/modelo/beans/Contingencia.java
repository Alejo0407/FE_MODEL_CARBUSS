package com.pe.amd.modelo.beans;

import java.io.Serializable;

public class Contingencia implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String periodo;
	private String motivo;
	private String fecemision;
	private String tipodocum;
	private String serie;
	private String numero;
	private String tipodocucli;
	private String numdocucli;
	private String nombrecli;
	private Double valventagra;
	private Double valventaexo;
	private Double valventaina;
	private Double isc;
	private Double igv;
	private Double otroscargos;
	private Double totalcomp;
	private String tipocompnota;
	private String seriemod;
	private String numeromod;
	
	public String getPeriodo() {
		return periodo;
	}
	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}
	public String getMotivo() {
		return motivo;
	}
	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}
	public String getFecemision() {
		return fecemision;
	}
	public void setFecemision(String fecemision) {
		this.fecemision = fecemision;
	}
	public String getTipodocum() {
		return tipodocum;
	}
	public void setTipodocum(String tipodocum) {
		this.tipodocum = tipodocum;
	}
	public String getSerie() {
		return serie;
	}
	public void setSerie(String serie) {
		this.serie = serie;
	}
	public String getNumero() {
		return numero;
	}
	public void setNumero(String numero) {
		this.numero = numero;
	}
	public String getTipodocucli() {
		return tipodocucli;
	}
	public void setTipodocucli(String tipodocucli) {
		this.tipodocucli = tipodocucli;
	}
	public String getNumdocucli() {
		return numdocucli;
	}
	public void setNumdocucli(String numdocucli) {
		this.numdocucli = numdocucli;
	}
	public String getNombrecli() {
		return nombrecli;
	}
	public void setNombrecli(String nombrecli) {
		this.nombrecli = nombrecli;
	}
	public Double getValventagra() {
		return valventagra;
	}
	public void setValventagra(Double valventagra) {
		this.valventagra = valventagra;
	}
	public Double getValventaexo() {
		return valventaexo;
	}
	public void setValventaexo(Double valventaexo) {
		this.valventaexo = valventaexo;
	}
	public Double getValventaina() {
		return valventaina;
	}
	public void setValventaina(Double valventaina) {
		this.valventaina = valventaina;
	}
	public Double getIsc() {
		return isc;
	}
	public void setIsc(Double isc) {
		this.isc = isc;
	}
	public Double getIgv() {
		return igv;
	}
	public void setIgv(Double igv) {
		this.igv = igv;
	}
	public Double getOtroscargos() {
		return otroscargos;
	}
	public void setOtroscargos(Double otroscargos) {
		this.otroscargos = otroscargos;
	}
	public Double getTotalcomp() {
		return totalcomp;
	}
	public void setTotalcomp(Double totalcomp) {
		this.totalcomp = totalcomp;
	}
	public String getTipocompnota() {
		return tipocompnota;
	}
	public void setTipocompnota(String tipocompnota) {
		this.tipocompnota = tipocompnota;
	}
	public String getSeriemod() {
		return seriemod;
	}
	public void setSeriemod(String seriemod) {
		this.seriemod = seriemod;
	}
	public String getNumeromod() {
		return numeromod;
	}
	public void setNumeromod(String numeromod) {
		this.numeromod = numeromod;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
