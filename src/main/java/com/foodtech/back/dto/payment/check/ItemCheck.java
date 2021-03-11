package com.foodtech.back.dto.payment.check;

import lombok.Data;

@Data
public class ItemCheck {

    private String label;
    private double price;
    private double quantity;
    private double amount;
    private String measurementUnit;
}


// "label": "Наименование товара 1", //наименование товара
//         "price": 100.00, //цена
//         "quantity": 1.00, //количество
//         "amount": 100.00, //сумма
//         "vat": 0, //ставка НДС
//         "method": 0, // тег-1214 признак способа расчета - признак способа расчета
//         "object": 0, // тег-1212 признак предмета расчета - признак предмета товара, работы, услуги, платежа, выплаты, иного предмета расчета
//         "measurementUnit": "шт" //единица измерения