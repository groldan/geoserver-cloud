package org.geoserver.jackson.databind.catalog.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import lombok.Data;
import org.geoserver.jackson.databind.config.dto.ContactInfoDto;
import org.geoserver.jackson.databind.config.dto.ProcessGroupInfoDto;
import org.geoserver.jackson.databind.config.dto.ProcessInfoDto;

@JsonSubTypes({
    @JsonSubTypes.Type(value = AttributionInfoDto.class),
    @JsonSubTypes.Type(value = ContactInfoDto.class),
    @JsonSubTypes.Type(value = DataLinkInfoDto.class),
    @JsonSubTypes.Type(value = LayerGroupStyleDto.class),
    @JsonSubTypes.Type(value = LegendInfoDto.class),
    @JsonSubTypes.Type(value = MetadataLinkInfoDto.class),
    @JsonSubTypes.Type(value = ProcessGroupInfoDto.class),
    @JsonSubTypes.Type(value = ProcessInfoDto.class)
})
@Data
// @ToString(callSuper = true)
// @EqualsAndHashCode(callSuper = true)
public abstract class ValueObjectInfoDto { // extends InfoDto {
    private String id;
}
