package com.logicbus.backend.message;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;


import org.w3c.dom.Document;
import org.w3c.dom.Element;


import com.anysoft.util.IOTools;
import com.anysoft.util.XmlTools;

/**
 * XML 消息
 * @author duanyy
 *
 * @version 1.0.4 [20140410 duanyy] <br>
 * - 提升encoding为父类成员
 * 
 */
public class XMLMessage extends Message {
	protected Document doc = null;
	protected Element root = null;

	public Document getDocument(){return doc;}
	public Element getRoot(){return root;}
	
	public XMLMessage(MessageDoc _doc,StringBuffer buf,String _encoding){
		super(_doc,_encoding);
		if (buf.length() > 0){
			try {
				doc = XmlTools.loadFromContent(buf.toString());
			} catch (Exception ex) {
				
			}
		}
		if (doc == null){
			try {
				doc = XmlTools.newDocument("root");
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}		
		root = doc.getDocumentElement();

		setContentType("text/xml;charset="+encoding);
	}
	@Override
	public void output(OutputStream out,HttpServletResponse response) {
		root.setAttribute("code",msgDoc.getReturnCode());
		root.setAttribute("reason", msgDoc.getReason());
		root.setAttribute("duration", String.valueOf(msgDoc.getDuration()));
		
		try {
			XmlTools.saveToOutputStream(doc, out);
		} catch (Exception ex) {
			ex.printStackTrace();
		}finally {
			IOTools.closeStream(out);
		}
	}
	
	public String toString(){
		try {
			return XmlTools.node2String(doc);
		} catch (TransformerException e) {
			return doc.toString();
		}
	}
}
