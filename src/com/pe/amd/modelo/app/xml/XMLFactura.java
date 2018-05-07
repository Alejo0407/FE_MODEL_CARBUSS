package com.pe.amd.modelo.app.xml;

import java.io.File;/*
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;*/
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.pe.amd.modelo.app.out.Escritor;
import com.pe.amd.modelo.beans.Cabdocumentos;
import com.pe.amd.modelo.beans.Detdocumentos;
import com.pe.amd.modelo.beans.Empresa;

class XMLFactura implements XMLDocument{
	
	private Cabdocumentos cabecera;
	private List<Detdocumentos> detalle;
	private Empresa empresa;
	private String filename,serie,correlativo;
	
	
	
	public XMLFactura(Cabdocumentos cabecera, List<Detdocumentos> detalle,Empresa empresa,
			String filename,String serie, String correlativo) {
		this.cabecera = cabecera;
		this.detalle = detalle;
		this.setEmpresa(empresa);
		this.filename = filename;
		this.serie = serie;
		this.correlativo = correlativo;
	}
	
	public  File generarDocumento() throws ParserConfigurationException, TransformerException {
	
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder	=	docFactory.newDocumentBuilder();
		
		//Creacion del Documento
		
		Document doc = docBuilder.newDocument();
		Element root = doc.createElement("Invoice");

		//Namespaces
		root.setAttribute("xmlns", "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2");
		root.setAttribute("xmlns:cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
		root.setAttribute("xmlns:cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
		root.setAttribute("xmlns:ccts", "urn:un:unece:uncefact:documentation:2");
		root.setAttribute("xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
		root.setAttribute("xmlns:ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
		root.setAttribute("xmlns:qdt", "urn:oasis:names:specification:ubl:schema:xsd:QualifiedDatatypes-2");
		root.setAttribute("xmlns:sac", "urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1");
		root.setAttribute("xmlns:udt", "urn:un:unece:uncefact:data:specification:UnqualifiedDataTypesSchemaModule:2");
		root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		
		doc.appendChild(root);
		
		//COMPONENTE DE EXTENSION
		Element ublextns = doc.createElement("ext:UBLExtensions");
		
		//TOTALES GRAVADA, INAFECTAS, EXONERADAS, GRATUITAS
		Element ublext = doc.createElement("ext:UBLExtension");
		Element ublext1 = doc.createElement("ext:ExtensionContent");
		Element ublext2 = doc.createElement("sac:AdditionalInformation");
				
		
		if(cabecera.getValventaafe()!= null)
				ublext2.appendChild(this.cabeceraTotales(doc,
						cabecera.getValventaafe(),"PEN","1001"));
		if(cabecera.getValventaina() != null)
			ublext2.appendChild(this.cabeceraTotales(doc,
					cabecera.getValventaina(),"PEN","1002"));
		if(cabecera.getValventaexo()  != null)
			ublext2.appendChild(this.cabeceraTotales(doc,
					cabecera.getValventaexo() ,"PEN","1003"));/*
		if(factura.getTOTVENT() != null)
			ublext2.appendChild(XMLFactura.cabeceraTotales(doc,
					factura.getTOTVENT(),factura.getMONEDA(),"1004"));
			
		if(factura.getTOTDESC() != null)
			ublext2.appendChild(XMLFactura.cabeceraTotales(doc,
					factura.getTOTDESC(), factura.getMONEDA(), "2005"));*/
		
		ublext1.appendChild(ublext2);
		ublext.appendChild(ublext1);
			
		ublextns.appendChild(ublext);
		root.appendChild(ublextns);
		
		//Version UBL
		root.appendChild(doc.createElement("cbc:UBLVersionID"));
		root.getLastChild().appendChild(doc.createTextNode("2.0"));
		//Versión de la estructura del documento
		root.appendChild(doc.createElement("cbc:CustomizationID"));
		root.getLastChild().appendChild(doc.createTextNode("1.0"));
		//NUMERACION
		root.appendChild(doc.createElement("cbc:ID"));
		root.getLastChild().appendChild(doc.createTextNode("F"+this.serie+"-"+this.correlativo));
		//FECHA DE EMISION
		Element f_emision = doc.createElement("cbc:IssueDate");
				f_emision.appendChild(doc.createTextNode(cabecera.getFechaemision().substring(0, 4) + "-" 
				+ cabecera.getFechaemision().substring(4, 6) + "-" + cabecera.getFechaemision().substring(6)));
		root.appendChild(f_emision);	

		//TIPO DOCUMENTO
		root.appendChild(doc.createElement("cbc:InvoiceTypeCode"));
		root.getLastChild().appendChild(doc.createTextNode(cabecera.getTipodocumento()));
		
		//TIPO DE MONEDA
		root.appendChild(doc.createElement("cbc:DocumentCurrencyCode"));
		root.getLastChild().appendChild(doc.createTextNode("PEN"));
				
		//REFERENCIA A LA FIRMA
		root.appendChild(this.referenciaFirma(doc));
	
		//INFO EMISOR
		root.appendChild(this.informacionEmisor(doc));	
		
		//INFO DEL RECEPTOR
		root.appendChild(this.informacionReceptor(doc));
		
		//SUMATORIA IGV
		root.appendChild(this.cabeceraImpuestos(doc,"PEN",cabecera.getIgv(),"1000","IGV","VAT"));
				
		//SUMATORIA ISC
		if(cabecera.getIsc() != null)
			if(cabecera.getIgv().doubleValue() != 0.00)
				root.appendChild(this.cabeceraImpuestos(doc,"PEN",cabecera.getIsc(),"2000","ISC","EXC"));
				
		//SUMATORIA OTROS
		if(cabecera.getOtros() != null)
			if(cabecera.getOtros().doubleValue() != 0.00)
				root.appendChild(this.cabeceraImpuestos(doc,"PEN",cabecera.getOtros(),"9999","OTROS","OTH"));
		
		//SUMATORIA OTROS CARGOS
		/*SIN IMPLEMENTAR EN EL ESQUEMA*/
		
		//IMPORTE TOTAL
		Element legal= doc.createElement("cac:LegalMonetaryTotal");
		root.appendChild(legal);
		Element importe = doc.createElement("cbc:PayableAmount");
		importe.setAttribute("currencyID", "PEN");
		importe.appendChild(doc.createTextNode(String.format("%.2f",cabecera.getTotaldoc()).replace(',', '.')));
		root.getLastChild().appendChild(importe);
		
		for(Detdocumentos det:detalle) {
			root.appendChild(this.detalleLinea(doc, det));
		}
		
		
		//Este es el contenedero de la firma
		Element ublext_firma = doc.createElement("ext:UBLExtension");
		Element ublext_cont = doc.createElement("ext:ExtensionContent");
		ublext_firma.appendChild(ublext_cont);
		ublextns.appendChild(ublext_firma);
		
		new Escritor().escribirXML(this.filename, "", doc);
		
		try {
			doc = Firmador.sign(new File(this.filename), empresa.getCe().getBinaryStream(), empresa.getPin(), empresa.getAlias());
		}catch(Exception e) {
			throw new NullPointerException("Error en la Firma DIGITAL: "
					+ e.getMessage());
		}
		
		return new Escritor().escribirXML(this.filename, "", doc);
	}
	
	private Node referenciaFirma(Document doc) {
		Element signature = doc.createElement("cac:Signature");
		signature.appendChild(doc.createElement("cbc:ID"));
		signature.getLastChild().appendChild(doc.createTextNode(empresa.getRuc().trim()));//REFERENCIA AL ID de la firma
		
		Element signatory_party = doc.createElement("cac:SignatoryParty");
			Element sp1 = doc.createElement("cac:PartyIdentification");
			sp1.appendChild(doc.createElement("cbc:ID"));
			sp1.getLastChild().appendChild(doc.createTextNode(empresa.getRuc().trim()));//RUC DEL EMISOR
			
			Element sp2 = doc.createElement("cac:PartyName");
			sp2.appendChild(doc.createElement("cbc:Name"));
			sp2.getLastChild().appendChild(doc.createTextNode(empresa.getNombre().trim()));//NOMBRE DEL EMISOR
		
			signatory_party.appendChild(sp1);
			signatory_party.appendChild(sp2);
			
		signature.appendChild(signatory_party);
		
		Element digital_signature_attach = doc.createElement("cac:DigitalSignatureAttachment");
			Element dsa1 = doc.createElement("cac:ExternalReference");
			dsa1.appendChild(doc.createElement("cbc:URI"));
			dsa1.getLastChild().appendChild(doc.createTextNode("#"+empresa.getRuc().trim()));//REFERENCIA A URI
			digital_signature_attach.appendChild(dsa1);
			
		signature.appendChild(digital_signature_attach);
		
		// TODO Auto-generated method stub
		return signature;
	}

	private Node detalleLinea(Document doc, Detdocumentos det) {
		Element linea = doc.createElement("cac:InvoiceLine");
		
		linea.appendChild(doc.createElement("cbc:ID"));
		linea.getLastChild().appendChild(doc.createTextNode(det.getSec()));
		
		// Unidad de medida por ítem y Cantidad de unidades por ítem
		Element unidades = doc.createElement("cbc:InvoicedQuantity");
		unidades.setAttribute("unitCode", "NIU");//UNIDAD DE MEDIDA -> BUSCAR
		unidades.appendChild(doc.createTextNode(String.format("%.2f", det.getCantidad()).replace(',', '.')));//CANTIDAD
		linea.appendChild(unidades);
		
		//VALOR DE VENTA POR ITEM
		Element vvi = doc.createElement("cbc:LineExtensionAmount");
		vvi.setAttribute("currencyID","PEN");
		vvi.appendChild(doc.createTextNode(String.format("%.2f",( det.getValunitario()*det.getCantidad() )).replace(',', '.')));
		linea.appendChild(vvi);
		
		// Precio de venta unitario por item  y código 
		Element pricing_reference = doc.createElement("cac:PricingReference");
		Element pr1 = doc.createElement("cac:AlternativeConditionPrice");
		Element val = doc.createElement("cbc:PriceAmount");
		val.setAttribute("currencyID", "PEN");
		val.appendChild(doc.createTextNode(String.format("%.2f",det.getValtotal()).replace(',', '.')));//ES IGUAL NO HAY IGV
		pr1.appendChild(val);
		pr1.appendChild(doc.createElement("cbc:PriceTypeCode"));
		pr1.getLastChild().appendChild(doc.createTextNode("01"));//QUI SE CAMBIA A 2 si es no ONEROSA
		pricing_reference.appendChild(pr1);
				
		/*if(detalle.getNONVALUNI() != null)
			if(Double.valueOf(detalle.getNONVALUNI()) > 0 ) {
				//Implementar para no onerosas
				
			}
		*/
		linea.appendChild(pricing_reference);
				
		//AFECTACION IGV POR ITEM
		Element igv_tax = doc.createElement("cac:TaxTotal");
		Element igv_tot1 = doc.createElement("cbc:TaxAmount");
		igv_tot1.setAttribute("currencyID", "PEN");//moneda
		igv_tot1.appendChild(doc.createTextNode(String.format("%.2f", det.getIgv()).replace(',', '.')));//total1
		igv_tax.appendChild(igv_tot1);
		Element igv_sub = doc.createElement("cac:TaxSubtotal");
			Element igv_tot2 = doc.createElement("cbc:TaxAmount");
			igv_tot2.setAttribute("currencyID", "PEN");//moneda
			igv_tot2.appendChild(doc.createTextNode(String.format("%.2f", det.getIgv()).replace(',', '.')));//total2
			igv_sub.appendChild(igv_tot2);
			
			Element afec = doc.createElement("cbc:TaxExemptionReasonCode");
			afec.appendChild(doc.createTextNode(det.getCodigv()));//AFECTACION
			Element igv_cod = doc.createElement("cbc:ID");
			igv_cod.appendChild(doc.createTextNode("1000"));//CODIGO 1000
			Element igv_nom = doc.createElement("cbc:Name");
			igv_nom.appendChild(doc.createTextNode("IGV"));//LOCAL
			Element igv_enom = doc.createElement("cbc:TaxTypeCode");
			igv_enom.appendChild(doc.createTextNode("VAT"));//INTER
			
			igv_sub.appendChild(doc.createElement("cac:TaxCategory"));
			igv_sub.getLastChild().appendChild(afec);
			igv_sub.getLastChild().appendChild(doc.createElement("cac:TaxScheme"));
			igv_sub.getLastChild().getLastChild().appendChild(igv_cod);
			igv_sub.getLastChild().getLastChild().appendChild(igv_nom);
			igv_sub.getLastChild().getLastChild().appendChild(igv_enom);
				
		igv_tax.appendChild(igv_sub);		
		linea.appendChild(igv_tax);
		
		//AFECTACION ISC POR ITEM
		/*SIN IMPLEMENTAR EN EL ESQUEMA*/
		
		//DESCRIPCION DETALLADA
		Element item = doc.createElement("cac:Item");
		item.appendChild(doc.createElement("cbc:Description"));
		item.getLastChild().appendChild(doc.createTextNode(det.getDenominacion()));
		//CODIGO DEL PRODUCTO
		if(det.getCodigo() != null) {
			Element e = doc.createElement("cac:SellersItemIdentification");
			e.appendChild(doc.createElement("cbc:ID"));
			e.getLastChild().appendChild(doc.createTextNode(det.getCodigo()));
		}
		linea.appendChild(item);
				
		//VALOR UNITARIO POR ITEM
		Element vui = doc.createElement("cac:Price");
		Element vui1 = doc.createElement("cbc:PriceAmount");
		vui1.setAttribute("currencyID", "PEN");
		vui1.appendChild(doc.createTextNode(String.format("%.2f", det.getValunitario()).replace(',', '.')));
		vui.appendChild(vui1);
		linea.appendChild(vui);
		
		//DESCUENTOS POR ITEM
		/*SIN IMPLEMENTAR EN EL ESQUEMA*/
		return linea;
	}

	private Node cabeceraImpuestos(Document doc, String moneda, Double tot, String cod, String nom,
			String ecod) {
		// TODO Auto-generated method stub
		Element e = doc.createElement("cac:TaxTotal");
		Element e1 = doc.createElement("cbc:TaxAmount");
		e1.setAttribute("currencyID", moneda);
		e1.appendChild(doc.createTextNode(String.format("%.2f", tot).replace(',', '.')));
		Element e2 = doc.createElement("cac:TaxSubtotal");
			Element f1 = doc.createElement("cbc:TaxAmount");
			f1.setAttribute("currencyID", moneda);
			f1.appendChild(doc.createTextNode(String.format("%.2f", tot).replace(',', '.')));
			Element f2 = doc.createElement("cac:TaxCategory");
			Element f3 = doc.createElement("cac:TaxScheme");
			f3.appendChild(doc.createElement("cbc:ID"));
			f3.getLastChild().appendChild(doc.createTextNode(cod));
			f3.appendChild(doc.createElement("cbc:Name"));
			f3.getLastChild().appendChild(doc.createTextNode(nom));
			f3.appendChild(doc.createElement("cbc:TaxTypeCode"));
			f3.getLastChild().appendChild(doc.createTextNode(ecod));
			f2.appendChild(f3);
		e2.appendChild(f1);
		e2.appendChild(f2);
		
		e.appendChild(e1);
		e.appendChild(e2);
		return e;
	}

	private Node informacionReceptor(Document doc) {
		Element receptor = doc.createElement("cac:AccountingCustomerParty");
		
		Element documento = doc.createElement("cbc:CustomerAssignedAccountID");
		documento.appendChild(doc.createTextNode(cabecera.getNumcliente().trim()));
		Element tipo_documento = doc.createElement("cbc:AdditionalAccountID");
		tipo_documento.appendChild(doc.createTextNode(cabecera.getTipocliente().trim()));
				
		Element party = doc.createElement("cac:Party");
		Element party_legal = doc.createElement("cac:PartyLegalEntity");
		party_legal.appendChild(doc.createElement("cbc:RegistrationName"));
		party_legal.getLastChild().appendChild(doc.createTextNode(cabecera.getNomcliente().trim()));
		party.appendChild(party_legal);
				
		receptor.appendChild(documento);
		receptor.appendChild(tipo_documento);
		receptor.appendChild(party);
				
		return receptor;
	}

	private Node informacionEmisor(Document doc) {
		Element emisor = doc.createElement("cac:AccountingSupplierParty");
		
		//NUMERO DE Y TIPO RUC
		Element nro = doc.createElement("cbc:CustomerAssignedAccountID");
		nro.appendChild(doc.createTextNode(empresa.getRuc().trim()));
		Element tipo = doc.createElement("cbc:AdditionalAccountID");
		tipo.appendChild(doc.createTextNode("6"));
		
		Element party = doc.createElement("cac:Party");
			//Nombre Comercial
			if(empresa.getNombre_comercial() != null)
				if(!empresa.getNombre_comercial().isEmpty()) {
					Element p = doc.createElement("cac:PartyName");
					p.appendChild(doc.createElement("cbc:Name"));
					p.getLastChild().appendChild(doc.createTextNode(empresa.getNombre_comercial().trim()));
					party.appendChild(p);
				}
			
			//DIRECCION
			if(empresa.getDireccion() != null && empresa.getDireccion() != null
					&& empresa.getUrbanizacion() != null && empresa.getUbigeo() != null
					&& empresa.getProvincia() != null && empresa.getDepartamento() != null
					&& empresa.getDistrito() != null) {
				Element postal_adress = doc.createElement("cac:PostalAddress");
				postal_adress.appendChild(doc.createElement("cbc:ID"));//CODIGO UBIGEO
				postal_adress.getLastChild().appendChild(doc.createTextNode(empresa.getUbigeo().trim()));
				postal_adress.appendChild(doc.createElement("cbc:StreetName"));//DIRECCION
				postal_adress.getLastChild().appendChild(doc.createTextNode(empresa.getDireccion().trim()));
				postal_adress.appendChild(doc.createElement("cbc:CitySubdivisionName"));//URBANIZACION
				postal_adress.getLastChild().appendChild(doc.createTextNode(empresa.getUrbanizacion().trim()));
				postal_adress.appendChild(doc.createElement("cbc:CityName"));//PROVINCIA
				postal_adress.getLastChild().appendChild(doc.createTextNode(empresa.getProvincia().trim()));
				postal_adress.appendChild(doc.createElement("cbc:CountrySubentity"));//DEPARTAMENTO
				postal_adress.getLastChild().appendChild(doc.createTextNode(empresa.getDepartamento().trim()));
				postal_adress.appendChild(doc.createElement("cbc:District"));//DISTRITO
				postal_adress.getLastChild().appendChild(doc.createTextNode(empresa.getDistrito().trim()));
				postal_adress.appendChild(doc.createElement("cac:Country"));//CODIGO PAIS
				Element pais = doc.createElement("cbc:IdentificationCode");
				pais.appendChild(doc.createTextNode("PE"));
				postal_adress.getLastChild().appendChild(pais);
				
				party.appendChild(postal_adress);
			}
			//Apellidos y nombres, denominación o razón social 
			Element den = doc.createElement("cac:PartyLegalEntity");
			den.appendChild(doc.createElement("cbc:RegistrationName"));
			den.getLastChild().appendChild(doc.createTextNode(empresa.getNombre().trim()));
			party.appendChild(den);
			
		emisor.appendChild(nro);
		emisor.appendChild(tipo);
		emisor.appendChild(party);
		
		return emisor;
	}

	private Node cabeceraTotales(Document doc, Double valventaexo, String moneda, String codigo) {
			
		Element e = doc.createElement("sac:AdditionalMonetaryTotal");
		e.appendChild(doc.createElement("cbc:ID"));
		e.getLastChild().appendChild(doc.createTextNode(codigo));
		Element tot = doc.createElement("cbc:PayableAmount");
		tot.setAttribute("currencyID", moneda);
		tot.appendChild(doc.createTextNode( String.format("%.2f", valventaexo.doubleValue()).replace(',', '.')));
		e.appendChild(tot);
			
		return e;
	}

	public Cabdocumentos getCabecera() {
		return cabecera;
	}
	public void setCabecera(Cabdocumentos cabecera) {
		this.cabecera = cabecera;
	}
	public List<Detdocumentos> getDetalle() {
		return detalle;
	}
	public void setDetalle(List<Detdocumentos> detalle) {
		this.detalle = detalle;
	}
	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}
}
