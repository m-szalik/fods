Failover DataSource

Url:
http://wiki.eo.pl/people:marek.gruszecki:fods


Tomcat:
<Resource
     name="jdbc/myDS"
     auth="Container"
     type="javax.sql.DataSource"
     factory="pl.eo.apps.bossa.fods.FODataSourceFactory"
     backTime="60"	   // optional, default "60"
     testSql="SELECT 1+2"  // optional, default "SELECT 1+2"
     jndiDataSources="jdbc/ds0,jdbc/ds1,jdbc/ds2"
     debug="true"  // optional, default "false"
     logFile="/var/log/ds.log" // optional if not set stdout
     jmx="true" // optional, default "true"
/>


										 