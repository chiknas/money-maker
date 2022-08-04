package database;

import org.alfasoftware.morf.upgrade.adapt.TableSetSchema;

import java.util.List;

import static org.alfasoftware.morf.metadata.DataType.*;
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
                        table("trade_transaction").columns(
                                column("id", BIG_INTEGER).autoNumbered(1),
                                column("asset_code", STRING, 10),
                                column("type", STRING, 10),
                                column("price", DECIMAL, 100),
                                column("cost", DECIMAL, 100),
                                // strategy name that executed this trade
                                column("strategy", STRING, 100),
                                // LocalDateTime timestamp
                                column("time", BIG_INTEGER, 19),
                                // the period time length for each candlestick
                                column("period_length", STRING, 19)
                        )
                )
        );
    }
}
