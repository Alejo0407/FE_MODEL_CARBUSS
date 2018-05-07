package com.pe.amd.modelo.app.xml;

import java.util.List;

import com.pe.amd.modelo.beans.BeanManager;
import com.pe.amd.modelo.beans.Cabdocumentos;
import com.pe.amd.modelo.beans.Detdocumentos;
import com.pe.amd.modelo.beans.Empresa;

public class XMLFactory {
	
	/**
	 * Retorna una factura o boleta dependiendo del codigo ingresado en el campo tipo 
	 * el cual compara con los codigos de BeanManager
	 * @param cabecera
	 * @param detalle
	 * @param empresa
	 * @param filename
	 * @param serie
	 * @param correlativo
	 * @param tipo
	 * @return
	 */
	public static XMLDocument getXMLFacturaBoleta(Cabdocumentos cabecera, List<Detdocumentos> detalle,Empresa empresa,
			String filename,String serie, String correlativo, int tipo) {
		XMLDocument doc = null;
		if(tipo == BeanManager.COD_FACTURA)
			doc = new XMLFactura(cabecera,detalle,empresa,filename,serie,correlativo);
		else if(tipo == BeanManager.COD_BOLETA)
			doc = new XMLBoleta(cabecera,detalle,empresa,filename,serie,correlativo);
		return doc;
	}
	
	/**
	 * Retorna un documento de resumenes de Bajas para generar
	 * @param documentos -> documentos a anular
	 * @param empresa -> datos de la empresa
	 * @param razones -> razones de anulacion
	 * @param filename ->  nombre del archivo
	 * @param correlacion -> correlativo del archivo
	 * @return
	 */
	public static XMLDocument getXMLResumenBajas(List<Cabdocumentos> documentos, Empresa empresa,List<String> razones,
			String filename,String correlacion) {
		return new XMLResumenBaja(documentos,empresa,razones,filename,correlacion);
	}
	
	/**
	 * Retorna un objeto del tipo Resumen Diario
	 * @param boletas  -> boletas para el resumen
	 * @param empresa  -> datos de la empresa
	 * @param filename -> nombre del archivo
	 * @param correlacion -> correlacion del archivp
	 * @param tipo -> Si es boleta, nota de credito o nota de debito
	 * @return
	 */
	public static XMLDocument getXMLResumenDiario(List<Cabdocumentos> boletas, Empresa empresa,
			String filename,String correlacion, int tipo) {
		return new XMLResumenDiario(boletas,empresa,filename,correlacion,tipo);
	}

}
