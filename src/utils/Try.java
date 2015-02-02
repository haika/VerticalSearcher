package utils;


public class Try {

	public static void main(String[] arg){
		String q = " 武汉理工大学  ";
		while(q.startsWith(" ")){
			q=q.substring(1);
		}
		while(q.endsWith(" ")){
			q=q.substring(0,q.length()-1);
		}
		System.out.println(q.substring(0,0));
	}
}
