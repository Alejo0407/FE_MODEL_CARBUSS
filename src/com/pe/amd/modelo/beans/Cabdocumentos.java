package com.pe.amd.modelo.beans;

import java.io.Serializable;
import java.sql.Blob;

public class Cabdocumentos implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private String transaccion;
	private String periodo;
	private String tipodocumento;
	private String serie;
	private String numero;
	private String fechaemision;
	private String fechavencimiento;
	private String tipocliente;
	private String numcliente;
	private String nomcliente;
	private String direccion;
	private String departamento;
	private String provincia;
	private String distrito;
	private String email;
	private Double valventaafe;
	private Double valventaina;
	private Double valventaexo;
	private Double isc;
	private String codisc;
	private Double igv;
	private String codigv;
	private Double otros;
	private Double totaldoc;
	private String tipodocmod;
	private String serieno;
	private String numerono;
	private String serieelec;
	private String numeroelec;
	private Integer homologado;
	private String fechahomologado;
	private Blob archivo;
	private String nombre_archivo;
	private String mensaje_homologado;
	private Blob archivo_homologado;
	private String nom_archivo_homologado;
	private Integer resumen;
	private Integer anulado;
	public String getTransaccion() {
		return transaccion;
	}
	public void setTransaccion(String transaccion) {
		this.transaccion = transaccion;
	}
	public String getPeriodo() {
		return periodo;
	}
	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}
	public String getTipodocumento() {
		return tipodocumento;
	}
	public void setTipodocumento(String tipodocumento) {
		this.tipodocumento = tipodocumento;
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
	public String getFechaemision() {
		return fechaemision;
	}
	public void setFechaemision(String fechaemision) {
		this.fechaemision = fechaemision;
	}
	public String getFechavencimiento() {
		return fechavencimiento;
	}
	public void setFechavencimiento(String fechavencimiento) {
		this.fechavencimiento = fechavencimiento;
	}
	public String getTipocliente() {
		return tipocliente;
	}
	public void setTipocliente(String tipocliente) {
		this.tipocliente = tipocliente;
	}
	public String getNumcliente() {
		return numcliente;
	}
	public void setNumcliente(String numcliente) {
		this.numcliente = numcliente;
	}
	public String getNomcliente() {
		return nomcliente;
	}
	public void setNomcliente(String nomcliente) {
		this.nomcliente = nomcliente;
	}
	public String getDireccion() {
		return direccion;
	}
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}
	public String getDepartamento() {
		return departamento;
	}
	public void setDepartamento(String departamento) {
		this.departamento = departamento;
	}
	public String getProvincia() {
		return provincia;
	}
	public void setProvincia(String provincia) {
		this.provincia = provincia;
	}
	public String getDistrito() {
		return distrito;
	}
	public void setDistrito(String distrito) {
		this.distrito = distrito;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Double getValventaafe() {
		return valventaafe;
	}
	public void setValventaafe(Double valventaafe) {
		this.valventaafe = valventaafe;
	}
	public Double getValventaina() {
		return valventaina;
	}
	public void setValventaina(Double valventaina) {
		this.valventaina = valventaina;
	}
	public Double getValventaexo() {
		return valventaexo;
	}
	public void setValventaexo(Double valventaexo) {
		this.valventaexo = valventaexo;
	}
	public Double getIsc() {
		return isc;
	}
	public void setIsc(Double isc) {
		this.isc = isc;
	}
	public String getCodisc() {
		return codisc;
	}
	public void setCodisc(String codisc) {
		this.codisc = codisc;
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
	public Double getOtros() {
		return otros;
	}
	public void setOtros(Double otros) {
		this.otros = otros;
	}
	public Double getTotaldoc() {
		return totaldoc;
	}
	public void setTotaldoc(Double totaldoc) {
		this.totaldoc = totaldoc;
	}
	public String getTipodocmod() {
		return tipodocmod;
	}
	public void setTipodocmod(String tipodocmod) {
		this.tipodocmod = tipodocmod;
	}
	public String getSerieno() {
		return serieno;
	}
	public void setSerieno(String serieno) {
		this.serieno = serieno;
	}
	public String getNumerono() {
		return numerono;
	}
	public void setNumerono(String numerono) {
		this.numerono = numerono;
	}
	public String getSerieelec() {
		return serieelec;
	}
	public void setSerieelec(String serieelec) {
		this.serieelec = serieelec;
	}
	public String getNumeroelec() {
		return numeroelec;
	}
	public void setNumeroelec(String numeroelec) {
		this.numeroelec = numeroelec;
	}
	public Integer getHomologado() {
		return homologado;
	}
	public void setHomologado(Integer homologado) {
		this.homologado = homologado;
	}
	public String getFechahomologado() {
		return fechahomologado;
	}
	public void setFechahomologado(String fechahomologado) {
		this.fechahomologado = fechahomologado;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public Blob getArchivo() {
		return archivo;
	}
	public void setArchivo(Blob archivo) {
		this.archivo = archivo;
	}
	public String getNombre_archivo() {
		return nombre_archivo;
	}
	public void setNom_archivo(String nombre_archivo) {
		this.nombre_archivo = nombre_archivo;
	}
	public String getMensaje_homologado() {
		return mensaje_homologado;
	}
	public void setMensaje_homologado(String mensaje_homologado) {
		this.mensaje_homologado = mensaje_homologado;
	}
	public Blob getArchivo_homologado() {
		return archivo_homologado;
	}
	public void setArchivo_homologado(Blob archivo_homologado) {
		this.archivo_homologado = archivo_homologado;
	}
	public String getNom_archivo_homologado() {
		return nom_archivo_homologado;
	}
	public void setNom_archivo_homologado(String nom_archivo_homologado) {
		this.nom_archivo_homologado = nom_archivo_homologado;
	}
	public Integer getResumen() {
		return resumen;
	}
	public void setResumen(Integer resumen) {
		this.resumen = resumen;
	}
	public Integer getAnulado() {
		return anulado;
	}
	public void setAnulado(Integer anulado) {
		this.anulado = anulado;
	}

}
