package com.jm.mappers;

import com.jm.dto.ObjectDTO;
import com.jm.security.entity.ObjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ObjectMapper {

    ObjectDTO toDto(ObjectEntity entity);
}
