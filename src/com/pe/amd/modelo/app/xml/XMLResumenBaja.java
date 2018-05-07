package com.pe.amd.modelo.app.xml;

import java.io.File;
import java.util.GregorianCalendar;
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
import com.pe.amd.modelo.beans.Empresa;

class XMLResumenBaja implements XMLDocument{
	private List<Cabdocumentos> documentos;
	private Empresa empresa;
	private List<String> razones;
	private String filename,correlacion;
	
	public XMLResumenBaja(List<Cabdocumentos> documentos, Empresa empresa,List<String> razones,
			String filename,String correlacion) {
		super();
		this.setDocumentos(documentos);
		this.setEmpresa(empresa);
		this.setRazones(razones);
		this.filename = filename;
		this.correlacion = correlacion;
		
		
		if(getDocumentos().size() == 0)
			throw new IllegalArgumentException("Para generar el resumen se debe te tener al menos "
					+ "un documento");
		if(getDocumentos().size() != getRazones().size())
			throw new IllegalArgumentException("Para generar el resumen se debe tener la misma cantidad"
					+ " de razones y de documentos");
	}
	
	public File generarDocumento() throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		//Creacion del Documento
		Document doc = docBuilder.newDocument();
		Element root = doc.createElement("VoidedDocuments");
		
		root.setAttribute("xmlns", "urn:sunat:names:specification:ubl:peru:schema:xsd:VoidedDocuments-1");
		root.setAttribute("xmlns:cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
		root.setAttribute("xmlns:cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
		root.setAttribute("xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
		root.setAttribute("xmlns:ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
		root.setAttribute("xmlns:sac", "urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1");
		root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		doc.appendChild(root);
		
		//COMPONENTE DE EXTENSION
		Element ublextns = doc.createElement("ext:UBLExtensions");
		root.appendChild(ublextns);
				
		GregorianCalendar date = new GregorianCalendar();
		Element fec_resumen = doc.createElement("cbc:IssueDate");
		String anio = String.format("%04d", date.get(GregorianCalendar.YEAR)),
				mes = String.format("%02d", date.get(GregorianCalendar.MONTH)+1),
				dia = String.format("%02d", date.get(GregorianCalendar.DATE));
		//Version UBL
		root.appendChild(doc.createElement("cbc:UBLVersionID"));
		root.getLastChild().appendChild(doc.createTextNode("2.0"));
		//Versión de la estructura del documento
		root.appendChild(doc.createElement("cbc:CustomizationID"));
		root.getLastChild().appendChild(doc.createTextNode("1.0"));
		
		//IDENTIFICADOR DEL RESUMEN
		Element id = doc.createElement("cbc:ID");
		id.appendChild(doc.createTextNode("RA-"+anio+mes+dia+"-"+this.correlacion));
		root.appendChild(id);
		
		//FECHA EMISION DOCUEMTNOS
		Element f_emision = doc.createElement("cbc:ReferenceDate");
			f_emision.appendChild(doc.createTextNode(documentos.get(0).getFechaemision().substring(0, 4) + "-" 
			+ documentos.get(0).getFechaemision().substring(4, 6) + "-" + documentos.get(0).getFechaemision().substring(6)));
		root.appendChild(f_emision);
				
		//FECHA GENERACION DE RESUMEN
		fec_resumen.appendChild(doc.createTextNode(anio+"-"+mes+"-"+dia));
		root.appendChild(fec_resumen);
		//REFERENCIA A LA FIRMA
		root.appendChild(this.referenciaFirma(doc));
				
		//INFO DEL EMISOR
		root.appendChild(this.informacionEmisor(doc));
		
		for(int i = 0 ; i < getDocumentos().size() ; i++) {
			root.appendChild(this.generarLinea(doc,getDocumentos().get(i),i+1
					,this.getRazones().get(i)));
		}
		//Este es el contenedero de la firma
		Element ublext_firma = doc.createElement("ext:UBLExtension");
		Element ublext_cont = doc.createElement("ext:ExtensionContent");
		ublext_firma.appendChild(ublext_cont);
		ublextns.appendChild(ublext_firma);
				
		new Escritor().escribirXML(this.filename, "", doc);
			
		try {
			doc = Firmador.sign(new File(this.filename), empresa.getCe().getBinaryStream()
					, empresa.getPin(), empresa.getAlias());
		}catch(Exception e) {
			throw new NullPointerException("Error en la Firma DIGITAL: "
						+ e.getMessage());
		}
		return new Escritor().escribirXML(this.filename, "", doc);
	}
	private Node generarLinea(Document doc, Cabdocumentos documento, int i,String razon) {
		Element line = doc.createElement("sac:VoidedDocumentsLine");
		Element line_id = doc.createElement("cbc:LineID");
		line_id.appendChild(doc.createTextNode(String.valueOf(i)));
		Element tipo = doc.createElement("cbc:DocumentTypeCode");
		tipo.appendChild(doc.createTextNode(documento.getTipodocumento()));
		Element serie = doc.createElement("sac:DocumentSerialID");
		serie.appendChild(doc.createTextNode(documento.getSerieelec()));
		Element correlativo = doc.createElement("sac:DocumentNumberID");
		correlativo.appendChild(doc.createTextNode(documento.getNumeroelec()));
		Element raz = doc.createElement("sac:VoidReasonDescription");
		raz.appendChild(doc.createTextNode(razon));
		
		line.appendChild(line_id);
		line.appendChild(tipo);
		line.appendChild(serie);
		line.appendChild(correlativo);
		line.appendChild(raz);
		return line;
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
	
	
	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public List<Cabdocumentos> getDocumentos() {
		return documentos;
	}

	public void setDocumentos(List<Cabdocumentos> documentos) {
		this.documentos = documentos;
	}

	public List<String> getRazones() {
		return razones;
	}

	public void setRazones(List<String> razones) {
		this.razones = razones;
	}
}
