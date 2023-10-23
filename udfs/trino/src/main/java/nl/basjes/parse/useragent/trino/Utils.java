package nl.basjes.parse.useragent.trino;

import io.trino.spi.block.Block;
import io.trino.spi.block.MapBlockBuilder;
import io.trino.spi.type.MapType;
import io.trino.spi.type.TypeOperators;

import java.util.Map;

import static io.trino.spi.type.VarcharType.VARCHAR;

final class Utils {
    private Utils() {
        // Utilities only
    }

    public static Block encodeMap(Map<String, String> map) {
        MapType mapType = new MapType(VARCHAR, VARCHAR, new TypeOperators());

        MapBlockBuilder blockBuilder = mapType.createBlockBuilder(null, map.size());
        blockBuilder.buildEntry((keyBuilder, valueBuilder) ->
            map.forEach((key, value) -> {
                VARCHAR.writeString(keyBuilder, key);
                VARCHAR.writeString(valueBuilder, value);
            })
        );
        return blockBuilder.build().getObject(0, Block.class);
    }
}
