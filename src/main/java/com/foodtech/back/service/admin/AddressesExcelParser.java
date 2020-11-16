package com.foodtech.back.service.admin;

import com.foodtech.back.entity.model.AddressDirectory;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Service
public class AddressesExcelParser {

    private static final int STREET_INDEX = 0;
    private static final int HOME_INDEX = 1;
    private static final DataFormatter FORMATTER = new DataFormatter();

    public Set<AddressDirectory> parseWorkbook(Workbook workbook) {
        Set<AddressDirectory> addresses = new HashSet<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            addresses.addAll(parseSheet(sheet));
        }
        return addresses;
    }

    private Set<AddressDirectory> parseSheet(Sheet sheet) {
        Set<AddressDirectory> addresses = new HashSet<>();
        sheet.forEach(row -> addresses.add(parseRow(row)));
        return addresses;
    }

    private AddressDirectory parseRow(Row row) {
        String street = FORMATTER.formatCellValue(row.getCell(STREET_INDEX)).trim();
        String house = FORMATTER.formatCellValue(row.getCell(HOME_INDEX)).trim();
        if (!StringUtils.hasText(house)) {
            house = "без дома";
        }
        return AddressDirectory.of(street, house);
    }
}
