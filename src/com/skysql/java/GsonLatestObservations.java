/*
 * This file is distributed as part of the SkySQL Cloud Data Suite.  It is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * version 2.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright SkySQL Ab
 */

package com.skysql.java;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.skysql.java.GsonNode.Nodes;

/**
 * Handle the latest observations for all the Gson objects.
 * Provides methods to save and retrieve the objects and the
 * date when the current instance has last updated them.
 * 
 * @author Massimo Siani
 *
 */
// TODO: Convert this class to a singleton?
public class GsonLatestObservations {
	/**
	 * The default date to return, if any error occurs.
	 */
	private String																m_standardDate;
	/**
	 * The table of (system ID, system) saved by the current instance.
	 */
	private LinkedHashMap<Integer, GsonSystem.Systems>							m_system;
	/**
	 * The table of (system ID, node ID, node) saved by the current instance.
	 */
	private LinkedHashMap<Integer, LinkedHashMap<Integer, GsonNode.Nodes>>		m_node;
	/**
	 * The (monitor ID, monitor classes) table.
	 */
	private LinkedHashMap<Integer, GsonMonitorClasses.MonitorClasses>			m_monitor;
	/**
	 * The (system ID, last system update date) table.
	 */
	private LinkedHashMap<Integer, String>										m_systemDates;
	/**
	 * The (system ID, node ID, last node update date) table.
	 */
	private LinkedHashMap<Integer, LinkedHashMap<Integer, String>>				m_nodeDates;
	/**
	 * The last monitor list update date in rfc 2822 format.
	 */
	private String																m_monitorDates;


	/**
	 * Constructor for the class.
	 */
	public GsonLatestObservations() {
		m_system = new LinkedHashMap<Integer, GsonSystem.Systems>(3);
		m_node = new LinkedHashMap<Integer, LinkedHashMap<Integer, GsonNode.Nodes>>(3);
		m_monitor = new LinkedHashMap<Integer, GsonMonitorClasses.MonitorClasses>(3);
		m_systemDates = new LinkedHashMap<Integer, String>(3);
		m_nodeDates = new LinkedHashMap<Integer, LinkedHashMap<Integer,String>>(3);
		m_monitorDates = "";
		m_standardDate = "Thu, 01 Jan 1970 01:00:00 +0100";
	}

	/**
	 * Get the object that corresponds to a system. The system of interest
	 * is specified by its ID.
	 * 
	 * @param systemID		the ID of the system to retrieve
	 * @return				the Java object of the requested system, null if it doesn't exist
	 */
	public GsonSystem getSystem (int systemID) {
		try {
			GsonSystem gsonSystem = new GsonSystem(m_system.get(systemID));
			return gsonSystem;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get the object that corresponds to a node. The node of is specified by
	 * the system ID the node belongs to, and the ID of the node itself.
	 * 
	 * @param systemID		the ID of the system the node belongs to
	 * @param nodeID		the ID of the node
	 * @return				the object of the node, or null
	 */
	public GsonNode getNode (int systemID, int nodeID) {
		try {
			GsonNode gsonNode = new GsonNode(m_node.get(systemID).get(nodeID));
			return gsonNode;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Return the object that corresponds to a list of all known nodes that belong
	 * to a specified system.
	 * 
	 * @param systemID		the system id of the nodes
	 * @return				the object of the nodes, or null
	 */
	public GsonNode getAllNodes (int systemID) {
		try {
			LinkedHashMap<Integer, Nodes> hmNodes = m_node.get(systemID);
			List<GsonNode.Nodes> nodeList = new ArrayList<GsonNode.Nodes>();
			for (Integer nodeID : hmNodes.keySet()) {
				nodeList.add(hmNodes.get(nodeID));
			}
			GsonNode gsonNode = new GsonNode(nodeList);
			return gsonNode;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Get the object that corresponds to the monitor class with given
	 * monitor ID, if has been cached.
	 * 
	 * @param monitorID		the monitor ID
	 * @return				the object of the monitor, null if not found
	 */
	public GsonMonitorClasses getMonitorClasses (int monitorID) {
		try {
			GsonMonitorClasses gsonMonitorClasses = new GsonMonitorClasses(m_monitor.get(monitorID));
			return gsonMonitorClasses;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Return the object that corresponds to a list of all known monitor classes
	 * 
	 * @return				the object of the monitor classes, or null
	 */
	public GsonMonitorClasses getAllMonitorClasses () {
		try {
			List<GsonMonitorClasses.MonitorClasses> monitorList = new ArrayList<GsonMonitorClasses.MonitorClasses>();
			for (Integer monitorID : m_monitor.keySet()) {
				monitorList.add(m_monitor.get(monitorID));
			}
			GsonMonitorClasses gsonMonitorClasses = new GsonMonitorClasses(monitorList);
			return gsonMonitorClasses;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Get the last time that a system has been updated by this instance.
	 * The system is identified by its ID. If the ID does not exist or has not been
	 * saved yet, a date in the past is returned.
	 * 
	 * @param systemID		the ID of the system
	 * @return				the date when the system has been updated
	 */
	public String getSystemUpdateDate (int systemID) {
		try {
			if (! m_systemDates.containsKey(systemID)) throw new RuntimeException();
			return m_systemDates.get(systemID);
		} catch (Exception e) {
			return m_standardDate;
		}
	}
	
	/**
	 * Get the last date when a node has been updated by the instance.
	 * If the ID of the system or of the node do not exist, or if
	 * the node does not belong to the given system, the returned
	 * value is a date in the past.
	 * 
	 * @param systemID		the ID of the system the node belongs to
	 * @param nodeID		the ID of the node
	 * @return				the date of the node last update
	 */
	public String getNodeUpdateDate (int systemID, int nodeID) {
		try {
			if (! m_nodeDates.containsKey(systemID)) throw new RuntimeException();
			if (! m_nodeDates.get(systemID).containsKey(nodeID)) throw new RuntimeException();
			return m_nodeDates.get(systemID).get(nodeID);
		} catch (Exception e) {
			return m_standardDate;
		}
	}
	
	/**
	 * Get the last time that the monitor list has been updated by this instance.
	 * If the ID does not exist or has not been
	 * saved yet, a date in the past is returned.
	 * 
	 * @param monitorID		the monitor ID
	 * @return				the date when the system has been updated
	 */
	public String getMonitorUpdateDate () {
		String result;
		try {
			result = m_monitorDates;
			if (m_monitorDates.isEmpty() || result == null) {
				result = m_standardDate;
			}
		} catch (Exception e) {
			result = m_standardDate;
		}
		return result;
	}

	/**
	 * Save a system object. If the object contains a list of systems,
	 * each of them is saved separately with the current date.
	 * 
	 * @param systemObj		the object to save
	 */
	public void setLastSystem (GsonSystem systemObj) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
			String now = sdf.format(new Date());
			Iterator<GsonSystem.Systems> it = systemObj.getSystems().iterator();
			while (it.hasNext()) {
				GsonSystem.Systems systemTmp = it.next();
				int systemID = systemTmp.getSystemId();
				m_system.put(systemID, systemTmp);
				m_systemDates.put(systemID, now);
			}
		} catch (Exception e) {
			// IGNORE
		}
	}

	/**
	 * Save a node object. If the object contains a list of nodes,
	 * each of them is saved separately with the current date.
	 * 
	 * @param nodeObj		the object to save
	 */
	public void setLastNode (GsonNode nodeObj) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
			String now = sdf.format(new Date());
			Iterator<GsonNode.Nodes> it = nodeObj.getNodes().iterator();
			while (it.hasNext()) {
				GsonNode.Nodes nodeTmp = it.next();
				int systemID = nodeTmp.getSystemId();
				int nodeID = nodeTmp.getNodeId();
				LinkedHashMap<Integer, GsonNode.Nodes> lhm = new LinkedHashMap<Integer, GsonNode.Nodes>(1);
				if (m_node != null && m_node.containsKey(systemID))
					lhm = m_node.get(systemID);
				lhm.put(nodeID, nodeTmp);
				m_node.put(systemID, lhm);
				LinkedHashMap<Integer, String> lhmDate = new LinkedHashMap<Integer, String>(1);
				if (m_nodeDates != null && m_nodeDates.containsKey(systemID))
					lhmDate = m_nodeDates.get(systemID);
				lhmDate.put(nodeID, now);
				m_nodeDates.put(systemID, lhmDate);
			}
		} catch (Exception e) {
			// IGNORE
		}
	}
	
	/**
	 * Remove all the cached data about a system.
	 * 
	 * @param systemID	the system ID
	 */
	public void clearAllNodes(int systemID) {
		try {
			m_node.get(systemID).clear();
			m_node.remove(systemID);
		} catch (Exception e) {
			// IGNORE
		}
	}

	/**
	 * Save all the monitors in the object and
	 * set the current time as the last monitor list update time.
	 * 
	 * @param		the monitor class object
	 */
	public void setLastMonitor (GsonMonitorClasses monitorObj) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
			String now = sdf.format(new Date());
			Iterator<GsonMonitorClasses.MonitorClasses> it = monitorObj.getMonitorClasses().iterator();
			while (it.hasNext()) {
				GsonMonitorClasses.MonitorClasses monitorTmp = it.next();
				Integer monitorID = monitorTmp.getMonitorId();
				m_monitor.put(monitorID, monitorTmp);
				m_monitorDates = now;
			}
		} catch (Exception e) {
			// IGNORE
		}
	}
	
}
