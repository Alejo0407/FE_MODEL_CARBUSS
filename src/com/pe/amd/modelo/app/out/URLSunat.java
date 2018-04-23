package com.pe.amd.modelo.app.out;

public enum URLSunat {
	
	BetaFactura("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService"),
	BetaGuia("https://e-beta.sunat.gob.pe/ol-ti-itemision-guia-gem-beta/billService"),
	BetaRetenciones("https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService"),
	
	ProduccionFactura("https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService"),
	//ProduccionFactura("https://www.sunat.gob.pe/ol-ti-itcpfegem/billService"),
	
	ProduccionRemision("https://e-guiaremision.sunat.gob.pe/ol-ti-itemision-guia-gem/billService"),
	ProduccionRetencionPercepcion("https://www.sunat.gob.pe/ol-ti-itemision-otroscpe-gem/billService"),
	
	ConsultaValidezDeFE("https://www.sunat.gob.pe/ol-it-wsconsvalidcpe/billValidService"),
	ConsultaDeCDR("https://www.sunat.gob.pe/ol-it-wsconscpegem/billConsultService");
	
	private String valor = null;
	private URLSunat(String valor) {
		this.valor = valor;
	}
	public String getValor() {return this.valor;}
	
}
