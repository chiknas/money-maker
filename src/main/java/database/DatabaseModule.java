package database;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.alfasoftware.morf.guicesupport.MorfModule;
import org.alfasoftware.morf.jdbc.ConnectionResourcesBean;
import org.alfasoftware.morf.jdbc.DatabaseDataSetProducer;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.upgrade.Deployment;
import org.alfasoftware.morf.upgrade.UpgradeStep;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DatabaseModule extends MorfModule {

    private static final ThreadLocal<EntityManager> ENTITY_MANAGER_CACHE = new ThreadLocal<>();

    private final String DB_HOST_NAME = System.getenv("DB_HOST_NAME");
    private final String DB_PORT = System.getenv("DB_PORT");
    private final String DB_USER_NAME = System.getenv("DB_USER_NAME");
    private final String DB_USER_PASSWORD = System.getenv("DB_USER_PASSWORD");

    @Provides
    @Singleton
    public ConnectionResourcesBean provideDatabaseConnectionDetails() {
        ConnectionResourcesBean connectionResources = new ConnectionResourcesBean();
        connectionResources.setDatabaseType("PGSQL");
        connectionResources.setDatabaseName("postgres");
        connectionResources.setSchemaName("public");
        connectionResources.setHostName(DB_HOST_NAME);
        connectionResources.setUserName(DB_USER_NAME);
        connectionResources.setPassword(DB_USER_PASSWORD);
        connectionResources.setPort(Integer.parseInt(DB_PORT));
        return connectionResources;
    }

    @Provides
    @Singleton
    public EntityManagerFactory provideEntityManagerFactory() {
        ConnectionResourcesBean connectionResourcesBean = provideDatabaseConnectionDetails();

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        properties.put("hibernate.connection.url", connectionResourcesBean.getJdbcUrl());
        properties.put("hibernate.connection.username", connectionResourcesBean.getUserName());
        properties.put("hibernate.connection.password", connectionResourcesBean.getPassword());
        properties.put("hibernate.connection.pool_size", "1");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "validate");
        return Persistence.createEntityManagerFactory("db-manager", properties);
    }

    @Provides
    public EntityManager provideEntityManager(EntityManagerFactory entityManagerFactory) {
        EntityManager entityManager = ENTITY_MANAGER_CACHE.get();
        if (entityManager == null) {
            ENTITY_MANAGER_CACHE.set(entityManager = entityManagerFactory.createEntityManager());
        }
        return entityManager;
    }

    @Override
    protected void configure() {
        super.configure();
        // Define the target database schema
        Schema targetSchema = new MoneyMakerDatabaseSchema();

        // No initial upgrades
        Collection<Class<? extends UpgradeStep>> upgradeSteps = new HashSet<>();

        ConnectionResourcesBean connectionResources = provideDatabaseConnectionDetails();

        // Deploy the schema
        if (isCurrentDatabaseEmpty(connectionResources)) {
            Deployment.deploySchema(targetSchema, upgradeSteps, connectionResources);
        } else {
            // TODO: fix NoUpgradePathExistsException: No upgrade path exists
            // Upgrade.performUpgrade(targetSchema, upgradeSteps, connectionResources, new ViewDeploymentValidator.AlwaysValidate());
        }
    }

    private boolean isCurrentDatabaseEmpty(ConnectionResourcesBean connectionResources) {
        DatabaseDataSetProducer databaseDataSetProducer = new DatabaseDataSetProducer(connectionResources);
        databaseDataSetProducer.open();
        boolean isEmptyDatabase = databaseDataSetProducer.getSchema().isEmptyDatabase();
        databaseDataSetProducer.close();
        return isEmptyDatabase;
    }
}
