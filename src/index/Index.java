package index;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import utils.Config;
import utils.DiyAnalyzer;
import utils.MySQL;

public class Index {
	// private static String dataDir = Config.dataDir;
	private static String indexDir = Config.indexDir;
	private IndexWriter writer;
	private MySQL db;
	private DiyAnalyzer analyzer;

	/**
	 * 构造函数，初始化时同时打开IndexWriter
	 * 
	 * @param indexDir
	 * @throws IOException
	 */
	public Index(String indexDir) throws IOException {
		analyzer = DiyAnalyzer.getAnalyzer();
		Directory dir = FSDirectory.open(new File(indexDir));
		// writer = new IndexWriter(dir,new IKAnalyzer(true),true,
		// IndexWriter.MaxFieldLength.UNLIMITED);//每次创建索引都删除原来的
		writer = new IndexWriter(dir, analyzer, true,
				IndexWriter.MaxFieldLength.UNLIMITED);
	}

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();
		Index index = new Index(indexDir);
		int numIndexed = 0;
		try {
			// numIndexed = index.index(dataDir, new TextFilesFilter());//
			numIndexed = index.indexDb();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			index.close();
		}
		long end = System.currentTimeMillis();
		System.out.println("Indexing " + numIndexed + "weibos took"
				+ (end - start) + "ms");
	}

	private void close() throws IOException {
		writer.close();
	}

	// /**
	// * 仅索引txt文档
	// * @author 
	// */
	// private static class TextFilesFilter implements FileFilter{
	// public boolean accept(File path){
	// return path.getName().toLowerCase().endsWith(Config.fileFormat);
	// }
	// }

	private int indexDb() throws SQLException, CorruptIndexException,
			IOException {
		db = new MySQL();
		ResultSet rs = db.selectAll();
		int i = 0;
		while (rs.next()) {
			Document doc = getDocument(rs);
			writer.addDocument(doc);
			if (i % 50 == 0) {
				System.out.println(i);
			}
			i++;
		}
		return writer.numDocs();
	}

	private Document getDocument(ResultSet rs) throws SQLException {
		Document document = new Document();

		org.jsoup.nodes.Document jDoc = Jsoup.parse(rs.getString("html_text"));

		document.add(new Field("url", rs.getString("url"), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		document.add(new Field("text", jDoc.select("body").text(),
				Field.Store.YES, Field.Index.ANALYZED));

		Field f1 = new Field("title", parseTitle(jDoc), Field.Store.YES,
				Field.Index.ANALYZED);
		f1.setBoost((float) 1.1);
		document.add(f1);

		document.add(new Field("date", rs.getString("parse_time"),
				Field.Store.YES, Field.Index.NOT_ANALYZED));

		String[] keywords = parseKeyword(jDoc);
		if (null != keywords) {
			for (String s : keywords) {
				Field f = new Field("keyword", s, Field.Store.NO,
						Field.Index.ANALYZED);
				f.setBoost((float) 1.1);
				document.add(f);
				document.setBoost(1);
			}
		}
		return document;
	}

	private String[] parseKeyword(org.jsoup.nodes.Document jDoc)
			throws SQLException {
		Element temp = jDoc.select("meta[name=keywords]").first();
		if (null != temp)
			return temp.attr("content").split(",");
		else
			return null;
	}

	private String parseTitle(org.jsoup.nodes.Document jDoc) {
		String temp = jDoc.select("title").text();
		return temp;
	}
}