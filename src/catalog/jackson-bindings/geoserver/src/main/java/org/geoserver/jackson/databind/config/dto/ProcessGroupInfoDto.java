package org.geoserver.jackson.databind.config.dto;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.geoserver.jackson.databind.catalog.dto.MetadataMapDto;
import org.geoserver.jackson.databind.catalog.dto.ValueObjectInfoDto;
import org.geoserver.wps.ProcessGroupInfo;

/** DTO for {@link ProcessGroupInfo} */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
// @JsonTypeName("ProcessGroupInfo")
public class ProcessGroupInfoDto extends ValueObjectInfoDto {
    private String factoryClass;
    private boolean isEnabled;
    private List<ProcessInfoDto> filteredProcesses;
    private MetadataMapDto metadata;
    private List<String> roles;
}
