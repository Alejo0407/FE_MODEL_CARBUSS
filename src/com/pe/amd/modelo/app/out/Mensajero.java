package com.pe.amd.modelo.app.out;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;



public class Mensajero {
	private String url;
	private File archivo;
	private String[] respuesta;
	
	public Mensajero(String string, File archivo) {
		setUrl(string);
		setArchivo(archivo);
	}
	
	public File sendBill(String ruc, String usr, String pass) throws UnsupportedOperationException, SOAPException, TransformerException, ParserConfigurationException, SAXException, IOException {
		SOAPConnectionFactory connFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection conn = connFactory.createConnection();
		SOAPMessage msj = requestBillService(ruc,usr,pass);
		SOAPMessage resp = conn.call(msj, url);
		return response(resp);
	}
	public SOAPMessage requestBillService(String ruc, String usr, String password) throws SOAPException, IOException {
		MessageFactory mf = MessageFactory.newInstance();
		SOAPMessage message = mf.createMessage();
		
		message.getSOAPPart().getEnvelope().addNamespaceDeclaration("soap", "http:"
				+ "//schemas.xmlsoap.org/soap/envelope/");
		message.getSOAPPart().getEnvelope().addNamespaceDeclaration("ser", "http://service.sunat.gob.pe");
		message.getSOAPPart().getEnvelope().addNamespaceDeclaration("wsse", "http://docs.oasis-open.org/"
				+ "wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		
		//CABECERA
		SOAPHeader header = message.getSOAPHeader();
		SOAPElement he1 = header.addChildElement("Security", "wsse");
		SOAPElement he2 = he1.addChildElement("UsernameToken", "wsse");
		SOAPElement user = he2.addChildElement("Username", "wsse");
		SOAPElement pass = he2.addChildElement("Password", "wsse");
		
		if(url.equals(URLSunat.BetaFactura.getValor())) {
			user.addTextNode(ruc+"MODDATOS");//RUC+USUARIO
			pass.addTextNode("MODDATOS");
		}else {
			user.addTextNode(ruc+usr);//RUC+USUARIO
			pass.addTextNode(password);
		}
		
		
		//CUERPO
		SOAPBody body = message.getSOAPBody();
		SOAPElement be1 = body.addChildElement("sendSummary", "ser");
		SOAPElement filename = be1.addChildElement("fileName");
		filename.addTextNode(getArchivo().getName());
		SOAPElement content = be1.addChildElement("contentFile");
		String cont = encodeToBase64Binary(getArchivo().getName());
		content.addTextNode(cont);
	
		//System.out.println("SOAP-REQUEST");
		//message.writeTo(System.out);
		
		return message;
	}
	
	
	public File enviar(String ruc, String usr, String pass) throws Exception {
		
		SOAPConnectionFactory connFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection conn = connFactory.createConnection();
		SOAPMessage msj = request(ruc,usr,pass);
		SOAPMessage resp = conn.call(msj, url);
		return response(resp);
	}
	
	public SOAPMessage request(String ruc,String usr,String password) throws Exception{
		MessageFactory mf = MessageFactory.newInstance();
		SOAPMessage message = mf.createMessage();
		
		message.getSOAPPart().getEnvelope().addNamespaceDeclaration("soap", "http:"
				+ "//schemas.xmlsoap.org/soap/envelope/");
		message.getSOAPPart().getEnvelope().addNamespaceDeclaration("ser", "http://service.sunat.gob.pe");
		message.getSOAPPart().getEnvelope().addNamespaceDeclaration("wsse", "http://docs.oasis-open.org/"
				+ "wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		
		//CABECERA
		SOAPHeader header = message.getSOAPHeader();
		SOAPElement he1 = header.addChildElement("Security", "wsse");
		SOAPElement he2 = he1.addChildElement("UsernameToken", "wsse");
		SOAPElement user = he2.addChildElement("Username", "wsse");
		SOAPElement pass = he2.addChildElement("Password", "wsse");
		
		if(url.equals(URLSunat.BetaFactura.getValor())) {
			user.addTextNode(ruc+"MODDATOS");//RUC+USUARIO
			pass.addTextNode("MODDATOS");
		}else {
			user.addTextNode(ruc+usr);//RUC+USUARIO
			pass.addTextNode(password);
		}
		
		
		//CUERPO
		SOAPBody body = message.getSOAPBody();
		SOAPElement be1 = body.addChildElement("sendBill", "ser");
		SOAPElement filename = be1.addChildElement("fileName");
		filename.addTextNode(getArchivo().getName());
		SOAPElement content = be1.addChildElement("contentFile");
		String cont = encodeToBase64Binary(getArchivo().getName());
		content.addTextNode(cont);
	
		//System.out.println("SOAP-REQUEST");
		//message.writeTo(System.out);
		
		return message;
	}
	
	public File response(SOAPMessage response) throws SOAPException, TransformerException, ParserConfigurationException, SAXException, IOException{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		Source sourceContent = response.getSOAPPart().getContent();
		
		File resp = new File("response.xml");
		StreamResult rs = new StreamResult(new FileOutputStream(resp));
		transformer.transform(sourceContent, rs);
		//rs = new StreamResult(new FileOutputStream(new File("response.xml")));
		//transformer.transform(sourceContent, rs);
		//System.out.println("\nSOAP-RESPONSE");
		//transformer.transform(sourceContent, new StreamResult(System.out));
		return resp;
	}
	
	public String encodeToBase64Binary(String fileName)
			throws IOException {

		File file = new File(fileName);
		byte[] bytes = loadFile(file);
		byte[] encoded = Base64.getEncoder().encode(bytes);
		String encodedString = new String(encoded);

		return encodedString;
	}
	
	public byte[] encodeToBase64BinaryBytes(String fileName)
			throws IOException {

		File file = new File(fileName);
		byte[] bytes = loadFile(file);
		byte[] encoded = Base64.getEncoder().encode(bytes);

		return encoded;
	}

	private byte[] loadFile(File file) throws IOException {
	    InputStream is = new FileInputStream(file);

	    long length = file.length();
	    if (length > Integer.MAX_VALUE) {
	        // File is too large
	    }
	    byte[] bytes = new byte[(int)length];
	    
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }

	    if (offset < bytes.length) {
	        extracted(file);
	    }

	    is.close();
	    return bytes;
	}

	private void extracted(File file) throws IOException {
		throw new IOException("Could not completely read file "+file.getName());
	}

	public File getStatus(String selectedTicket, String ruc, String usrSecundario, String pass) throws UnsupportedOperationException, SOAPException, TransformerException, ParserConfigurationException, SAXException, IOException {
		SOAPConnectionFactory connFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection conn = connFactory.createConnection();
		SOAPMessage msj = requestStatus(selectedTicket,ruc,usrSecundario,pass);
		SOAPMessage resp = conn.call(msj, url);
		return responseStatus(resp);
	}
	public SOAPMessage requestStatus(String ticket, String ruc, String usr, String password) throws SOAPException, IOException {
		MessageFactory mf = MessageFactory.newInstance();
		SOAPMessage message = mf.createMessage();
		
		message.getSOAPPart().getEnvelope().addNamespaceDeclaration("soap", "http:"
				+ "//schemas.xmlsoap.org/soap/envelope/");
		message.getSOAPPart().getEnvelope().addNamespaceDeclaration("ser", "http://service.sunat.gob.pe");
		message.getSOAPPart().getEnvelope().addNamespaceDeclaration("wsse", "http://docs.oasis-open.org/"
				+ "wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		
		//CABECERA
		SOAPHeader header = message.getSOAPHeader();
		SOAPElement he1 = header.addChildElement("Security", "wsse");
		SOAPElement he2 = he1.addChildElement("UsernameToken", "wsse");
		SOAPElement user = he2.addChildElement("Username", "wsse");
		SOAPElement pass = he2.addChildElement("Password", "wsse");
		
		if(url.equals(URLSunat.BetaFactura.getValor())) {
			user.addTextNode(ruc+"MODDATOS");//RUC+USUARIO
			pass.addTextNode("MODDATOS");
		}else {
			user.addTextNode(ruc+"RCARBUS6");//RUC+USUARIO
			pass.addTextNode("billy2020");
		}
		
		
		//CUERPO
		SOAPBody body = message.getSOAPBody();
		SOAPElement be1 = body.addChildElement("getStatus", "ser");
		SOAPElement filename = be1.addChildElement("ticket");
		filename.addTextNode(ticket);
	
		//System.out.println("SOAP-REQUEST");
		//message.writeTo(System.out);
		
		return message;
	}
	public File responseStatus(SOAPMessage response) throws SOAPException, TransformerException, ParserConfigurationException, SAXException, IOException{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		Source sourceContent = response.getSOAPPart().getContent();
		
		File resp = new File("response-status.xml");
		StreamResult rs = new StreamResult(new FileOutputStream(resp));
		transformer.transform(sourceContent, rs);
		return resp;
	}

	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public File getArchivo() {
		return archivo;
	}

	public void setArchivo(File archivo) {
		this.archivo = archivo;
	}

	public String[] getRespuesta() {
		return respuesta;
	}

	public void setRespuesta(String[] respuesta) {
		this.respuesta = respuesta;
	}
}
