package com.pe.amd.modelo.app.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

abstract class Firmador {
	
	public static Document sign(File in, InputStream cer, String pass, String alias)  throws Exception{
		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
		Reference ref = fac.newReference
				 ("", fac.newDigestMethod(DigestMethod.SHA1, null), //DEFINO LA FIRMA SOBRE TODO EL DOCUMENTO
				  Collections.singletonList
				   (fac.newTransform
				    (Transform.ENVELOPED, (TransformParameterSpec) null)), //DEFINO QUE EL TIPO DE FIRMA SERA ENVELOPED
				     null, null);
		
		SignedInfo si = fac.newSignedInfo
				 (fac.newCanonicalizationMethod
				  (CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
				   (C14NMethodParameterSpec) null),
				    fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
				     Collections.singletonList(ref));
		
		// Load the KeyStore and get the signing key and certificate.
		KeyStore ks = KeyStore.getInstance("pkcs12");
		ks.load(cer, pass.toCharArray());
		KeyStore.PrivateKeyEntry keyEntry =
		    (KeyStore.PrivateKeyEntry) ks.getEntry
		        (alias, new KeyStore.PasswordProtection(pass.toCharArray()));
		X509Certificate cert = (X509Certificate) keyEntry.getCertificate();

		// Create the KeyInfo containing the X509Data.
		KeyInfoFactory kif = fac.getKeyInfoFactory();
		List<Object> x509Content = new ArrayList<>();
		x509Content.add(cert.getSubjectX500Principal().getName());
		x509Content.add(cert);
		X509Data xd = kif.newX509Data(x509Content);
		KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document doc = dbf.newDocumentBuilder().parse
		    (new FileInputStream(in+".xml"));

		// Create a DOMSignContext and specify the RSA PrivateKey and
		// location of the resulting XMLSignature's parent element.
		NodeList list = doc.getElementsByTagName("ext:ExtensionContent");
		
		Node n = null;
		for(int i = 0 ; i < list.getLength() ; i++) {
			n = list.item(i);
			if(!n.hasChildNodes())
				break;
		}
		DOMSignContext dsc = new DOMSignContext
		    (keyEntry.getPrivateKey(), n);
		dsc.setDefaultNamespacePrefix("ds");
		// Create the XMLSignature, but don't sign it yet.
		XMLSignature signature = fac.newXMLSignature(si, ki);

		// Marshal, generate, and sign the enveloped signature.
		signature.sign(dsc);
		//TERMINO LA FIRMA
		if ( validate(doc,fac) ) 
			return doc;
		else
			return null;
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean validate(Document doc, XMLSignatureFactory fac) throws Exception {
		//VALIDAMOS LA FIRMA
		
				NodeList nl =
					    doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
					if (nl.getLength() == 0) {
					    throw new Exception("Cannot find Signature element");
					}

					// Create a DOMValidateContext and specify a KeySelector
					// and document context.
					DOMValidateContext valContext = new DOMValidateContext
					    (new X509KeySelector(), nl.item(0));

					// Unmarshal the XMLSignature.
					XMLSignature signature = fac.unmarshalXMLSignature(valContext);

					// Validate the XMLSignature.
					boolean coreValidity = signature.validate(valContext);

					// Check core validation status.
					if (coreValidity == false) {
					    System.err.println("Signature failed core validation");
					    boolean sv = signature.getSignatureValue().validate(valContext);
					    System.out.println("signature validation status: " + sv);
					    if (sv == false) {
					        // Check the validation status of each Reference.
					        Iterator i = signature.getSignedInfo().getReferences().iterator();
					        for (int j=0; i.hasNext(); j++) {
					            boolean refValid = ((Reference) i.next()).validate(valContext);
					            System.out.println("ref["+j+"] validity status: " + refValid);
					        }
					        
					    }
					} else {
					   // System.out.println("Signature passed core validation");
					}
				return coreValidity;
	}
}

class X509KeySelector extends KeySelector {
    @SuppressWarnings("rawtypes")
	public KeySelectorResult select(KeyInfo keyInfo,
                                    KeySelector.Purpose purpose,
                                    AlgorithmMethod method,
                                    XMLCryptoContext context)
        throws KeySelectorException {
        Iterator ki = keyInfo.getContent().iterator();
        while (ki.hasNext()) {
            XMLStructure info = (XMLStructure) ki.next();
            if (!(info instanceof X509Data))
                continue;
            X509Data x509Data = (X509Data) info;
            Iterator xi = x509Data.getContent().iterator();
            while (xi.hasNext()) {
                Object o = xi.next();
                if (!(o instanceof X509Certificate))
                    continue;
                final PublicKey key = ((X509Certificate)o).getPublicKey();
                // Make sure the algorithm is compatible
                // with the method.
                if (algEquals(method.getAlgorithm(), key.getAlgorithm())) {
                    return new KeySelectorResult() {
                        public Key getKey() { return key; }
                    };
                }
            }
        }
        throw new KeySelectorException("No key found!");
    }

    static boolean algEquals(String algURI, String algName) {
        if ((algName.equalsIgnoreCase("DSA") &&
            algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) ||
            (algName.equalsIgnoreCase("RSA") &&
            algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1))) {
            return true;
        } else {
            return false;
        }
    }
}
