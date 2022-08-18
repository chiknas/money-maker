package database.entities;

/**
 * An order might open a new trade or exit an existing one.
 */
public enum TradeOrderType {
    // order that opened a new trade
    ENTRY,

    // Orders required during the trade.
    // For example a cancelled exit order or more complicated trade that required more than just
    // entry and exit orders.
    INTERMEDIATE,

    // order that closed a trade
    EXIT;
}
