package com.pe.amd.modelo.beans;

/**
 * Representa las variables absolutas del Sistema
 * puede crecer con forme el programa incremente
 * @author Diego
 *
 */
public class Sistema {
	private Sistema() {}
	private static Sistema s = null;
	
	public static Sistema getSistema() {
		if(Sistema.s == null)
			Sistema.s = new Sistema();
		
		return Sistema.s;
	}
	
	private String urlServidor;
	private String urlProduccion;
	private String urlRemision;
	private String urlRetencion;
	private String urlConsultaCDR;
	private String urlValidarCE;
	private Integer validarBoletas;
	
	public String getUrlServidor() {
		return urlServidor;
	}
	public void setUrlServidor(String urlServidor) {
		this.urlServidor = urlServidor;
	}
	public String getUrlProduccion() {
		return urlProduccion;
	}
	public void setUrlProduccion(String urlProduccion) {
		this.urlProduccion = urlProduccion;
	}
	public String getUrlRemision() {
		return urlRemision;
	}
	public void setUrlRemision(String urlRemision) {
		this.urlRemision = urlRemision;
	}
	public String getUrlRetencion() {
		return urlRetencion;
	}
	public void setUrlRetencion(String urlRetencion) {
		this.urlRetencion = urlRetencion;
	}
	public String getUrlConsultaCDR() {
		return urlConsultaCDR;
	}
	public void setUrlConsultaCDR(String urlConsultaCDR) {
		this.urlConsultaCDR = urlConsultaCDR;
	}
	public String getUrlValidarCE() {
		return urlValidarCE;
	}
	public void setUrlValidarCE(String urlValidarCE) {
		this.urlValidarCE = urlValidarCE;
	}
	public Integer getValidarBoletas() {
		return validarBoletas;
	}
	public void setValidarBoletas(Integer validarBoletas) {
		this.validarBoletas = validarBoletas;
	}
	
}
