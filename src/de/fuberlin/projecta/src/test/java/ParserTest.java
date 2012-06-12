import static org.junit.Assert.assertTrue;

import java.io.File;

import lexer.Lexer;
import lexer.SyntaxErrorException;
import lexer.io.FileCharStream;

import org.junit.Test;

import parser.ParseException;
import parser.Parser;


public class ParserTest {

	@Test
	public void testReadSourceFile() throws SyntaxErrorException {
		final String path = Config.TEST_DATA_FOLDER + "LexerTestFile2.txt";
		
		File sourceFile = new File(path);
		assertTrue(sourceFile.exists());
		assertTrue(sourceFile.canRead());

		Lexer lexer = new Lexer(new FileCharStream(path));
		Parser parser = new Parser();
		try {
			parser.parse(lexer, "");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		parser.printParseTree();
	}
}
