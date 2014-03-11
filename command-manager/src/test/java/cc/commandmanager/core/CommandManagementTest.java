package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class CommandManagementTest {

    private final ChainManagement _chainManager = new ChainManagement();

    @Test
    public void testSetCatalog() {
	this._chainManager.setCatalog("/DummyCatalog1.xml");
	assertThat(this._chainManager.catalog).isNotNull();
    }

    @Test(expected = CatalogNotInstantiableException.class)
    public void testSetCatalogWithNoValidCatalog() throws Exception {
	this._chainManager.setCatalog("");
	fail();
    }

}
