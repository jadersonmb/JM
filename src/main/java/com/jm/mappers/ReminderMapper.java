package com.jm.mappers;

import com.jm.dto.ReminderDTO;
import com.jm.entity.Reminder;
import com.jm.entity.Users;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface ReminderMapper {

    @Mapping(target = "targetUserId", source = "targetUser.id")
    @Mapping(target = "targetUserName", source = "targetUser", qualifiedByName = "mapUserName")
    @Mapping(target = "targetUserPhone", source = "targetUser.phoneNumber")
    @Mapping(target = "createdByUserId", source = "createdBy.id")
    @Mapping(target = "createdByUserName", source = "createdBy", qualifiedByName = "mapUserName")
    ReminderDTO toDTO(Reminder entity);

    @Mapping(target = "targetUser", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Reminder toEntity(ReminderDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "targetUser", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDto(ReminderDTO dto, @MappingTarget Reminder entity);

    @Named("mapUserName")
    default String mapUserName(Users user) {
        if (user == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        if (user.getName() != null && !user.getName().isBlank()) {
            builder.append(user.getName());
        }
        if (user.getLastName() != null && !user.getLastName().isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(user.getLastName());
        }
        if (builder.isEmpty() && user.getEmail() != null) {
            builder.append(user.getEmail());
        }
        return builder.isEmpty() ? null : builder.toString();
    }
}
