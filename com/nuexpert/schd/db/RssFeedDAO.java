package com.nuexpert.schd.db;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.commons.lang3.StringUtils;

import com.nuexpert.schd.feed.Feed;

@SuppressWarnings("serial")
@ManagedBean
@ApplicationScoped
public class RssFeedDAO implements Serializable{
	private final static String QUERY_SQL="select * from rss_feed where active=1 and language=?";
	
	public List<Feed> getFeedList(String language) throws Exception{
		if (StringUtils.isBlank(language)){
			language="en";
		}

		
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps =  conn.prepareStatement(QUERY_SQL); 
		ps.setString(1, language);
		
		ResultSet rs =  ps.executeQuery();
		List<Feed> resultList = new ArrayList<Feed>();
		
		while(rs.next()){
			Feed feed = new Feed(rs.getString("title"),rs.getString("link"),rs.getString("description"),rs.getString("language"),null,rs.getDate("last_update"));			
			feed.setCategory(rs.getString("category"));
			resultList.add(feed);
		}
		
		rs.close();
		ps.close();
		conn.close();
		
		return resultList;
	}

}
