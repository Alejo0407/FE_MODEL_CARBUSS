package com.pe.amd.modelo.beans;

import java.io.Serializable;

public class Detdocumentos implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String transaccion;
	private String sec;
	private String codigo;
	private String denominacion;
	private String unidad;
	private Double valunitario;
	private Double cantidad;
	private Double igv;
	private String codigv;
	private Double valtotal;
	
	
	public String getTransaccion() {
		return transaccion;
	}
	public void setTransaccion(String transaccion) {
		this.transaccion = transaccion;
	}
	public String getSec() {
		return sec;
	}
	public void setSec(String sec) {
		this.sec = sec;
	}
	public String getCodigo() {
		return codigo;
	}
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	public String getDenominacion() {
		return denominacion;
	}
	public void setDenominacion(String denominacion) {
		this.denominacion = denominacion;
	}
	public String getUnidad() {
		return unidad;
	}
	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}
	public Double getValunitario() {
		return valunitario;
	}
	public void setValunitario(Double valunitario) {
		this.valunitario = valunitario;
	}
	public Double getCantidad() {
		return cantidad;
	}
	public void setCantidad(Double cantidad) {
		this.cantidad = cantidad;
	}
	public Double getIgv() {
		return igv;
	}
	public void setIgv(Double igv) {
		this.igv = igv;
	}
	public String getCodigv() {
		return codigv;
	}
	public void setCodigv(String codigv) {
		this.codigv = codigv;
	}
	public Double getValtotal() {
		return valtotal;
	}
	public void setValtotal(Double valtotal) {
		this.valtotal = valtotal;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
