package com.pe.amd.modelo.beans;

import java.io.Serializable;

public class Usuario implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String pass;
	private String dni;
	private String correo;
	private String nombres;
	private String apellidos;
	private Integer rango;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getDni() {
		return dni;
	}
	public void setDni(String dni) {
		this.dni = dni;
	}
	public String getCorreo() {
		return correo;
	}
	public void setCorreo(String correo) {
		this.correo = correo;
	}
	public String getNombres() {
		return nombres;
	}
	public void setNombres(String nombres) {
		this.nombres = nombres;
	}
	public String getApellidos() {
		return apellidos;
	}
	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}
	public Integer getRango() {
		return rango;
	}
	public void setRango(Integer rango) {
		this.rango = rango;
	}
	@Override
	public String toString() {
		return "Usuario [id=" + id + ", pass=" + pass + ", dni=" + dni + ", correo=" + correo + ", nombres=" + nombres
				+ ", apellidos=" + apellidos + ", rango=" + rango + "]";
	}
	
	
}
