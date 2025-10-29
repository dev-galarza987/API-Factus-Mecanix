package dev.galarza.factus.client.mapper;

import dev.galarza.factus.client.dto.ClientRequestDTO;
import dev.galarza.factus.client.dto.ClientResponseDTO;
import dev.galarza.factus.client.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClientMapper {

    ClientResponseDTO toResponseDTO(Client client);

    Client toEntity(ClientRequestDTO dto);

    void updateEntityFromDTO(ClientRequestDTO dto, @MappingTarget Client client);
}

