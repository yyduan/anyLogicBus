package com.logicbus.manager.monitor.timer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.anysoft.util.DefaultProperties;
import com.anysoft.util.Properties;
import com.anysoft.util.Settings;
import com.anysoft.util.XmlTools;
import com.anysoft.util.resource.ResourceFactory;
import com.logicbus.backend.timer.DefaultLogListener;
import com.logicbus.backend.timer.Matcher;
import com.logicbus.backend.timer.Task;
import com.logicbus.backend.timer.TaskListener;
import com.logicbus.backend.timer.TimerManager;
import com.logicbus.backend.timer.matcher.Interval;
import com.logicbus.backend.timer.matcher.Once;
import com.logicbus.manager.monitor.AssetManager;
import com.logicbus.manager.monitor.Cell;
import com.logicbus.manager.monitor.CellManager;

public class CellChecker extends Task {

	@Override
	public void run(Object _context, Properties _config, TaskListener _listener) {
		// TODO Auto-generated method stub
		String masterUrl = _config
				.GetValue("master",
						"http://localhost/logicbus/services/core/manager/ActiveCellQuery");

		String slaveUrl = _config
				.GetValue("slave",
						"http://localhost/logicbus/services/core/manager/ActiveCellQuery");

		Settings settings = Settings.get();
		ResourceFactory resourceFactory = (ResourceFactory) settings
				.get("ResourceFactory");
		resourceFactory = resourceFactory == null ? (new ResourceFactory())
				: resourceFactory;

		try {
			Document doc = XmlTools.loadFromInputStream(resourceFactory.load(
					masterUrl, slaveUrl,
					null));
			
			Element root = doc.getDocumentElement();
			
			NodeList eLocations = root.getElementsByTagName("cell");
			
			CellManager cellManager = CellManager.get();
			Cell.TheFactory factory = new Cell.TheFactory();
			int count = eLocations.getLength();
			for (int i = 0 ; i < eLocations.getLength() ; i ++){
				Node n = eLocations.item(i);
				if (n.getNodeType() != Node.ELEMENT_NODE){
					continue;
				}
				Element e = (Element)n;
				String id = e.getAttribute("id");
				Cell cell = cellManager.get(id);
				if (cell == null){
					cell = factory.newInstance(e, settings);
					cellManager.add(id, cell);
				}
				if ((i+1) * 100 / count % 10 == 0){
					if (_listener != null){
						_listener.taskProcess(this, (i + 1) * 100 / count, "已完成");
					}
				}
				executeCheckTask(cell);
			}
		} catch (Exception e) {
			if (_listener != null){
				_listener.taskMessage(this, "Error occured:" + e.getMessage());
			}
		}
	}
	
	private void executeCheckTask(Cell cell) {
		// TODO Auto-generated method stub
		TimerManager tm = TimerManager.get();
		Matcher iterator = new Once();
		Task task = new CheckTask(cell);
		tm.schedule(iterator,task,null);	
	}

	public static class CheckTask extends Task{
		protected Cell cell = null;
		public CheckTask(Cell _cell){
			cell = _cell;
		}
		@Override
		public void run(Object _context, Properties _config,
				TaskListener _listener) {
			// TODO Auto-generated method stub
			Settings settings = Settings.get();
			ResourceFactory resourceFactory = (ResourceFactory) settings
					.get("ResourceFactory");
			resourceFactory = resourceFactory == null ? (new ResourceFactory())
					: resourceFactory;
			
			String url = cell.getCheckUrl();
			if (url != null && url.length() > 0){
				try {
					Document doc = XmlTools.loadFromInputStream(resourceFactory.load(url, null));
					if (doc != null){
						cell.update(doc,AssetManager.get());
						cell.setState(Cell.STATE.VALID);
					}else{
						cell.setState(Cell.STATE.ERROR);
					}
				} catch (Exception e){
					if (_listener != null){
						_listener.taskMessage(this, "Error occured:" + e.getMessage());
					}
					cell.setState(Cell.STATE.ERROR);
					if (cell.isDead()){
						cell.setState(Cell.STATE.DEAD);
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		TimerManager tm = TimerManager.get();
		tm.putLogListener(new DefaultLogListener());
		Matcher iterator = new Interval(20000);
		Task task = new CellChecker();
		Properties props = new DefaultProperties();
		props.SetValue("master", "http://localhost:8080/logicbus/services/core/metadata/ActiveCellQuery");
		tm.schedule(iterator,task,props);		
	}
}
