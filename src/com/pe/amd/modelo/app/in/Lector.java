package com.pe.amd.modelo.app.in;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Lector {
	
	private File file;
	//Atributos para el manejo de CDR
	private String mensaje = null;//Mensaje de Homologacion 
	private File zip = null; //Apunta al zip del cdr
	
	private FileInputStream  ref1,ref2;
	public Lector(File file) {
		setFile(file);
	}
	public Lector(String file) {
		if(file == null)
			setFile(null);
		else
			setFile(new File(file));
	}

	public byte[] getZipAsByteArray() throws IOException {
		FileInputStream in = new FileInputStream(zip);
		byte[] array = new byte[ (int)zip.length()];
		in.read(array);

		in.close();
		
		return array;
	}
	
	public FileInputStream getZipAsFileInputStream() throws FileNotFoundException {
		ref1 = new FileInputStream(zip);
		return ref1;
	}
	
	public byte[] getFileAsByteArray() throws IOException {
		FileInputStream in = new FileInputStream(file);
		byte[] array = new byte[ (int)file.length()];
		in.read(array);
		in.close();
		return array;
	}
	
	public FileInputStream getFileAsFileInputStream() throws FileNotFoundException {
		ref2 = new FileInputStream(file);
		return ref2;
	}
	
	public int decodeRespuesta(String nombre_doc) throws ParserConfigurationException, SAXException, IOException {
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();	
		Document doc = dBuilder.parse(file);
		
		NodeList nl = doc.getElementsByTagName("applicationResponse");
		if(nl.getLength() == 0) {
			String msj = "";
			nl = doc.getElementsByTagName("soap-env:Fault");
			nl = nl.item(0).getChildNodes();
			Node n = nl.item(0);
			msj += ("["+n.getNodeName()+":"+n.getTextContent()+"]-" );
			n = nl.item(1);
			msj += ("["+n.getNodeName()+":"+n.getTextContent()+"]" );
			setMensaje(msj);
			//System.out.println("MENSAJE ERROR: "+msj);
			return -1;//Rechazo en produccion
		}
		else {
			String s = nl.item(0).getTextContent();//Extraigo el contenido del zip
			byte[] zip = Base64.getDecoder().decode(s.getBytes());
			setZip(new File("response-"+nombre_doc+".zip"));//Guardo la que sera la referencia al zip
			FileOutputStream fout = new FileOutputStream(getZip());//Escribo el contenido
			fout.write(zip);
			fout.close();
			
			//Extraigo la constancia de recepcion
			ZipFile zipFile = new ZipFile(getZip());
			Document cdr = null;
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while(entries.hasMoreElements()){
		        ZipEntry entry = entries.nextElement();
		        if(!entry.isDirectory()) {
			        InputStream stream = zipFile.getInputStream(entry);
			        cdr = dBuilder.parse(stream);
			        break;
		        }
			}
			zipFile.close();
			
			nl = cdr.getElementsByTagName("cac:Response");
			Node response = nl.item(0);
			nl = response.getChildNodes();
			int ret = -2;
			String msj = "";
			for(int i = 0 ; i < nl.getLength() ; i++) {
				Node temp = nl.item(i);
				if(temp.getNodeName().equals("cbc:ResponseCode")) 
					if(Integer.parseInt(temp.getTextContent()) == 0 
							|| Integer.parseInt(temp.getTextContent()) >= 4000)
						ret = 1;
				msj += ("["+temp.getNodeName()+":"+temp.getTextContent()+"]" );
			}
			setMensaje(msj);
			return ret;
		}
	}
	public String decodeSummaryRespuesta(String nombre,boolean produccion) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();	
		Document doc = dBuilder.parse(file);
		
		NodeList nl = doc.getElementsByTagName((produccion?"ser":"br")+":sendSummaryResponse");
		if(nl.getLength() == 0) {
			String msj = "";
			nl = doc.getElementsByTagName("soap-env:Fault");
			nl = nl.item(0).getChildNodes();
			Node n = nl.item(0);
			msj += ("["+n.getNodeName()+":"+n.getTextContent()+"]-" );
			n = nl.item(1);
			msj += ("["+n.getNodeName()+":"+n.getTextContent()+"]" );
			setMensaje(msj);

			return "-1";
		}
		else{
			NodeList t= doc.getElementsByTagName("ticket");
			String ticket = t.item(0).getTextContent();
			return ticket;	
		}
	}

	public Object[] decodeStatus() throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();	
		Document doc = dBuilder.parse(file);

		NodeList nl = doc.getElementsByTagName("ns2:getStatusResponse");
		if(nl.getLength() == 0) {
			String msj = "";
			nl = doc.getElementsByTagName("soap-env:Fault");
			nl = nl.item(0).getChildNodes();
			Node n = nl.item(0);
			msj += ("["+n.getNodeName()+":"+n.getTextContent()+"]-" );
			n = nl.item(1);
			msj += ("["+n.getNodeName()+":"+n.getTextContent()+"]" );
			setMensaje(msj);
			//System.out.println("MENSAJE ERROR: "+msj);
			return null;
		}
		else{
			Object[] retorno = new Object[2];
			NodeList t= doc.getElementsByTagName("statusCode");
			String val = t.item(0).getTextContent();

			retorno[0] = Integer.parseInt(val);
			t = doc.getElementsByTagName("content");
			retorno[1] = t.item(0).getTextContent();
			
			return retorno;	
		}
	}
	public void decodeStatus(String base64File,String tipo) throws IOException {
		byte[] zip = Base64.getDecoder().decode(base64File.getBytes());
		setZip(new File("response-"+tipo+".zip"));//Guardo la que sera la referencia al zip
		FileOutputStream fout = new FileOutputStream(getZip());//Escribo el contenido
		fout.write(zip);
		fout.close();
	}
	
	public String[][] decodeResumen(InputStream binaryStream, String tipo) throws ParserConfigurationException
		, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();	
		Document doc = dBuilder.parse(binaryStream);
		
		String[][] datos = null;
		
		if(tipo.equals("RA")) {
			NodeList nl = doc.getElementsByTagName("sac:VoidedDocumentsLine");
			if(nl.getLength() == 0)
				throw new IllegalArgumentException("ERROR EN LA LECTURA DEL RESUMEN DE BAJAS"
						+ ", NO FUE ENCONTRADO NINGUN ARCHIVO");
			else {
				datos = new String[nl.getLength()][2];
				for(int i = 0 ; i < nl.getLength() ; i++) {
					NodeList baja = nl.item(i).getChildNodes();
					for(int j = 0 ; j < baja.getLength() ; j++) {
						if(baja.item(j).getNodeName().equals("sac:DocumentSerialID"))
							datos[i][0] = baja.item(j).getNodeValue();
						else if(baja.item(j).getNodeName().equals("sac:DocumentNumberID"))
							datos[i][1] = baja.item(j).getNodeValue();
					}
					
				}
			}
		}
		else if(tipo.equals("RC")) {
			NodeList nl = doc.getElementsByTagName("sac:SummaryDocumentsLine");
			if(nl.getLength() == 0)
				throw new IllegalArgumentException("ERROR EN LA LECTURA DEL RESUMEN DE BOLETAS"
						+ ", NO FUE ENCONTRADO NINGUN ARCHIVO");
			else{
				datos = new String[nl.getLength()][2];
				for(int i = 0 ; i < nl.getLength() ; i++) {
					NodeList resumen = nl.item(i).getChildNodes();
					for(int j = 0 ; j < resumen.getLength() ; j++) {
						if(resumen.item(j).getNodeName().equals("cbc:ID")) {
							int idx = nl.item(j).getNodeValue().indexOf('-');
							datos[i][0] = resumen.item(j).getNodeValue().substring(0, idx);
							datos[i][1] = resumen.item(j).getNodeValue().substring(idx+1);
						}
					}
					
				}
			}
		}
		return datos;
	}
	
	
	public void close() {
		try {this.ref1.close();}catch(Exception e) {}
		try {this.ref2.close();}catch(Exception e) {}
	}
	public void deleteFiles() {
		try {this.zip.delete();}catch(Exception e) {}
		try {this.file.delete();}catch(Exception e) {}
	}
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public File getZip() {
		return zip;
	}

	public void setZip(File zip) {
		this.zip = zip;
	}
	
	
	

}
