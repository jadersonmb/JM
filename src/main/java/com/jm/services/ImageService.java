package com.jm.services;

import com.jm.dto.ImageDTO;
import com.jm.entity.Image;
import com.jm.execption.JMException;
import com.jm.mappers.ImageMapper;
import com.jm.repository.ImageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ImageService {

    private final ImageRepository repository;
    private final ImageMapper mapper;
    private final UserService userService;

    public ImageDTO save(ImageDTO imageDTO) {
        Image entity = mapper.toEntity(imageDTO);
        entity.setUsers(userService.findByEntityId(imageDTO.getUserId()));

        return mapper.toDTO(repository.save(entity));
    }

    public ImageDTO findById(UUID id) {
        Image entity = repository.findById(id).orElseThrow(() -> new JMException("Not found image with id: " + id));
        entity.setUsers(userService.findByEntityId(entity.getId()));

        return mapper.toDTO(entity);
    }

    public List<Image> findByUserId(UUID userId) {
        return repository.findByUsersId(userId);
    }

    public void updateImage(Image entity) {
        repository.save(entity);
    }
}
