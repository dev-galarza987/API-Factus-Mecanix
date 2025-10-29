package dev.galarza.factus.invoice.mapper;

import dev.galarza.factus.client.mapper.ClientMapper;
import dev.galarza.factus.detail.mapper.DetailMapper;
import dev.galarza.factus.invoice.dto.InvoiceResponseDTO;
import dev.galarza.factus.invoice.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ClientMapper.class, DetailMapper.class})
public interface InvoiceMapper {

    @Mapping(source = "client", target = "client")
    @Mapping(source = "detalles", target = "detalles")
    InvoiceResponseDTO toResponseDTO(Invoice invoice);
}

