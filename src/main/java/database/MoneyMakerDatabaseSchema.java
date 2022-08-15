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
                        table("trade").columns(
                                column("id", BIG_INTEGER).autoNumbered(1),
                                // strategy name of the strategy used for this trade
                                column("entry_strategy", STRING, 100),
                                // the period time length for each candlestick
                                column("period_length", STRING, 19),
                                // strategy id of the exit_strategy table with more details about the exit strategy to be used for this trade
                                column("exit_strategy", STRING, 100),
                                // the order id that opened this trade
                                column("entry_order_id", BIG_INTEGER),
                                // the order id that closed this trade
                                column("exit_order_id", BIG_INTEGER).nullable(),
                                // how much we made from this trade
                                column("profit", BIG_INTEGER).nullable()
                        ),
                        table("trade_order").columns(
                                column("id", BIG_INTEGER).autoNumbered(1),
                                column("order_reference", STRING, 100),
                                column("type", STRING, 10),
                                column("asset_code", STRING, 10),
                                column("price", DECIMAL, 100),
                                // LocalDateTime timestamp
                                column("time", BIG_INTEGER, 19),
                                column("cost", DECIMAL, 100),
                                // if the trade is currently open/closed/pending
                                column("status", STRING, 19),
                                column("volume", BIG_INTEGER, 19)
                        )
                )
        );
    }
}
