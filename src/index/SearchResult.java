package index;

public class SearchResult {

	private String url = "";
	private String title = "";
	private String date = "";
	private String text = "";
	private long ms = 0;
	private int hits = 0;
	private String[] keys ;

	public SearchResult(String url, String title, long ms, int hits,
			String text, String q, String date) {
		this.url = url;
		this.title = title;
		this.hits = hits;
		this.ms = ms;
		this.date = date.substring(0, 10);
		this.text = text;
		while (q.startsWith(" ")) {
			q = q.substring(1);
		}
		while (q.endsWith(" ")) {
			q = q.substring(0, q.length() - 1);
		}
		this.keys = q.split("[\\s]+");//一个或多个空格作为分隔符
	}

	public long getMs() {
		return ms;
	}

	public int getHits() {
		return hits;
	}

	public String getUrl() {
		return url;
	}

	public String getTitle() {
		if (title.length() > 40) {
			title = title.substring(0, 40);
		}
		for (String key : keys) {
			title = title.replace(key, "<b>" + key + "</b>");
		}

		return title;
	}

	public String getDate() {
		return date;
	}
	/**
	 * 截取第一次出现关键词开始的150个字符，并将关键词高亮
	 * @return
	 */
	public String getText() {
		int count = keys.length;
		int[] indexes = new int[count];
		int min = 10000;
		for (int i = 0; i < keys.length; i++) {
			indexes[i] = text.indexOf(keys[i]);
		}

		for (int j = 0; j < keys.length; j++) {
			if (min > indexes[j] & indexes[j] != -1)
				min = indexes[j];
		}
		min = (min == 10000 ? 0 : min);

		if (text.length() - min < 150) {
			text = text.substring(min);
		} else {
			text = text.substring(min, min + 150);
		}
		
		for (String key : keys) {
			text = text.replace(key, "<b>"+key+"</b>");
		}
		
		return "..."+text+"...";
	}
}
