package utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class DiyAnalyzer extends Analyzer implements Closeable {

	public static void main(String[] args) {
		DiyAnalyzer analyzer = DiyAnalyzer.getAnalyzer();
		System.out.print(CLibrary.Instance.NLPIR_ParagraphProcess("我正在使用微信的新版",
				0));
		analyzer.close();
	}

	public static DiyAnalyzer getAnalyzer() {
		return new DiyAnalyzer();
	}

	public void close() {
		CLibrary.Instance.NLPIR_Exit();
	}

	public DiyAnalyzer() {
		stop = loadStopword();
		//System.out.println(Arrays.toString(stop));
		int init_flag = CLibrary.Instance.NLPIR_Init(Config.dataDir, 1, "0");
		if (0 == init_flag) {
			System.err.println("初始化失败！");
			System.exit(0);
		} else {
			System.out.println("analyzer init success");
		}
	}
	public String fragment(String q){
		q=q.replaceAll("\\s.+\\s", "");
		return CLibrary.Instance.NLPIR_ParagraphProcess(q, 0);
	}
	
	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		String outStr = "";
		try {
			String inStr = readerToString(reader);
			outStr = CLibrary.Instance.NLPIR_ParagraphProcess(inStr, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// System.out.println(outStr);

		TokenStream result = new DiyTokenizier(new StringReader(outStr));
		result = new StopFilter(true, result, StopFilter.makeStopSet(stop));
		result = new LowerCaseFilter(result);
		return result;
	}

	private String[] stop ;

	public String readerToString(Reader reader) throws IOException {
		BufferedReader br = new BufferedReader(reader);
		String ttt = null;
		String tttt = "";
		while ((ttt = br.readLine()) != null) {
			tttt += ttt;
		}
		return tttt;
	}
	
	public String[] loadStopword(){
		File f = new File(Config.stopWordPath);
		StringBuilder log = new StringBuilder();
		try {
			BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(f),"gbk"));
			
			String temp = "";
			while((temp =reader.readLine())!=null){
				log.append(temp);
				log.append(" ");
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return log.toString().split(" ");
	}

	// 定义接口CLibrary，继承自com.sun.jna.Library
	public interface CLibrary extends Library {
		// 定义并初始化接口的静态变量
		CLibrary Instance = (CLibrary) Native.loadLibrary(Config.dllPath,
				CLibrary.class);

		public int NLPIR_Init(String sDataPath, int encoding,
				String sLicenceCode);

		public String NLPIR_ParagraphProcess(String sSrc, int bPOSTagged);

		public String NLPIR_GetKeyWords(String sLine, int nMaxKeyLimit,
				boolean bWeightOut);

		public int NLPIR_AddUserWord(String sWord);

		public int NLPIR_DelUsrWord(String sWord);

		public void NLPIR_Exit();
	}

}
