package petrigaal.edg.mdg;

import petrigaal.edg.Configuration;
import petrigaal.edg.dg.DGConfiguration;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MetaConfiguration implements Configuration<MetaConfiguration, MetaEdge, MetaTarget> {
    private final Set<MetaEdge> successors = new HashSet<>();
    private final Set<DGConfiguration> configurations;

    public MetaConfiguration(Set<DGConfiguration> configurations) {
        this.configurations = configurations;
    }

    public Set<DGConfiguration> getConfigurations() {
        return configurations;
    }

    @Override
    public Set<MetaEdge> getSuccessors() {
        return successors;
    }

    @Override
    public String toString() {
        return "{" + configurations + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaConfiguration that = (MetaConfiguration) o;
        return Objects.equals(configurations, that.configurations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configurations);
    }
}
