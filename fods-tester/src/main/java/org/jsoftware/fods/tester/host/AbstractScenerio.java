package org.jsoftware.fods.tester.host;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;


public abstract class AbstractScenerio implements TestScenerio {
	protected TesterConfig testerConfig;
	protected DataSource dataSource;
	
	@Override
	public void init(TesterConfig tc, DataSource ds) {
		this.dataSource = ds;
		this.testerConfig = tc;
	}
	
	@Override
	public void before() throws SQLException, IOException {
		executeScript(getClass().getSimpleName() + "-before.sql");
	}


	private void executeScript(String sc) throws SQLException, IOException {
		InputStream ins = getClass().getResourceAsStream(sc);
		if (ins != null) {
			System.out.println("Executing " + sc + "...");
			BufferedReader br = new BufferedReader(new InputStreamReader(ins));
			String str;
			Connection con = dataSource.getConnection();
			Statement stm = con.createStatement();
			while((str = br.readLine()) != null) {
				System.out.println("\t" + str);
				stm.execute(str);
			}
			stm.close();
			con.close();
		}
	}

	@Override
	public void after() throws SQLException, IOException {
		executeScript(getClass().getSimpleName() + "-after.sql");
	}
	
	

}
