package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

public class CommunicationContextTest {

    @Test
    public void testClone_equal() {
	CommunicationContext context = new CommunicationContext();
	context.put("Key", "Value");
	assertThat(context.clone()).isEqualTo(context);
    }

    @Test
    public void testClone_notTheSame() {
	CommunicationContext context = new CommunicationContext();
	assertThat(context.clone()).isNotSameAs(context);
    }
}
