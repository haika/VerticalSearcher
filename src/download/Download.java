package download;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utils.MySQL;

public class Download extends Thread {

	private MaintainLinks maintainQueue = new MaintainLinks();
	private Db db = new Db();
	private boolean full = false;
	private static final int expextedLinks = 150000;
	private static String ua = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36";

	public static void main(String[] args) {
		String start = "http://www.zol.com.cn/";
		Document doc = null;
		try {
			Response res = Jsoup.connect(start).response();
			// res.header("");
			doc = res.parse();
		} catch (IOException e) {
			e.printStackTrace();
		}
		getAllLinks(doc);
	}

	@Override
	public void run() {
		while (!maintainQueue.isWaitEmpty()) {

			maintainQueue.addWait(cachedWait);
			cachedWait.clear();
			ArrayList<String> currentQueue = maintainQueue.getOptWait();

			for (String url : currentQueue) {
				if (!maintainQueue.hasDownloaded(url)) {
					download(url);
				}
			}
			System.out.println("线程:" + Thread.currentThread().getId()
					+ ";当前队列链接数：" + maintainQueue.waitSize() + "已下载:"
					+ maintainQueue.downloadSize());
			//
			if (!full) {
				full = maintainQueue.waitSize() > expextedLinks;
			}
		}
		db.commit();
		db.finalise();
		maintainQueue.persistence();
	}

	// private HashSet<String> cachedSuccess = new HashSet<String>();
	private ArrayList<String> cachedWait = new ArrayList<String>();

	/**
	 * 下载url内容，并将url内的链接添加到缓冲队列中
	 * 
	 * @param url
	 */
	public void download(String url) {
		WrapDownload result = null;

		try {
			result = Download.getPage(url);
			// db.addBatch(result);
			db.addBatch(result);

			// 下载队列里面只保存前100w个网页
			if (!full) {
				cachedWait.addAll(result.getLinkList());
			}
			maintainQueue.addSuccess(url);

		} catch (IOException e) {
			// e.printStackTrace();
			maintainQueue.addFailed(url);
			System.err.println(url + "下载失败");
		}
	}

	/**
	 * 下载指定url的网页内容
	 * 
	 * @param url
	 * @return 包含一个网页的内容和该网页链接的对象
	 * @throws IOException
	 */
	public static WrapDownload getPage(String url) throws IOException {
		Document doc = null;

		doc = Jsoup.connect(url).userAgent(ua).timeout(10000).get();

		return new WrapDownload(getAllLinks(doc), getText(doc), url);
	}

	/**
	 * 获取一个document对象中所有链接
	 * 
	 * @param doc
	 * @return 链接列表
	 */
	public static ArrayList<String> getAllLinks(Document doc) {
		ArrayList<String> linkList = new ArrayList<String>();
		String url = "";
		int temp = 0;
		Elements elements = doc.select("a[href]");
		for (Element e : elements) {
			String attr = e.attr("href");
			if (attr.startsWith(".")) {
				url = e.baseUri() + e.attr("href").substring(1);
				url = url.replace("//", "/").replace("http:/", "http://");
				if((temp=url.indexOf("#"))!=-1){
					url = url.substring(0,temp);
				}
				if(url.endsWith("/")){
					url = url.substring(0,url.length()-1);
				}
						
				linkList.add(url);
				
			} else if (attr.startsWith("http")) {
				url = e.attr("href");
				if((temp=url.indexOf("#"))!=-1){
					url = url.substring(0,temp);
				}
				if(url.endsWith("/")){
					url = url.substring(0,url.length()-1);
				}
				linkList.add(url);
			}
		}
		return linkList;
	}

	public static String getText(Document doc) {
		return doc.html();
	}

	public class Db {

		public PreparedStatement ps = null;
		public int count = 0;
		private int fail = 0;
		private MySQL mysql;

		public Db() {
			String sql = "INSERT INTO html(url,html_text) VALUES(?,?)";
			mysql = new MySQL();
			try {
				mysql.conn.setAutoCommit(false);
				ps = mysql.conn.prepareStatement(sql);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		public void addBatch(WrapDownload data) {
			try {
				ps.setString(1, data.getUrl());
				ps.setString(2, data.getHtml());
				ps.addBatch();
				count++;
				if (count % 20 == 0) {
					ps.executeBatch();
					mysql.conn.commit();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println(data.getUrl() + "存储失败" + (++fail));
			}
		}

		/**
		 * 强制提交batch中的缓存数据
		 */
		public void commit() {
			try {
				ps.executeBatch();
				mysql.conn.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		public void finalise() {
			try {
				mysql.conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println("共失败" + fail);
		}

		public void insert(WrapDownload data) {
			String sql = "INSERT INTO html(url,html_text) VALUES(?,?)";

			try {
				PreparedStatement ps = mysql.conn.prepareStatement(sql);
				ps.setString(1, data.getUrl());
				ps.setString(2, data.getHtml());
				ps.executeBatch();
			} catch (SQLException e) {
				System.out.println(data.getUrl());
				e.printStackTrace();
			}

		}

	}

}
