package com.datastax.driver.core.schemabuilder;


/**
 * Use for Cassandra 3.x+
 */
public class CompressionOptions3 extends TableOptions.CompressionOptions {
    // Cassandra 2.x requires 'sstable_compression', Cassandra 3.x+ expects 'class'.
    // Keep 2.x as default for local baseline; set JVM flag
    // -Dcassandra.compression.useClassOption=true when running on Cassandra 3.x+.
    private static final boolean USE_CLASS_OPTION =
            Boolean.parseBoolean(System.getProperty("cassandra.compression.useClassOption", "false"));

    private Algorithm algorithm;

    public CompressionOptions3(Algorithm algorithm) {
        super(algorithm);
        this.algorithm = algorithm;
    }

    @Override
    public String build() {
        String superBuild = super.build();

        // MegaSpikePower because by some reason driver 3.5.11 does not have this change,
        // though this should be from Cassandra 3.
        // @see
        // https://docs.datastax.com/en/cassandra-oss/3.x/cassandra/operations/opsConfigCompress.html
        // section 3

        if (Algorithm.NONE.equals(algorithm)) {
            return USE_CLASS_OPTION ? "{'enabled': false }" : superBuild;
        }

        return USE_CLASS_OPTION
                ? superBuild.replace("sstable_compression", "class")
                : superBuild;
    }

}
