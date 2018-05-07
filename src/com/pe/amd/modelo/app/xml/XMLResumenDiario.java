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

class XMLResumenDiario implements XMLDocument {
	

	private List<Cabdocumentos> boletas;
	private Empresa empresa;
	private String filename,correlacion;
	private int tipo;
	
	
	public XMLResumenDiario(List<Cabdocumentos> boletas, Empresa empresa,
			String filename,String correlacion, int tipo) {
		super();
		this.boletas = boletas;
		this.empresa = empresa;
		this.filename = filename;
		this.correlacion = correlacion;
		this.tipo = tipo;
		if(getBoletas().size() == 0)
			throw new IllegalArgumentException("Para generar el resumen se debe te tener al menos "
					+ "un documento");
	}
	
	public File generarDocumento() throws ParserConfigurationException, TransformerException {
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder	=	docFactory.newDocumentBuilder();
		
		//Creacion del Documento
		Document doc = docBuilder.newDocument();
		Element root = doc.createElement("p:SummaryDocuments");
		
		//Namespaces
		root.setAttribute("xmlns:p","urn:sunat:names:specification:ubl:peru:schema:xsd:SummaryDocuments-1");
		root.setAttribute("xmlns:ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
		root.setAttribute("xmlns:cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
		root.setAttribute("xmlns:cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
		root.setAttribute("xmlns:sac", "urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1");
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
		root.getLastChild().appendChild(doc.createTextNode("1.1"));
		//IDENTIFICADOR DEL RESUMEN
		Element id = doc.createElement("cbc:ID");
		id.appendChild(doc.createTextNode("RC-"+anio+mes+dia+"-"+this.correlacion));
		root.appendChild(id);
		
		//FECHA EMISION DOCUEMTNOS
		Element f_emision = doc.createElement("cbc:ReferenceDate");
			f_emision.appendChild(doc.createTextNode(boletas.get(0).getFechaemision().substring(0, 4) + "-" 
			+ boletas.get(0).getFechaemision().substring(4, 6) + "-" + boletas.get(0).getFechaemision().substring(6)));
		root.appendChild(f_emision);
		
		//FECHA GENERACION DE RESUMEN
		fec_resumen.appendChild(doc.createTextNode(anio+"-"+mes+"-"+dia));
		root.appendChild(fec_resumen);

		//REFERENCIA A LA FIRMA
		root.appendChild(this.referenciaFirma(doc));
		
		//INFO DEL EMISOR
		root.appendChild(this.informacionEmisor(doc));
		
		int k = 1;
		for(Cabdocumentos documento:this.getBoletas()) {
			if(this.tipo == 0) {
				root.appendChild(this.createBoleta(doc,documento,k));
			}else if (this.tipo == 1) {
				root.appendChild(this.createNC(doc,documento,k));
			}else if (this.tipo == 2) {
				root.appendChild(this.createND(doc,documento,k));
			}
			else
				throw new IllegalArgumentException("El parametro tipo n"
						+ "o puede ser otro mas que 0 para Boletas, 1 para NC o 2 para ND");
			k++;
		}
		//Este es el contenedero de la firma
		Element ublext_firma = doc.createElement("ext:UBLExtension");
		Element ublext_cont = doc.createElement("ext:ExtensionContent");
		ublext_firma.appendChild(ublext_cont);
		ublextns.appendChild(ublext_firma);
		
		new Escritor().escribirXML(this.filename, "", doc);
		
		try {
			doc = Firmador.sign(new File(this.filename), empresa.getCe().getBinaryStream(), 
					empresa.getPin(), empresa.getAlias());
		}catch(Exception e) {
			throw new NullPointerException("Error en la Firma DIGITAL: "
					+ e.getMessage());
		}
		return new Escritor().escribirXML(this.filename, "", doc);
	}
	private Node createBoleta(Document doc, Cabdocumentos documento, int pos) {
		Element line = doc.createElement("sac:SummaryDocumentsLine");
		
		line.appendChild(doc.createElement("cbc:LineID"));
		line.getLastChild().appendChild(doc.createTextNode(String.valueOf(pos)));//POSICION
		line.appendChild(doc.createElement("cbc:DocumentTypeCode"));
		line.getLastChild().appendChild(doc.createTextNode("03"));//BOLETA
		line.appendChild(doc.createElement("cbc:ID"));
		line.getLastChild().appendChild(doc.createTextNode(documento.getSerieelec()
				+"-"+documento.getNumeroelec()));//SERIE + NUMERO
		
		//INFORMACION DEL CLIENTE
		line.appendChild(this.informacionCliente(doc,documento));
		//ESTADO DEL ITEM
		line.appendChild(doc.createElement("cac:Status"));
		Element code = doc.createElement("cbc:ConditionCode");code.appendChild(doc.createTextNode("1"));
		line.getLastChild().appendChild(code);//adicionar es por defecto
		
		//Importe total de venta
		Element total = doc.createElement("sac:TotalAmount");
		total.setAttribute("currencyID", "PEN");
		total.appendChild(doc.createTextNode(String.format("%.2f", documento.getTotaldoc()).replace(',', '.')));
		line.appendChild(total);
		
		//TOTALES DE VENTA
		if(documento.getValventaafe() != null)
			if(documento.getValventaafe() != 0.00)
				line.appendChild(this.cabeceraTotales(doc,
					documento.getValventaafe(),"PEN","01"));
		if(documento.getValventaexo() != null)
			if(documento.getValventaexo() != 0.00)
				line.appendChild(this.cabeceraTotales(doc,
					documento.getValventaexo(),"PEN","02"));
		if(documento.getValventaina() != null)
			if(documento.getValventaina() != 0.00)
				line.appendChild(this.cabeceraTotales(doc,
					documento.getValventaina(),"PEN","03"));
		/*
		//SUMATORIA OTROS CARGOS
		Element tot = doc.createElement("cac:AllowanceCharge");
		Element inst = doc.createElement("cbc:ChargeIndicator");
		inst.appendChild(doc.createTextNode("true"));
		Element amount = doc.createElement("cbc:Amount");
		amount.setAttribute("currencyID", "PEN");
		amount.appendChild(doc.createTextNode("0.00"));
		tot.appendChild(inst);
		tot.appendChild(amount);
		line.appendChild(tot);
		*/
		
		//SUMATORIA IGV
		line.appendChild(this.cabeceraImpuestos(doc,"PEN",documento.getIgv(),"1000","IGV","VAT"));
								
		//SUMATORIA ISC
		if(documento.getIsc() != null)
			if(documento.getIgv().doubleValue() != 0.00)
				line.appendChild(this.cabeceraImpuestos(doc,"PEN",documento.getIsc(),"2000","ISC","EXC"));
								
		//SUMATORIA OTROS
		if(documento.getOtros() != null)
			if(documento.getOtros().doubleValue() != 0.00)
				line.appendChild(this.cabeceraImpuestos(doc,"PEN",documento.getOtros(),"9999","OTROS","OTH"));
		
		
		
		return line;
	}
	private Node createNC(Document doc, Cabdocumentos documento,int pos) {return null;}
	private Node createND(Document doc, Cabdocumentos documento, int pos) {return null;}
	
	private Node cabeceraImpuestos(Document doc, String moneda, Double tot, String cod, String nom,
			String ecod) {
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
	private Node informacionCliente(Document doc, Cabdocumentos documento) {
		Element cliente = doc.createElement("cac:AccountingCustomerParty");
		
		String numcliente = documento.getNomcliente().trim();
		String tipocliente = documento.getTipocliente().trim();
		
		if(documento.getTotaldoc().doubleValue() <= 700.00) {
			if(numcliente == null) {
				numcliente = "-";
				tipocliente = "-";
			}
			else if(numcliente.equals("--")) {
				numcliente = "-";
				tipocliente = "-";
			}
		}
		cliente.appendChild(doc.createElement("cbc:CustomerAssignedAccountID"));
		cliente.getLastChild().appendChild(doc.createTextNode(numcliente));
		cliente.appendChild(doc.createElement("cbc:AdditionalAccountID"));
		cliente.getLastChild().appendChild(doc.createTextNode(tipocliente));
		
		return cliente;
	}
	
	private Node cabeceraTotales(Document doc, Double valventaexo, String moneda, String tipo) {
		Element tot = doc.createElement("sac:BillingPayment");
			Element amount = doc.createElement("cbc:PaidAmount");
			amount.setAttribute("currencyID", moneda);
			amount.appendChild(doc.createTextNode(String.format("%.2f", valventaexo.doubleValue()).replace(',', '.')));
			Element inst = doc.createElement("cbc:InstructionID");
			inst.appendChild(doc.createTextNode(tipo));
		tot.appendChild(amount);
		tot.appendChild(inst);
		
		return tot;
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
	
	
	
	public List<Cabdocumentos> getBoletas() {
		return boletas;
	}
	public void setBoletas(List<Cabdocumentos> boletas) {
		this.boletas = boletas;
	}
	public Empresa getEmpresa() {
		return empresa;
	}
	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}
}
