package com.pe.amd.modelo.app.xml;

import java.io.File;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public interface XMLDocument {
	
	public File generarDocumento() throws ParserConfigurationException, TransformerException;

}
