package org.geoserver.jackson.databind.config.dto;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.geoserver.jackson.databind.catalog.dto.MetadataMapDto;
import org.geoserver.jackson.databind.catalog.dto.ValueObjectInfoDto;
import org.geoserver.wps.ProcessInfo;
import org.geotools.jackson.databind.dto.NameDto;

/** DTO for {@link ProcessInfo} */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
// @JsonTypeName("ProcessInfo")
public class ProcessInfoDto extends ValueObjectInfoDto {
    private NameDto name;
    private boolean enabled;
    private List<String> roles;
    private MetadataMapDto metadata;
}
