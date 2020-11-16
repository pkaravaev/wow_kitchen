package com.foodtech.back.dto.iiko;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IikoOrder {

    private String id;

    private String externalId;

    private LocalDateTime date; // iiko manual: “YYYY-MM-DD hh:mm:ss”, может быть null, тогда iiko сама подставит тек. время

    private List<IikoOrderItem> items;

    private List<IikoPaymentItem> paymentItems;

    private String phone; // iiko manual: regex “^(8|\+?\d{1,3})?[ -]?\(?(\d{3})\)?[ -]?(\d{3})[-]?(\d{2})[ -]?(\d{2})$”; max length = 40

    @JsonProperty(value = "isSelfService")
    private boolean isSelfService; // самовывоз

    private String orderTypeId;

    private IikoAddressDto address;

    private String comment; // iiko manual: max length 500

//    private int personsCount;

//    private int fullSum;

    //список возможных полей не полный, указаны лишь те, что необходимы на данный момент

}
