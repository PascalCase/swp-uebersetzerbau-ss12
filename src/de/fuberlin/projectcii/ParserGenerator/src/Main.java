package de.fuberlin.projectcii.ParserGenerator.src;
import java.io.IOException;

import de.fuberlin.commons.lexer.ILexer;
import de.fuberlin.commons.parser.ISyntaxTree;

import de.fuberlin.projecta.analysis.SemanticAnalyzer;
import de.fuberlin.projecta.lexer.Lexer;
import de.fuberlin.projecta.lexer.io.StringCharStream;
import de.fuberlin.projecta.utils.IOUtils;
import de.fuberlin.projectci.lrparser.LRParser;

@SuppressWarnings("unused")
public class Main {
	
	public static void main(String[] args) {
		
		String data;
		try {
			data = IOUtils.readFile("fibonacci.txt");
			ILexer lexer = new Lexer(new StringCharStream(data));
			LL1Parser ll1 = new LL1Parser();
			ISyntaxTree syntaxTree= ll1.parse(lexer,"input/de/fuberlin/projectci/non-ambigous.txt");
			
			//LRParser parser=new LRParser();
			//ISyntaxTree syntaxTree= parser.parse(lexer, "input/de/fuberlin/projectci/non-ambigous.txt");

			SemanticAnalyzer semanticAnalyzer= new SemanticAnalyzer(syntaxTree);
			semanticAnalyzer.analyze();
			semanticAnalyzer.getAST().printTree();	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
