package org.jboss.as.quickstarts.xa;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@Path("/xaservice")
public class XAService {

  private final static Logger LOGGER = Logger.getLogger(XAService.class.getName());

  @PersistenceContext(unitName = "postgresqlA")
  private EntityManager postgresqlA;

  @PersistenceContext(unitName = "postgresqlB")
  private EntityManager postgresqlB;

  @Inject
  private UserTransaction userTransaction;

  @Resource(lookup = "java:/TransactionManager")
  TransactionManager tm;

  private <T> T executeInTransaction(Callable<T> c) {
    try {
      userTransaction.begin();
      T result = c.call();
      userTransaction.commit();
      return result;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Exception when writing to database", e);
      throw new IllegalStateException("Database operation failed");
    }
  }

  private String updateDatabases(final String name, final Integer value) {
    try {
      userTransaction.begin();


		Query qa = postgresqlA.createNativeQuery("update Account set value = value + ? where name = ?");
		qa.setParameter(1, value);
		qa.setParameter(2, name);
		qa.executeUpdate();

/*
      Account accountA = postgresqlA.getReference(Account.class, name);
      accountA.setValue(accountA.getValue() + value);
      postgresqlA.merge(accountA);
*/

     /*
      Account accountB = postgresqlB.getReference(Account.class, name);
      accountB.setValue(accountB.getValue() - value);
      postgresqlA.merge(accountA);
      */

		Query qb = postgresqlB.createNativeQuery("update Account set value = value - ? where name = ?");
		qb.setParameter(1, value);
		qb.setParameter(2, name);
		qb.executeUpdate();

      userTransaction.commit();
      return "OK: " + userTransaction.getStatus();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Exception when writing to database", e);
      StringWriter sw = new StringWriter();
      PrintWriter stackLogger = new PrintWriter(sw);
      int txStatus = -1;
      try {
        txStatus = userTransaction.getStatus();
      } catch (SystemException etx) {
        LOGGER.log(Level.SEVERE, "Exception when reading TX status", e);
        etx.printStackTrace(stackLogger);
        stackLogger.println("\n=======================================");
      }
      e.printStackTrace(stackLogger);
      return "FAIL: " + txStatus + "\n" + sw.toString();
    }
  }

  private String insertSomeValues(Integer values) {
    try {
      userTransaction.begin();
      for (int i = 0; i < values; i++) {
        postgresqlA.persist(new Account("account" + i, 10000));
        postgresqlB.persist(new Account("account" + i, 10000));
      }
      userTransaction.commit();
      return "OK " + userTransaction.getStatus();
    } catch (Exception e) {
      int txStatus = -1;
      try {
        txStatus = userTransaction.getStatus();
      } catch (SystemException e1) {
        e1.printStackTrace();
      }
      return "FAIL: " + txStatus;
    }

  }

  @Path("insert/{n-values}")
  @GET
  public String insert(@PathParam("n-values") final Integer value) {
    return insertSomeValues(value);
  }

  @Path("update/{accountName}/{value}")
  @GET
  public String update(@PathParam("accountName") final String accountName, @PathParam("value") final Integer value) {
    return updateDatabases(accountName, value);
  }

  @Path("ping")
  @GET
  public String ping() {
    return "PONG";
  }

  private Integer listDatabase(final EntityManager em, String accountName) {
    return executeInTransaction(() -> {
      return em.createQuery("select a.value from Account a where a.name = :name",
              Integer.class).setParameter("name", accountName).getSingleResult();
    });
  }

  @GET
  @Path("get/{accountName}")
  public Integer getCurrentValueForAccount(@QueryParam("db") final String type, @PathParam("accountName") String accountName) {
    switch (type) {
      case "postgresqlA":
        return listDatabase(postgresqlA, accountName);
      case "postgresqlB":
        return listDatabase(postgresqlB, accountName);
      default:
        throw new IllegalArgumentException("Unknown database");
    }
  }

}
