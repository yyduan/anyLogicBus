package com.logicbus.backend;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.Reportable;


public class RRA implements Reportable{
	//样本个数
	protected int rows = 720;
	public int getRows(){return rows;}
	//集合函数
	enum CF {AVG,MAX,MIN,LAST,SUM}; 
	protected CF cf = CF.LAST;
	
	public static DecimalFormat df = new DecimalFormat("#.00"); 
	
	//每个样本的时间区域,60秒
	protected int step = 60 * 1000;
	public int getStep(){return step;}
	
	public RRA(CF _cf,int _step,int _rows){
		cf = _cf;
		step = _step * 1000;
		rows = _rows;
		rras = new RRAValue[rows];
	}
	
	public RRA(CF _cf,int _step){
		this(_cf,_step,720);
	}
	public RRA(CF _cf){
		this(_cf,60);
	}
	
	public String toString(){
		return "[" + cf.toString() + ":" + step + ":" + rows + "]";
	}
	
	//数据
	protected RRAValue[] rras = null;
	//最新的时间戳
	protected long lastest = 0;
	private double sum = 0;
	private long count = 0;
	
	public void update(long timestamp,double value){
		timestamp = (timestamp / step)*step;
		lastest = timestamp;
		
		int index = (int)(timestamp/step) % rows;
		if (rras[index] == null){
			rras[index] = new RRAValue();
		}

		if (rras[index].timestamp != timestamp){
			//新的cdp
			rras[index].timestamp = timestamp;
			rras[index].value = value;
			sum = value;
			count = 1;
		}else{
			switch (cf){
				case AVG:
					sum += value;
					count ++;
					rras[index].value = sum / count;
					break;
				case MAX:
					if (rras[index].value < value){
						rras[index].value = value;
					}
					break;
				case MIN:
					if (rras[index].value > value){
						rras[index].value = value;
					}					
					break;
				case SUM:
					rras[index].value += value;
					break;
				default:
					rras[index].value = value;
			}
		}
	}

	public void report(Element eRRA) {
		if (eRRA != null){
			eRRA.setAttribute("step", String.valueOf(step));
			eRRA.setAttribute("rows", String.valueOf(rows));
			eRRA.setAttribute("cf", cf.toString());
			
			Document doc = eRRA.getOwnerDocument();
			Iterator<RRAValue> iter = iterator();
			while (iter.hasNext()){			
				RRAValue v = iter.next();
				
				if (v != null){
					Element eValue = doc.createElement("value");
					eValue.setAttribute("t", String.valueOf(v.timestamp));
					eValue.setAttribute("v", df.format(v.value));
					eRRA.appendChild(eValue);
				}
			}
		}
	}	
	

	public void report(Map<String, Object> json) {
		if (json != null){
			json.put("step", step);
			json.put("rows", rows);
			json.put("cf", cf.toString());
			
			List<Object> values = new ArrayList<Object>();
			Iterator<RRAValue> iter = iterator();
			while (iter.hasNext()){			
				RRAValue v = iter.next();			
				if (v != null){
					Map<String,Object> value = new HashMap<String,Object>(2);
					value.put("t", v.timestamp);
					value.put("v", v.value);
					values.add(value);
				}
			}
			json.put("values", values);
		}
	}	
	
	public static class RRAValue {
		public long timestamp = 0;
		public double value = 0;
	}
	
	public Iterator<RRAValue> iterator(){
		return new MyIterator(this);
	}
	
	public static class MyIterator implements Iterator<RRAValue>{
		protected RRA rra;
		protected long lastest = 0;
		protected int current = 0;
		protected int count = 0;
		public MyIterator(RRA _rra){
			rra = _rra;
			lastest = rra.lastest;
			current = (int)(lastest / rra.step) % rra.rows;
		}
		@Override
		public boolean hasNext() {
			return count < rra.rows;
		}

		@Override
		public RRAValue next() {
			RRAValue value = rra.rras[current];
			current --;
			count ++;
			if (current < 0){
				current = rra.rows - 1;
			}
			return value;
		}

		@Override
		public void remove() {
			current = (int)lastest % rra.rows;
			count = 0;
		}
		
	}
	
	public static void main(String []args){
		try {
			RRA m = new RRA(RRA.CF.SUM,60);
			
			long current = (System.currentTimeMillis() / 60000) * 60000;
			long [] values = {12,32,20,455};
			for (long value:values){
				m.update(current, value);
				current += 90 * 1000;
			}

			Iterator<RRAValue> iter = m.iterator();
			int count = 0;
			while (iter.hasNext()){
				RRAValue v = iter.next();
				
				if (v != null){
					count ++;
					System.out.println(v.timestamp + "->" + v.value);
				}

			}

			System.out.println(count);
		}catch (Exception ex){
			ex.printStackTrace();
		}
		
	}


}
