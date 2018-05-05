package com.pe.amd.modelo.beans;

import java.io.Serializable;
import java.sql.Blob;

public class Empresa implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -772430955563246059L;
	
	private  Empresa() {}
	private static Empresa empresa = null;
	
	public static Empresa getEmpresa() {
		if(Empresa.empresa == null)
			empresa = new Empresa();
		return Empresa.empresa;
	}
	
	
	private String ruc;
	private String nombre;
	private String direccion;
	private String nombre_comercial;
	private String ubigeo;
	private String urbanizacion;
	private String provincia;
	private String departamento;
	private String distrito;
	private String telefono;
	private String fax;
	private String mail_empresa;
	private String web;
	
	//Firma Digital
	private Blob ce;
	private String nce;
	private String pin;
	private String pin_revocar;
	private String alias;
	
	//USUARIO SECUNDARIO
	private String usr_secundario;
	private String pass;

	public String getRuc() {
		return ruc;
	}

	public void setRuc(String ruc) {
		this.ruc = ruc;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getNombre_comercial() {
		return nombre_comercial;
	}

	public void setNombre_comercial(String nombre_comercial) {
		this.nombre_comercial = nombre_comercial;
	}

	public String getUbigeo() {
		return ubigeo;
	}

	public void setUbigeo(String ubigeo) {
		this.ubigeo = ubigeo;
	}

	public String getUrbanizacion() {
		return urbanizacion;
	}

	public void setUrbanizacion(String urbanizacion) {
		this.urbanizacion = urbanizacion;
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

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getMail_empresa() {
		return mail_empresa;
	}

	public void setMail_empresa(String mail_empresa) {
		this.mail_empresa = mail_empresa;
	}

	public String getWeb() {
		return web;
	}

	public void setWeb(String web) {
		this.web = web;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getDepartamento() {
		return departamento;
	}

	public void setDepartamento(String departamento) {
		this.departamento = departamento;
	}

	

	public Blob getCe() {
		return ce;
	}

	public void setCe(Blob ce) {
		this.ce = ce;
	}

	public String getNce() {
		return nce;
	}

	public void setNce(String nce) {
		this.nce = nce;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getPin_revocar() {
		return pin_revocar;
	}

	public void setPin_revocar(String pin_revocar) {
		this.pin_revocar = pin_revocar;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public String toString() {
		return "Empresa [ruc=" + ruc + ", nombre=" + nombre + ", direccion=" + direccion + ", nombre_comercial="
				+ nombre_comercial + ", ubigeo=" + ubigeo + ", urbanizacion=" + urbanizacion + ", provincia="
				+ provincia + ", departamento=" + departamento + ", distrito=" + distrito + ", telefono=" + telefono
				+ ", fax=" + fax + ", mail_empresa=" + mail_empresa + ", web=" + web + ", ce=" + ce + ", nce=" + nce
				+ ", pin=" + pin + ", pin_revocar=" + pin_revocar + ", alias=" + alias + "]";
	}

	public String getUsrSecundario() {
		return usr_secundario;
	}

	public void setUsrSecundario(String usr_secundario) {
		this.usr_secundario = usr_secundario;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}
	
	
	
}
