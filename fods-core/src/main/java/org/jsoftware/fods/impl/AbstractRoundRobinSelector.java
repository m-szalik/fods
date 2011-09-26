package org.jsoftware.fods.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.client.ext.Displayable;
import org.jsoftware.fods.client.ext.FodsDbState;
import org.jsoftware.fods.client.ext.FodsDbStateStatus;
import org.jsoftware.fods.client.ext.FodsState;
import org.jsoftware.fods.client.ext.ManageableViaMXBean;
import org.jsoftware.fods.client.ext.Selector;
import org.jsoftware.fods.impl.utils.RequiredPropertyMissing;

/**
 * Base class for round robin {@link Selector} strategy.
 * 
 * @author szalik
 */
public abstract class AbstractRoundRobinSelector implements Selector, ManageableViaMXBean, Displayable {
	protected List<String> sequenceList;
	protected long recoveryTime;
	private String last, selectorName;

	public AbstractRoundRobinSelector(String selectorName, Configuration configuration) {
		this.selectorName = selectorName.endsWith("Selector") ? selectorName : selectorName + "Selector";
		this.sequenceList = new LinkedList<String>();
		String str = configuration.getProperty("selectorMinRecoveryTime");
		if (str != null) {
			recoveryTime = Long.valueOf(str);
		} else {
			new RequiredPropertyMissing("selectorMinRecoveryTime");
		}

		String selectorSeq = configuration.getProperty("selectorSequence", null);
		if (selectorSeq != null) {
			for (String s : selectorSeq.split(",")) {
				s = s.trim();
				if (configuration.getDatabaseConfigurationByName(s) != null) {
					sequenceList.add(s);
				}
			}
		} else {
			sequenceList.addAll(configuration.getDatabaseNames());
		}
		if (sequenceList.isEmpty()) {
			throw new RuntimeException("Property \"sequenceList\" is empty.");
		}
	}

	public Object getMXBeanInstance() {
		return new RoundRobinSelectorMXBean() {
			public List<String> getSequence() {
				return new LinkedList<String>(getSequenceList());
			}
		};
	}

	@SuppressWarnings("unchecked")
	private Collection<? extends String> getSequenceList() {
		return (Collection<? extends String>) (sequenceList == null ? Collections.emptyList() : sequenceList);
	}

	protected String next(FodsState fodsState) {
		int ta = 0, i; String str;
		int size = sequenceList.size();
		if (last == null) {
			i = 0;
		} else {
			i = sequenceList.indexOf(last) +1;
		}
		do {
			if (i == size) {
				i = 0;
			}
			ta++;
			str = sequenceList.get(i);
			FodsDbState fodsdbState = fodsState.getDbstate(str);
			if (isValid(fodsdbState)) {
				break;
			} else {
				i++;
			}
			if (ta > size) {
				str = null;
				break;
			}
		} while(true);
		last = str;
		return str;
	}

	public String asString(boolean addDebugInfo) {
		return selectorName + "(" + sequenceList + ")";
	}

	protected boolean isValid(FodsDbState fodsdbState) {
		if (fodsdbState.getStatus() == FodsDbStateStatus.VALID) {
			return true;
		}
		if (fodsdbState.getStatus() == FodsDbStateStatus.BROKEN && fodsdbState.getBrokenTime() >= recoveryTime) {
			return true;
		}
		return false;
	}

}
