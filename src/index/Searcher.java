package index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import utils.Config;
import utils.DiyAnalyzer;

public class Searcher {
	public static String indexDir = Config.indexDir;
	private static IndexReader reader;
	private static DiyAnalyzer analyzer;
//	private static StandardAnalyzer analyzer;
	static {
		analyzer = DiyAnalyzer.getAnalyzer();
//		analyzer = new StandardAnalyzer(Version.LUCENE_30);
		try {
			reader = IndexReader.open(getDirectory(indexDir));
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Searcher() {
		
	}

	public static void main(String[] args) throws Exception {
		String temp = "武汉理工大学";
		// String[] q = temp.split(" ");
		Searcher wsearcher = new Searcher();
//		wsearcher.search1(temp);
		System.out.println(reader.document(1));
	}

	public IndexReader getReader() {
		return reader;
	}

	private static Directory getDirectory(String dir) throws IOException {
		return FSDirectory.open(new File(dir));
	}

	private void search1(String q) throws Exception {
		long start = System.currentTimeMillis();
		IndexSearcher searcher = new IndexSearcher(reader);
		q = analyzer.fragment(q);
		String[] fields = { "title", "text" };
//		BooleanQuery booleanQuery = new BooleanQuery();
		Query query = new MultiFieldQueryParser(Version.LUCENE_30, fields,
				analyzer).parse(q);
//		booleanQuery.add(query, BooleanClause.Occur.SHOULD);

		System.out.println(query.toString());

		TopDocs hits = searcher.search(query, 30);
		long end = System.currentTimeMillis();
		System.out.println("Found " + hits.totalHits + "document(s)(in"
				+ (end - start) + " ms that matched query'" + q + "':");

		for (ScoreDoc scoreDoc : hits.scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			System.out.println(doc.get("title") + "： " + doc.get("url"));
			searcher.explain(query, scoreDoc.doc).toString();
			// Explanation e = searcher.explain(booleanQuery, scoreDoc.doc);
			// System.out.print(e);
		}

		searcher.close();
	}

	/**
	 * 搜索查询词q的前top个结果
	 * 
	 * @param q
	 * @param top
	 * @return
	 * @throws ParseException
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public ArrayList<SearchResult> search(String q, int top) throws Exception {
		long start = System.currentTimeMillis();
		IndexSearcher searcher = new IndexSearcher(reader);
		
		while(q.startsWith(" ")){
			q=q.substring(1);
		}
		while(q.endsWith(" ")){
			q=q.substring(0,q.length()-1);
		}
		
		q = analyzer.fragment(q);
		String[] fields = { "title",  "text" };
		BooleanQuery booleanQuery = new BooleanQuery();
		Query query = new MultiFieldQueryParser(Version.LUCENE_30, fields,
				analyzer).parse(q);
		booleanQuery.add(query, BooleanClause.Occur.SHOULD);

		TopDocs hits = searcher.search(booleanQuery, top);
		long end = System.currentTimeMillis();
		System.out.println(booleanQuery.toString());
		System.out.println(new Date(start).toString() + ":Found " + hits.totalHits + "document(s)(in"
				+ (end - start) + " ms that matched query'" + q + "':");
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		for (ScoreDoc scoreDoc : hits.scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);

			results.add(new SearchResult(doc.get("url"), doc.get("title"), end
					- start, hits.totalHits, doc.get("text"),q,doc.get("date")));
		}

		searcher.close();
		return results;
	}
}
