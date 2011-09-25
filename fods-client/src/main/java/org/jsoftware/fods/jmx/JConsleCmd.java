package org.jsoftware.fods.jmx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Simple CommandLine JConsole 
 * @author szalik
 * DO NOT USE
 * FIXME dostosowac do wersj 4.0
 */
class JConsleCmd {
	private JMXServiceURL serviceURL;
	private MBeanServerConnection mbc;
	private ObjectName beanName;
	private OPERATION operation;
	private String propertyName;
	private String newValue;
	
	

	/**
	 * @param args
	 * <code>arg0<code> mbeanServer (hostName:portNum)
	 * <code>arg1<code> operation: <tt>get</tt> or <tt>set</tt> or <tt>info</tt>
	 * <code>arg2<code> mbean name
	 * <code>arg3<code> property name <i>case sensitive</i>
	 * <code>arg4<code> new value <i>for <tt>set</tt> only</i>
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			displayHelp();
			System.exit(2);
		}
		JConsleCmd jConsleCmd = new JConsleCmd();
		try {
			jConsleCmd.parseArgs(args);
			jConsleCmd.connect();
		} catch (IOException e) {
			System.out.println("Connection error - " + e.getMessage());
			e.printStackTrace();
			System.exit(3);
		} catch (Exception e) {
			e.printStackTrace();
			displayHelp();
			System.exit(2);
		}
		try {
			Object obj = jConsleCmd.doAction();
			if (obj != null) {
				System.out.println(obj.toString());
			}
		} catch (Exception e) {
			System.out.println("Action error - " + jConsleCmd.operation + ":: " + e.getMessage());
			e.printStackTrace();
			System.exit(4);
		}
	}

	private void parseArgs(String[] args) throws Exception {
		String serviceURLConenctionString = "service:jmx:rmi:///jndi/rmi://" + args[0] + "/jmxrmi";
		// service:jmx:rmi:///jndi/rmi://hostName:portNum/jmxrmi
		serviceURL = new JMXServiceURL(serviceURLConenctionString);
		operation = OPERATION.valueOf(args[1].trim().toUpperCase());
		if (args.length > 2) {
			beanName = new ObjectName(args[2]);
		}
		if (operation == OPERATION.GET || operation == OPERATION.SET) {
			propertyName = args[3].trim();
		}
		if (operation == OPERATION.SET) {
			newValue = args[4];
		}
	}
	
	private Object doAction() throws Exception {
		if (operation == OPERATION.LIST) {
			System.out.println("MBeans:");
			for(Object obj : mbc.queryNames(null, null)) {
				System.out.println("\t" + obj);
			}
			return null;
		}
		if (operation == OPERATION.INFO) {
			MBeanInfo info = mbc.getMBeanInfo(beanName);
			for(MBeanAttributeInfo ai : info.getAttributes()) {
				char[] rwro = {'R', 'O'};
				if (! ai.isReadable()) rwro = new char[] { ' ', ' ' }; 
				if (ai.isWritable()) rwro[1] = 'W';
				System.out.println(ai.getName() + " - " + new String(rwro) + ", type:" + ai.getType() + " (" + ai.getDescription() + ")");
			}
			return null;
		}
		if (operation == OPERATION.NOTIFICATIONS) {
			NotificationListener listener = new NotificationListener() {
				public void handleNotification(Notification n, Object handback) {
					System.out.println(new Date(n.getTimeStamp()) + " :: " + n.toString());
				}
			};
			mbc.addNotificationListener(beanName, listener, null, null);
			System.out.println("Press enter to stop!");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {	br.readLine(); 	} catch (IOException e) { System.out.println("break!"); }
			mbc.removeNotificationListener(beanName, listener);
			return null;
		}
		Object value = mbc.getAttribute(beanName, propertyName);
		if (operation == OPERATION.SET) {
			Object newValueObj = convert(value.getClass(), newValue);
			Attribute attr = new Attribute(propertyName, newValueObj);
			mbc.setAttribute(beanName, attr);
		}
		return value;
	}



	private Object convert(Class<?> type, String val) {
		if (type == String.class) return val;
		if (type == Boolean.class) return Boolean.valueOf(val);
		if (type == Integer.class) return Integer.valueOf(val);
		if (type == Long.class) return Long.valueOf(val);
		if (type == Double.class) return Double.valueOf(val);
		throw new IllegalArgumentException("Can not convert to type " + type);
	}

	private void connect() throws IOException {
		mbc = JMXConnectorFactory.connect(serviceURL).getMBeanServerConnection();
		if (beanName != null && ! mbc.isRegistered(beanName)) {
			throw new IOException("No bean for name " + beanName);
		}
	}

	private static void displayHelp() {
		System.out.println("arg0 - mbeanServer (hostName:portNum)");
		System.out.println("arg1 - operation: get/set/info/list/notifications");
		System.out.println("arg2 - mbean name");
		System.out.println("arg3 - property name, case sensitive");
		System.out.println("arg4 - new value, for set only");
	}

	
}


enum OPERATION {
	GET, SET, INFO, LIST, NOTIFICATIONS
}
