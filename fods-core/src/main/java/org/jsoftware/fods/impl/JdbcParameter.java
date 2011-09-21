package org.jsoftware.fods.impl;

import java.util.ArrayList;
import java.util.List;

class JdbcParameter {
	private int size;
	private String url, dbId;
	String username, password;
	
	private JdbcParameter() {
	}
	
	public int getSize() {
		return size;
	}
	public String getUrl() {
		return url;
	}
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public String getDbId() {
		return dbId;
	}
	
	public static JdbcParameter[] parse(String s) {
		s = s.replace("loadbalance:", "");
		String[] parts = s.split("/");
		String[] hosts = parts[2].split(",");
		List<JdbcParameter> params = new ArrayList<JdbcParameter>();
		
		for(String host : hosts) {
			JdbcParameter jp = new JdbcParameter();
			jp.url = parts[0] + "//" + host + "/" + parts[3];
			jp.dbId = host;
			params.add(jp);
		}
		return params.toArray(new JdbcParameter[params.size()]);
	}
	
}
