package org.jsoftware.fods.impl;

import java.util.LinkedList;
import java.util.List;

import org.jsoftware.fods.client.ext.FodsDbState;
import org.jsoftware.fods.client.ext.FodsDbState.STATE;
import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.client.ext.FodsState;
import org.jsoftware.fods.client.ext.Selector;


/**
 * Default database {@link Selector}
 * @author gruszecm
 *
 */
public class DefaultRoundrobinSelector implements Selector  {
	private List<String> sequence;
	private long recoveryTime = 1000 * 60 *10;
	
	
	public DefaultRoundrobinSelector(Configuration configuration) {
		this.sequence = new LinkedList<String>();
		String str = configuration.getProperty("recoveryTime");
		if (str != null) {
			recoveryTime = Long.valueOf(str);
		}
		String selectorSeq = configuration.getProperty("selectorSequence", null);
		if (selectorSeq != null) {
			for(String s : selectorSeq.split(",")) {
				s = s.trim();
				if (configuration.getDatabaseConfigurationByName(s) != null) {
					sequence.add(s);
				}
			}
		} else {
			sequence.addAll(configuration.getDatabaseNames());
		}
		if (sequence.isEmpty()) {
			throw new RuntimeException();
		}
	}
	

	public String select(FodsState fodsState) {
		String strStart = fodsState.getCurrentDatabase();
		if (strStart == null) {
			strStart = sequence.get(0);
		}
		String str = strStart;
		do {
			FodsDbState fodsdbState = fodsState.getDbstate(str);
			if (isValid(fodsdbState)) {
				return str;
			}
			int i = sequence.indexOf(str) +1;
			if (i >= sequence.size()) {
				i = 0;
			}
			str = sequence.get(i);
			if (strStart.equals(str)) {
				return null;
			}
		} while(true);
	}
	

	protected boolean isValid(FodsDbState fodsdbState) {
		if (fodsdbState.getState() == STATE.VALID) {
			return true;
		}
		if (fodsdbState.getState() == STATE.BROKEN && fodsdbState.getBrokenTime() >= recoveryTime) {
			return true;
		}
		return false;
	}

}
