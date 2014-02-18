package org.jsoftware.fods;

import junit.framework.Assert;
import org.jsoftware.fods.client.ext.FodsDbState;
import org.jsoftware.fods.client.ext.FodsDbStateStatus;
import org.jsoftware.fods.client.ext.FodsState;
import org.jsoftware.fods.client.ext.Selector;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class TestOfAllAbstractRoundRobinSelectorTest extends AbstractDbTestTemplate {
	private FodsState fodsState;
	private String currentName;
	private Set<String> invalidDbs;



	@Before
	public void prepareState() {
		invalidDbs = new HashSet<String>();
		fodsState = new FodsDbStateForTesting();
	}



	@Test
	public void testOnFail1() {
		Selector selector = new SelectorWrapper(new RoundRobinOnFailSelectorFactory().getSelector(configuration));
		Assert.assertEquals("db0", selector.select(fodsState));
		Assert.assertEquals("db0", selector.select(fodsState));
		invalidDbs.add("db0");
		Assert.assertEquals("db1", selector.select(fodsState));
		invalidDbs.remove("db0");
		Assert.assertEquals("db1", selector.select(fodsState));
		invalidDbs.add("db1");
		Assert.assertEquals("db0", selector.select(fodsState));
		invalidDbs.add("db2");
		Assert.assertEquals("db0", selector.select(fodsState));
	}



	@Test
	public void testOnAll1() {
		Selector selector = new SelectorWrapper(new RoundRobinSelectorFactory().getSelector(configuration));
		Assert.assertEquals("db0", selector.select(fodsState));
		Assert.assertEquals("db1", selector.select(fodsState));

		invalidDbs.add("db0");
		Assert.assertEquals("db1", selector.select(fodsState));

		invalidDbs.remove("db0");
		Assert.assertEquals("db0", selector.select(fodsState));
	}



	@Test
	public void testOnAllInvalid() {
		Selector selector = new SelectorWrapper(new RoundRobinSelectorFactory().getSelector(configuration));
		Assert.assertEquals("db0", selector.select(fodsState));
		invalidDbs.add("db0");
		invalidDbs.add("db1");
		invalidDbs.add("db2");
		Assert.assertNull(selector.select(fodsState));
	}

	class SelectorWrapper implements Selector {
		private Selector selector;



		public SelectorWrapper(Selector selector) {
			this.selector = selector;
		}



		public String select(FodsState fodsState) {
			String str = selector.select(fodsState);
			currentName = str;
			return str;
		}
	};

	class FodsDbStateForTesting implements FodsState {
		public FodsDbState getDbstate(final String name) {
			return new FodsDbState() {
				public boolean isReadOnly() {
					return false;
				}



				public FodsDbStateStatus getStatus() {
					return invalidDbs.contains(name) ? FodsDbStateStatus.BROKEN : FodsDbStateStatus.VALID;
				}



				public long getBrokenTime() {
					return invalidDbs.contains(name) ? 10 : -1;
				}
			};
		}



		public String getCurrentDatabase() {
			return currentName;
		}
	}
}
