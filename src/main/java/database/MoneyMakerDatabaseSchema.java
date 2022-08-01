package database;

import org.alfasoftware.morf.upgrade.adapt.TableSetSchema;

import java.util.List;

import static org.alfasoftware.morf.metadata.DataType.BIG_INTEGER;
import static org.alfasoftware.morf.metadata.DataType.STRING;
import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;
import static org.alfasoftware.morf.upgrade.db.DatabaseUpgradeTableContribution.deployedViewsTable;
import static org.alfasoftware.morf.upgrade.db.DatabaseUpgradeTableContribution.upgradeAuditTable;

public class MoneyMakerDatabaseSchema extends TableSetSchema {
    /**
     * Construct a TableSet which represents the specified set of tables.
     */
    public MoneyMakerDatabaseSchema() {
        super(List.of(
                        deployedViewsTable(),
                        upgradeAuditTable(),
                        table("Test1").columns(
                                column("id", BIG_INTEGER).autoNumbered(1),
                                column("val", STRING, 100)
                        ),
                        table("Test2").columns(
                                column("id", BIG_INTEGER).autoNumbered(1),
                                column("val", STRING, 100)
                        )
                )
        );
    }
}
