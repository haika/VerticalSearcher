package utils;

import java.io.Reader;

import org.apache.lucene.analysis.WhitespaceTokenizer;

public class DiyTokenizier extends WhitespaceTokenizer{

	public DiyTokenizier(Reader in) {
		super(in);
	}

	public static void main(String[] args) {
		
	}


}
