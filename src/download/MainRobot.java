package download;
import index.Index;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import utils.MySQL;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
public class MainRobot {

	private MySQL db = new MySQL();
	private String startUrl = "";
	private MaintainLinks maintainQueue = new MaintainLinks();
	
	public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		String start = "http://www.zol.com.cn/";
		new MainRobot(start);
		ExecutorService executor = Executors.newCachedThreadPool();
		Future<?>[] futures=new Future<?>[10];
		for(int i=0;i<10;i++)
		{
			futures[i]=executor.submit(new Download());
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		executor.shutdown();
		for(Future<?> future:futures)
			try {
				future.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		try {
			Method method=Index.class.getMethod("main", String[].class);
			method.invoke(null,new Object[]{null});
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public MainRobot(String start) {
		this.startUrl = start;
		try {
			downloadFirstPage(startUrl);
			/*new Download().start();
			Thread.sleep(3000);
			new Download().start();
			Thread.sleep(3000);
			new Download().start();
			Thread.sleep(3000);
			new Download().start();
			Thread.sleep(3000);
			new Download().start();
			Thread.sleep(3000);
			new Download().start();
			Thread.sleep(3000);
			new Download().start();*/
			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(startUrl + "下载失败");
		} /*catch (InterruptedException e) {
			e.printStackTrace();
		}*/

	}

	public void downloadFirstPage(String url) throws IOException {
		WrapDownload result = null;

		result = Download.getPage(url);

		insert(result);

		System.out.println("url" + "下载成功，获取链接数："
				+ maintainQueue.addWait(result.getLinkList()));
		maintainQueue.addSuccess(url);

	}

	private void insert(WrapDownload result) {
		String sql = "INSERT INTO html(url,html_text) VALUES(?,?)";

		try {
			PreparedStatement ps = db.conn.prepareStatement(sql);
			ps.setString(1, result.getUrl());
			ps.setString(2, result.getHtml());
			ps.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
