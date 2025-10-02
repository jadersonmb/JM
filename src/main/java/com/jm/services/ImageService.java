package com.jm.services;

import com.jm.dto.ImageDTO;
import com.jm.entity.Image;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.ImageMapper;
import com.jm.repository.ImageRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ImageService {

    private final ImageRepository repository;
    private final ImageMapper mapper;
    private final UserService userService;
    private final MessageSource messageSource;

    public ImageDTO save(ImageDTO imageDTO) {
        Image entity = mapper.toEntity(imageDTO);
        entity.setUsers(userService.findByEntityId(imageDTO.getUserId()));

        return mapper.toDTO(repository.save(entity));
    }

    public ImageDTO findById(UUID id) {
        ProblemType problemType = ProblemType.IMAGE_NOT_FOUND;
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[]{""}, LocaleContextHolder.getLocale());

        Image entity = repository.findById(id).orElseThrow(() -> new JMException(HttpStatus.BAD_REQUEST.value(),
                problemType.getTitle(), problemType.getUri(), messageDetails));
        entity.setUsers(userService.findByEntityId(entity.getId()));

        return mapper.toDTO(entity);
    }

    public List<Image> findByUserId(UUID userId) {
        ProblemType problemType = ProblemType.IMAGE_NOT_FOUND;
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[]{""}, LocaleContextHolder.getLocale());

        return repository.findByUsersId(userId).orElseThrow(() -> new JMException(HttpStatus.BAD_REQUEST.value(),
                problemType.getTitle(), problemType.getUri(), messageDetails));
    }

    public void delete(String fileName) { repository.deleteByFileName(fileName);}

    public void updateImage(Image entity) {
        repository.save(entity);
    }
}
