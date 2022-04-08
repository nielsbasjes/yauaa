package nl.basjes.parse.useragent.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

class TestStringTable {

    private static final Logger LOG = LogManager.getLogger(TestStringTable.class);

    @Test
    void testStringTable() {
        StringTable table = new StringTable();
        LOG.error("\n{}", table
            .withHeaders("One", "Two", "Three")
            .addRow("1", "2", "3", "4")
            .addRowSeparator()
            .addRow("11",       "22")
            .addRow("1111",     "2222",     "33")
            .addRow("111111",   "222222",   "3333",     "444444444")
            .addRow("11111111", "22222222", "333333",   "4444", "55"));
    }

}
