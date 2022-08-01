package database;

import com.google.inject.Provides;
import org.alfasoftware.morf.guicesupport.MorfModule;
import org.alfasoftware.morf.jdbc.ConnectionResourcesBean;
import org.alfasoftware.morf.jdbc.DatabaseDataSetProducer;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.upgrade.Deployment;
import org.alfasoftware.morf.upgrade.UpgradeStep;

import java.util.Collection;
import java.util.HashSet;

public class DatabaseModule extends MorfModule {

    private final String HOST_NAME = System.getenv("HOST_NAME");
    private final String PORT = System.getenv("PORT");
    private final String USER_NAME = System.getenv("USER_NAME");
    private final String USER_PASSWORD = System.getenv("USER_PASSWORD");

    @Provides
    public ConnectionResourcesBean provideDatabaseConnectionDetails() {
        ConnectionResourcesBean connectionResources = new ConnectionResourcesBean();
        connectionResources.setDatabaseType("PGSQL");
        connectionResources.setDatabaseName("postgres");
        connectionResources.setSchemaName("public");
        connectionResources.setHostName(HOST_NAME);
        connectionResources.setUserName(USER_NAME);
        connectionResources.setPassword(USER_PASSWORD);
        connectionResources.setPort(Integer.parseInt(PORT));
        return connectionResources;
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
