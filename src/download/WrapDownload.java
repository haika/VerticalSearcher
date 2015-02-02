package download;

import java.util.ArrayList;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 封装下载网页的返回对象；包括一个网页的html源代码和该网页包含的所有链接
 * @author 
 *
 */
public class WrapDownload {
	private ArrayList<String> linkList;
	private String html;
	private String url;
	private String title;
	private String key_words;

	public WrapDownload(ArrayList<String> linkList, String html, String url) {
		this.linkList = linkList;
		this.html = html;
		this.url = url;
		
	}
	
	public WrapDownload(Document doc,String url){
		this.linkList = getAllLinks(doc);
		this.html = getText(doc);
		this.url = url;
		this.title = doc.select("title").first().text();
		this.key_words = doc.select("META").attr("key_word");
	}

	public ArrayList<String> getLinkList() {
		return linkList;
	}

	public String getHtml() {
		return html;
	}

	public String getUrl() {
		return url;
	}
	
	/**
	 * 获取一个document对象中所有链接
	 * 
	 * @param doc
	 * @return 链接列表
	 */
	public static ArrayList<String> getAllLinks(Document doc) {
		ArrayList<String> linkList = new ArrayList<String>();

		Elements elements = doc.select("a[href]");
		for (Element e : elements) {
			String attr = e.attr("href");
			if (attr.startsWith(".")) {
				linkList.add(e.baseUri() + e.attr("href").substring(1));
				// System.out.println(e.baseUri() +
				// e.attr("href").substring(1));
			} else if (attr.startsWith("http")) {
				linkList.add(e.attr("href"));
				// System.out.println(e.attr("href"));
			}
		}
		return linkList;
	}

	public static String getText(Document doc) {
		return doc.html();
	}

}
