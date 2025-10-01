package com.jm.mappers;

import com.jm.dto.ImageDTO;
import com.jm.entity.Image;
import org.springframework.stereotype.Component;

@Component
public class ImageMapper {

    public ImageDTO toDTO(Image entity){
        return ImageDTO.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .url(entity.getUrl())
                .userId(entity.getUsers().getId())
                .build();
    }

    public Image toEntity(ImageDTO dto) {
        return Image.builder()
                .id(dto.getId())
                .fileName(dto.getFileName())
                .url(dto.getUrl())
                .users(null)
                .build();
    }
}
