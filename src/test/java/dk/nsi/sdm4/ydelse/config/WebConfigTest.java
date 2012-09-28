package dk.nsi.sdm4.ydelse.config;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class WebConfigTest {
	@Test
	public void canInstantiateStatusReporter() {
		assertNotNull(new WebConfig().statusReporter());
	}
}
