Failover DataSource
Website: http://jsoftware.org/fods


Tomcat: (do not use - wrong)

<Resource
     name="jdbc/myDS"
     auth="Container"
     type="javax.sql.DataSource"
     factory="xxxx.FODataSourceFactory"
     backTime="60"	   // optional, default "60"
     testSql="SELECT 1+2"  // optional, default "SELECT 1+2"
     jndiDataSources="jdbc/ds0,jdbc/ds1,jdbc/ds2"
     debug="true"  // optional, default "false"
     logFile="/var/log/ds.log" // optional if not set stdout
     jmx="true" // optional, default "true"
/>

