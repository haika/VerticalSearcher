<%@page language="java" contentType="text/html;charset=utf-8"%>
<%@page import="index.Searcher"%>
<%@page import="index.SearchResult"%>
<%@page import="java.util.ArrayList"%>

<%
	String q = new String(request.getParameter("q").getBytes(
			"UTF-8"), "UTF-8");
	String p = request.getParameter("f");
	int pCount = null==p ? 0 : Integer.parseInt(p);
	if (pCount<=0 ) {
		pCount = 0;
	} else {
		pCount -= 1;
	}
	Searcher searcher = new Searcher();
	ArrayList<SearchResult> results = searcher.search(q, 10 * (pCount+1));
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=8">
<title>数码搜索</title>
<meta name="description" content="数码搜索">
<meta name="keywords" content="数码，手机，笔记本，电子产品">
<link rel="stylesheet" media="screen" type="text/css"
	href="./static/style.css">
</head>
<body>
	<div class="wrapper">
		<div class="search-result">
			<div class="header-bar">
				<div class="logo">
					<a href="./"><img width="300"
						height="" src="http://p5.qhimg.com/t01512497e6e7151b1f.png"></a>
				</div>
				<div class="form_box">
					<form action="./search.jsp"
						onsubmit="return document.getElementById(&#39;search_txt_id&#39;).value==&#39;&#39;?false:true;">
						<input id="search_txt_id" class="s_text" type="text" name="q"
							value="<%out.print(q);%>"> <input
							class="s_submit m-button" value="" type="submit">
					</form>
				</div>
			</div>

			<div class="content">
				<div class="result_stats">
					<%
						if (results.size() > 0)
							out.print("找到约" + results.get(0).getHits() + " 条结果 （用时 "
									+ results.get(0).getMs() / 1000.0 + " 秒）");
						else
							out.print("Sorry,没搜到呢");
					%>
				</div>

				<div class="search_result">


					<%
						int limit = 10*(pCount+1) < results.size() ? 10*(pCount+1) : results.size();
						for(int i = pCount*10 ; i< limit ; i++){
							SearchResult sr = results.get(i);
							out.print("<div class=\"s_r\"> "
									+ " <a target=\"_blank\" class=\"title\" " + "href=\""
									+ sr.getUrl() + "\">" + sr.getTitle() + "</a>");
					%>

					<%
						out.print("<div class=\"visible_url\">" + sr.getUrl());
							out.print("</div><div class=\"min_content\">" + sr.getDate()+":"+ sr.getText());
							out.print("</div></div>");

						}
					%>
				</div>
			</div>
		</div>
		<div class="navcnt">
			<table
				style="border-collapse: collapse; text-align: left; margin: 30px auto 30px">
				<tbody>
					<tr valign="top">
						<td class="b navend"><span class="csb gbil"
							style="background: url(/static/nav_logo.png) no-repeat; background-position: -24px 0; background-size: 167px; width: 28px"></span>
						</td>
                        <%
                        int pages = results.size()/10 > 10 ? 10 : results.size()/10 +1;
                        //System.out.println(pages + " " +results.size()/10);
                        for(int index = 1; index <= pages;index++){
                        	%>
                        	 <td class="fl">
                                <a class="fl" href="./search.jsp?q=<%=q%>&f=<%=index%>">
                                    <span class="csb gbil" style="background:url(/static/img/nav_logo.png) no-repeat;background-position:-74px 0;background-size:167px;width:20px"></span><%=index%>
                                </a>
                            </td>
                        	<%
                        }
                        
                        %>
				</tbody>
			</table>
		</div>
	</div>
</body>