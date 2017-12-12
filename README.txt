
oc create -f openshift.yaml
oc process xa-load IMAGE=<eap64-image> | oc create -f -

oc start-build xa-load

oc expose service xa-load

# in the following script, replace the URL with your real route hostname...

# wait for the pods to be ready

curl http://xa-load-maschmid.apps.devel.xpaas/rest/xaservice/ping

# create 32 accounts
curl http://xa-load-maschmid.apps.devel.xpaas/rest/xaservice/insert/32


touch runtest
for i in `seq 0 31`
do
	(while [ -f runtest ]; do curl http://xa-load-maschmid.apps.devel.xpaas/rest/xaservice/update/account$i/1; done) > /dev/null 2>&1 &
done

# scale down
oc scale dc xa-load --replicas=2

# wait for the recovery to start

# notice the recovery pod keeps waiting

>Tue Dec 12 09:29:36 UTC 2017: Waiting for the following transactions: 0:ffff0a8301d8:-5076181:5a2f9da9:cc3
0:ffff0a8301d8:-5076181:5a2f9da9:ccf
0:ffff0a8301d8:-5076181:5a2f9da9:cd6
0:ffff0a8301d8:-5076181:5a2f9da9:ce7
0:ffff0a8301d8:-5076181:5a2f9da9:cee
0:ffff0a8301d8:-5076181:5a2f9da9:cff
0:ffff0a8301d8:-5076181:5a2f9da9:d02
0:ffff0a8301d8:-5076181:5a2f9da9:d17
0:ffff0a8301d8:-5076181:5a2f9da9:d23
0:ffff0a8301d8:-5076181:5a2f9da9:d2c
0:ffff0a8301d8:-5076181:5a2f9da9:d2f

# notice the application pods starts to spit timeout errors

09:30:04,856 ERROR [org.jboss.as.txn] (http-0.0.0.0:8080-33) JBAS010152: APPLICATION ERROR: transaction still active in request with status 1
09:30:04,870 WARN  [org.hibernate.engine.jdbc.spi.SqlExceptionHelper] (http-0.0.0.0:8080-32) SQL Error: 0, SQLState: null
09:30:04,871 ERROR [org.hibernate.engine.jdbc.spi.SqlExceptionHelper] (http-0.0.0.0:8080-32) javax.resource.ResourceException: IJ000453: Unable to get managed connection for java:jboss/datasources/testdba_postgresql
09:30:04,871 SEVERE [org.jboss.as.quickstarts.xa.XAService] (http-0.0.0.0:8080-32) Exception when writing to database: javax.persistence.PersistenceException: org.hibernate.exception.GenericJDBCException: Could not open connection
	at org.hibernate.ejb.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1387) [hibernate-entitymanager-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.ejb.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1310) [hibernate-entitymanager-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.ejb.AbstractEntityManagerImpl.throwPersistenceException(AbstractEntityManagerImpl.java:1397) [hibernate-entitymanager-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.ejb.AbstractQueryImpl.executeUpdate(AbstractQueryImpl.java:111) [hibernate-entitymanager-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.jboss.as.quickstarts.xa.XAService.updateDatabases(XAService.java:60) [classes:]
	at org.jboss.as.quickstarts.xa.XAService.update(XAService.java:128) [classes:]
	at org.jboss.as.quickstarts.xa.XAService$Proxy$_$$_WeldClientProxy.update(XAService$Proxy$_$$_WeldClientProxy.java) [classes:]
	at sun.reflect.GeneratedMethodAccessor36.invoke(Unknown Source) [:1.8.0_151]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) [rt.jar:1.8.0_151]
	at java.lang.reflect.Method.invoke(Method.java:498) [rt.jar:1.8.0_151]
	at org.jboss.resteasy.core.MethodInjectorImpl.invoke(MethodInjectorImpl.java:168) [resteasy-jaxrs-2.3.20.Final-redhat-1.jar:]
	at org.jboss.resteasy.core.ResourceMethod.invokeOnTarget(ResourceMethod.java:269) [resteasy-jaxrs-2.3.20.Final-redhat-1.jar:]
	at org.jboss.resteasy.core.ResourceMethod.invoke(ResourceMethod.java:227) [resteasy-jaxrs-2.3.20.Final-redhat-1.jar:]
	at org.jboss.resteasy.core.ResourceMethod.invoke(ResourceMethod.java:216) [resteasy-jaxrs-2.3.20.Final-redhat-1.jar:]
	at org.jboss.resteasy.core.SynchronousDispatcher.getResponse(SynchronousDispatcher.java:583) [resteasy-jaxrs-2.3.20.Final-redhat-1.jar:]
	at org.jboss.resteasy.core.SynchronousDispatcher.invoke(SynchronousDispatcher.java:565) [resteasy-jaxrs-2.3.20.Final-redhat-1.jar:]
	at org.jboss.resteasy.core.SynchronousDispatcher.invoke(SynchronousDispatcher.java:130) [resteasy-jaxrs-2.3.20.Final-redhat-1.jar:]
	at org.jboss.resteasy.plugins.server.servlet.ServletContainerDispatcher.service(ServletContainerDispatcher.java:208) [resteasy-jaxrs-2.3.20.Final-redhat-1.jar:]
	at org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher.service(HttpServletDispatcher.java:55) [resteasy-jaxrs-2.3.20.Final-redhat-1.jar:]
	at org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher.service(HttpServletDispatcher.java:50) [resteasy-jaxrs-2.3.20.Final-redhat-1.jar:]
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:847) [jboss-servlet-api_3.0_spec-1.0.2.Final-redhat-2.jar:1.0.2.Final-redhat-2]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:295) [jbossweb-7.5.24.Final-redhat-1.jar:7.5.24.Final-redhat-1]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:214) [jbossweb-7.5.24.Final-redhat-1.jar:7.5.24.Final-redhat-1]
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:231) [jbossweb-7.5.24.Final-redhat-1.jar:7.5.24.Final-redhat-1]
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:149) [jbossweb-7.5.24.Final-redhat-1.jar:7.5.24.Final-redhat-1]
	at org.jboss.as.jpa.interceptor.WebNonTxEmCloserValve.invoke(WebNonTxEmCloserValve.java:50) [jboss-as-jpa-7.5.17.Final-redhat-4.jar:7.5.17.Final-redhat-4]
	at org.jboss.as.jpa.interceptor.WebNonTxEmCloserValve.invoke(WebNonTxEmCloserValve.java:50) [jboss-as-jpa-7.5.17.Final-redhat-4.jar:7.5.17.Final-redhat-4]
	at org.jboss.as.web.security.SecurityContextAssociationValve.invoke(SecurityContextAssociationValve.java:169) [jboss-as-web-7.5.17.Final-redhat-4.jar:7.5.17.Final-redhat-4]
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:151) [jbossweb-7.5.24.Final-redhat-1.jar:7.5.24.Final-redhat-1]
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:97) [jbossweb-7.5.24.Final-redhat-1.jar:7.5.24.Final-redhat-1]
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:102) [jbossweb-7.5.24.Final-redhat-1.jar:7.5.24.Final-redhat-1]
	at org.apache.catalina.valves.RemoteIpValve.invoke(RemoteIpValve.java:621) [jbossweb-7.5.24.Final-redhat-1.jar:7.5.24.Final-redhat-1]
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:343) [jbossweb-7.5.24.Final-redhat-1.jar:7.5.24.Final-redhat-1]
	at org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:856) [jbossweb-7.5.24.Final-redhat-1.jar:7.5.24.Final-redhat-1]
	at org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler.process(Http11Protocol.java:656) [jbossweb-7.5.24.Final-redhat-1.jar:7.5.24.Final-redhat-1]
	at org.apache.tomcat.util.net.JIoEndpoint$Worker.run(JIoEndpoint.java:926) [jbossweb-7.5.24.Final-redhat-1.jar:7.5.24.Final-redhat-1]
	at java.lang.Thread.run(Thread.java:748) [rt.jar:1.8.0_151]
Caused by: org.hibernate.exception.GenericJDBCException: Could not open connection
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:54) [hibernate-core-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:124) [hibernate-core-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:109) [hibernate-core-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.engine.jdbc.internal.LogicalConnectionImpl.obtainConnection(LogicalConnectionImpl.java:221) [hibernate-core-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.engine.jdbc.internal.LogicalConnectionImpl.getConnection(LogicalConnectionImpl.java:157) [hibernate-core-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.engine.jdbc.internal.StatementPreparerImpl.connection(StatementPreparerImpl.java:56) [hibernate-core-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.engine.jdbc.internal.StatementPreparerImpl$1.doPrepare(StatementPreparerImpl.java:96) [hibernate-core-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.engine.jdbc.internal.StatementPreparerImpl$StatementPreparationTemplate.prepareStatement(StatementPreparerImpl.java:183) [hibernate-core-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.engine.jdbc.internal.StatementPreparerImpl.prepareStatement(StatementPreparerImpl.java:89) [hibernate-core-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.engine.query.spi.NativeSQLQueryPlan.performExecuteUpdate(NativeSQLQueryPlan.java:196) [hibernate-core-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.internal.SessionImpl.executeNativeUpdate(SessionImpl.java:1274) [hibernate-core-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.internal.SQLQueryImpl.executeUpdate(SQLQueryImpl.java:401) [hibernate-core-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.ejb.QueryImpl.internalExecuteUpdate(QueryImpl.java:198) [hibernate-entitymanager-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.ejb.AbstractQueryImpl.executeUpdate(AbstractQueryImpl.java:102) [hibernate-entitymanager-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	... 33 more
Caused by: java.sql.SQLException: javax.resource.ResourceException: IJ000453: Unable to get managed connection for java:jboss/datasources/testdba_postgresql
	at org.jboss.jca.adapters.jdbc.WrapperDataSource.getConnection(WrapperDataSource.java:151)
	at org.jboss.as.connector.subsystems.datasources.WildFlyDataSource.getConnection(WildFlyDataSource.java:69)
	at org.hibernate.ejb.connection.InjectedDataSourceConnectionProvider.getConnection(InjectedDataSourceConnectionProvider.java:70) [hibernate-entitymanager-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.internal.AbstractSessionImpl$NonContextualJdbcConnectionAccess.obtainConnection(AbstractSessionImpl.java:301) [hibernate-core-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	at org.hibernate.engine.jdbc.internal.LogicalConnectionImpl.obtainConnection(LogicalConnectionImpl.java:214) [hibernate-core-4.2.27.Final-redhat-1.jar:4.2.27.Final-redhat-1]
	... 43 more
Caused by: javax.resource.ResourceException: IJ000453: Unable to get managed connection for java:jboss/datasources/testdba_postgresql
	at org.jboss.jca.core.connectionmanager.AbstractConnectionManager.getManagedConnection(AbstractConnectionManager.java:410)
	at org.jboss.jca.core.connectionmanager.tx.TxConnectionManagerImpl.getManagedConnection(TxConnectionManagerImpl.java:367)
	at org.jboss.jca.core.connectionmanager.AbstractConnectionManager.allocateConnection(AbstractConnectionManager.java:499)
	at org.jboss.jca.adapters.jdbc.WrapperDataSource.getConnection(WrapperDataSource.java:143)
	... 47 more
Caused by: javax.resource.ResourceException: IJ000655: No managed connections available within configured blocking timeout (30000 [ms])
	at org.jboss.jca.core.connectionmanager.pool.mcp.SemaphoreArrayListManagedConnectionPool.getConnection(SemaphoreArrayListManagedConnectionPool.java:460)
	at org.jboss.jca.core.connectionmanager.pool.AbstractPool.getTransactionNewConnection(AbstractPool.java:535)
	at org.jboss.jca.core.connectionmanager.pool.AbstractPool.getConnection(AbstractPool.java:438)
	at org.jboss.jca.core.connectionmanager.AbstractConnectionManager.getManagedConnection(AbstractConnectionManager.java:344)
	... 50 more





# you can now end the curls with
rm runtest

