package test.task.bankcards.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import test.task.bankcards.dto.response.CardResponse;
import test.task.bankcards.entity.Card;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardMapper {

    @Mapping(target = "maskedCardNumber", source = "last4", qualifiedByName = "maskLast4")
    @Mapping(target = "holder", source = "holder")
    CardResponse toDto(Card card);

    @Named("maskLast4")
    static String maskLast4(String last4) {
        return "**** **** **** " + last4;
    }
}
