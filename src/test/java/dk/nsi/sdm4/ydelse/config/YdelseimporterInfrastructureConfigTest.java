package dk.nsi.sdm4.ydelse.config;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class YdelseimporterInfrastructureConfigTest {
	@Test
	public void canInstantiateSlaLogger() {
		assertNotNull(new YdelseimporterInfrastructureConfig().slaLogger());
	}
}
