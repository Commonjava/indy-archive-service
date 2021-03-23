package org.commonjava.indy.service.archive.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;

public class PreSeedConfig
{
    @ConfigProperty( name = "pre-seed.dir", defaultValue = "/deployments/data")
    public String dir;

    @ConfigProperty( name = "pre-seed.pattern")
    public String pattern;

    @ConfigProperty( name = "pre-seed.upstream.host")
    public String host;

    @ConfigProperty( name = "pre-seed.upstream.port")
    public int port;

    @ConfigProperty( name = "pre-seed.upstream.ssl")
    public boolean ssl;

    @Override
    public String toString() {
        return "PreSeedConfig{" +
                "dir='" + dir + '\'' +
                ", pattern='" + pattern + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", ssl=" + ssl +
                '}';
    }
}