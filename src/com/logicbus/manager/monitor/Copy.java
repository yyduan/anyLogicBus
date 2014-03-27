package com.logicbus.manager.monitor;

import org.w3c.dom.Element;

public class Copy extends MetricHolder {

	protected Cell location = null;
	protected Asset asset = null;
	public Copy(Cell _location, Asset _asset,String metrics) {
		// TODO Auto-generated constructor stub
		location = _location;
		asset = _asset;
		setMetrics(metrics);
	}

	public void update(Element e) {
		// TODO Auto-generated method stub
		super.update(e);
	}
	
	public Cell getLocation(){return location;}
	
	public Asset getAsset(){return asset;}

	public void toXML(Element root, String from) {
		// TODO Auto-generated method stub
		if (from.equals("cell")){
			root.setAttribute("id", asset.getUniqueID());
		}else{
			root.setAttribute("id", location.getID());
		}
		super.toXML(root);
	}
	
}
