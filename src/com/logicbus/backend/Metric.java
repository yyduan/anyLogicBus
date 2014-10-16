package com.logicbus.backend;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.Reportable;
import com.logicbus.backend.RRA.RRAValue;

public class Metric implements Reportable{
	protected String id;
	public String getID(){return id;}
	protected Vector<RRA> rras = new Vector<RRA>();

	public Metric(String _id, String rraDefine) {
		id = _id;
		createRRAs(rraDefine);
	}

	public Metric(String _id) {
		this(_id, "SUM:60:720,SUM:3600:720");
	}

	protected void createRRAs(String _rraDefine) {
		StringTokenizer t = new StringTokenizer(_rraDefine, ",");

		while (t.hasMoreTokens()) {
			String rraDef = t.nextToken();
			RRA instance = createRRA(rraDef);
			if (instance != null) {
				rras.add(instance);
			}
		}
	}

	public RRA [] getRRAs(){
		return rras.toArray(new RRA[0]);
	}
	
	public void update(long timestamp,double value){
		for (RRA rra:rras){
			rra.update(timestamp, value);
		}
	}
	
	protected RRA createRRA(String _def) {
		String[] tokens = _def.split(":");
		if (tokens.length != 3)
			return null;
		try {
			String strCF = tokens[0].toUpperCase();
			int step = Integer.parseInt(tokens[1]);
			int rows = Integer.parseInt(tokens[2]);
			RRA.CF cf = RRA.CF.valueOf(strCF);
			return new RRA(cf, step, rows);
		} catch (Exception ex) {
			return null;
		}
	}
	
	public static void main(String [] args){
		Metric m = new Metric("test");
		
		long current = (System.currentTimeMillis() / 60000) * 60000;
		long [] values = {12,32,20,455,23};
		for (long value:values){
			m.update(current, value);
			current += 30 * 1000;
		}
		
		RRA [] rras = m.getRRAs();
		for (RRA rra:rras){
			Iterator<RRAValue> iter = rra.iterator();
			while (iter.hasNext()){
				RRAValue v = iter.next();
				
				if (v != null){
					System.out.println(rra);
					System.out.println(new Date(v.timestamp) + "->" + v.value);
				}
			}
		}
	}

	@Override
	public void report(Element metric) {
		if (metric != null){
			Document doc = metric.getOwnerDocument();
			
			metric.setAttribute("id", getID());
			
			RRA [] _rras = getRRAs();
			if (_rras.length > 0){
	
				for (RRA _rra:_rras){
					Element eRRA = doc.createElement("rra");
					_rra.report(eRRA);								
					metric.appendChild(eRRA);
				}
			}
		}
	}

	@Override
	public void report(Map<String, Object> json) {
		if (json != null){
			json.put("id", getID());
			RRA [] _rras = getRRAs();
			if (_rras.length > 0){
				List<Object> rras = new ArrayList<Object>(_rras.length);
				for (RRA _rra:_rras){
					Map<String,Object> rra = new HashMap<String,Object>();
					_rra.report(rra);
					rras.add(rra);
				}
				json.put("rras", rras);
			}
		}
	}

}
