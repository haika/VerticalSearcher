package download;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import utils.MySQL;

/**
 * 用一个哈希表来存储下载过的链接 并进行与链接是否应该被下载的处理
 * 
 * @author 
 * 
 */
public class MaintainLinks {

	private static HashSet<String> downloaded = new HashSet<String>();
	private static HashSet<String> failed = new HashSet<String>();
	private static List<String> wait = Collections
			.synchronizedList(new ArrayList<String>());

	/**
	 * 判断一个链接是否已经被下载过
	 * 
	 * @param url
	 * @return
	 */
	public synchronized boolean hasDownloaded(String url) {
		boolean temp = downloaded.contains(url);
		return temp;
	}

	// public synchronized ArrayList<String> getWaitQueue() {
	// return wait;
	// }

	public synchronized int downloadSize() {
		return downloaded.size();
	}

	/**
	 * 从待下载队列里取30个链接
	 * 
	 * @return
	 */
	public synchronized ArrayList<String> getOptWait() {
		ArrayList<String> temp = new ArrayList<String>();
		if (wait.size() < 30) {
			temp.addAll(wait);
		} else {
			temp.addAll(wait.subList(0, 29));
		}
		wait.removeAll(temp);
		return temp;
	}

	//
	// public HashSet<String> getDownloaded() {
	// return downloaded;
	// }

	/**
	 * 将下载失败的url添加到下载失败的队列
	 * 
	 * @param url
	 * @return
	 */
	public synchronized int addFailed(String url) {
		failed.add(url);
		return failed.size();
	}

	public synchronized int waitSize() {
		return wait.size();
	}

	public synchronized boolean isWaitEmpty() {
		return wait.isEmpty();
	}

	public synchronized int addWait(String url) {
		wait.add(url);
		return wait.size();
	}

	public synchronized int addWait(ArrayList<String> urls) {
		wait.addAll(urls);
		return wait.size();
	}

	public synchronized int addSuccess(String url) {
		downloaded.add(url);
		return wait.size();
	}

	/**
	 * 添加一个下载成功的链接，返回当前下载过的链接数
	 * 
	 * @param urls
	 * @return
	 */
	public synchronized int addSuccess(HashSet<String> urls) {
		downloaded.addAll(urls);
		return wait.size();
	}

	/**
	 * 持久化存储下载失败和已经下载的url
	 */
	public synchronized void persistence() {
		PreparedStatement ps = null;
		MySQL db = new MySQL();
		int count = 0;
		String sql = "INSERT INTO queue(type,url) VALUES (?,?)";
		try {
			db.conn.setAutoCommit(false);
			ps = db.conn.prepareStatement(sql);

			for (String s : failed) {
				ps.setString(1, "fail");
				ps.setString(2, s);
				ps.addBatch();
				count++;
				if (count % 10 == 0) {
					db.conn.commit();
				}
			}
			failed.clear();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			db.conn.setAutoCommit(false);
			db.conn.prepareStatement(sql);

			for (String s : wait) {
				ps.setString(1, "wait");
				ps.setString(2, s);
				ps.addBatch();
				count++;
				if (count % 10 == 0) {
					db.conn.commit();
				}
			}
			db.conn.commit();
			wait.clear();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}
