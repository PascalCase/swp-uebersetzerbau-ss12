package parsetokdef;

import java.util.List;

import lexergen.Settings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import utils.IRule;

/**
 * 
 * @author benjamin
 */
public class ReadTokDefinitionTest {

	@Before
	public void readSettings() {
		Settings.readSettings();
	}

	/**
	 * Test of readFile method, of class ReadTokDefinition.
	 */
	@Test
	public void testReadFile() throws Exception {
		String path = Settings.getDefaultTokenDef();
		ReadTokDefinition instance = new ReadTokDefinition();
		instance.readFile(path);
	}

	@Test
	public void testRegex() throws Exception {
		String path = null;
		List<IRule> rules = new ReadTokDefinition(path).getRules();
		String tokenType = rules.get(0).getTokenType();
		String tokenValue = rules.get(0).getTokenValue();
		String tokenRegex = rules.get(0).getRegexp();
		System.out.println(rules.get(0));
		Assert.assertEquals("BRACKET", tokenType);
		Assert.assertEquals("(", tokenValue);
		Assert.assertEquals("\\(", tokenRegex);
		tokenType = rules.get(5).getTokenType();
		tokenValue = rules.get(5).getTokenValue();
		System.out.println(rules.get(5));
		Assert.assertEquals("OP", tokenType);
		Assert.assertEquals("L", tokenValue);
	}
}
