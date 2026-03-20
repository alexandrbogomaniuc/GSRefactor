package com.datastax.driver.core.schemabuilder;


/**
 * Use for Cassandra 3.x+
 */
public class CompressionOptions3 extends TableOptions.CompressionOptions {
    // Cassandra 3.x+ (including 5.x) expects 'class'; keep a JVM escape hatch
    // for legacy compatibility testing with older clusters.
    private static final boolean USE_CLASS_OPTION =
            Boolean.parseBoolean(System.getProperty("cassandra.compression.useClassOption", "true"));

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
