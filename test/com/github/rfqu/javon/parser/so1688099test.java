package com.github.rfqu.javon.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.github.rfqu.javon.builder.JavonBuilder;

public class so1688099test {
	static final String inp=
"Data ("+
"    title: 'ComputingandInformationsystems',"+
"    id: 1,"+
"    children: true,"+
"    groups: [Data("+
"        title: 'LeveloneCIS',"+
"        id: 2,"+
"        children: true,"+
"        groups: [Data ("+
"            title: 'IntroToComputingandInternet',"+
"            id: 3,"+
"            children: false,"+
"            groups: []"+
"        )]"+
"    )]"+
")";			
	
static final String exp="title:ComputingandInformationsystems,id:1,children:true,"+
"groups:[title:LeveloneCIS,id:2,children:true,"+
"groups:[title:IntroToComputingandInternet,id:3,children:false,groups:[]]]";
	
	@Test
    public void test() throws IOException, Exception {
//		InputStream r = getClass().getResourceAsStream("so1688099.json");
        JavonParser mp=new JavonParser(inp);
        JavonBuilder bd = new JavonBuilder();
        bd.put("Data", Data.class);
        Object obj = mp.parseWith(bd);
		String res = obj.toString();
        assertEquals(exp, res);
    }

	
	public static class Data {
		private String title;
		private int id;
		private Boolean children;
		private List<Data> groups;

		public String getTitle() {
			return title;
		}

		public int getId() {
			return id;
		}

		public Boolean getChildren() {
			return children;
		}

		public List<Data> getGroups() {
			return groups;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void setId(int id) {
			this.id = id;
		}

		public void setChildren(Boolean children) {
			this.children = children;
		}

		public void setGroups(List<Data> groups) {
			this.groups = groups;
		}

		public String toString() {
			return String.format("title:%s,id:%d,children:%s,groups:%s", title,
					id, children, groups);
		}
	}
}
