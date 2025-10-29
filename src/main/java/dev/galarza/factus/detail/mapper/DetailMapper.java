package dev.galarza.factus.detail.mapper;

import dev.galarza.factus.detail.dto.DetailRequestDTO;
import dev.galarza.factus.detail.dto.DetailResponseDTO;
import dev.galarza.factus.detail.entity.Detail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DetailMapper {

    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    Detail toEntity(DetailRequestDTO dto);

    DetailResponseDTO toResponseDTO(Detail detail);
}

