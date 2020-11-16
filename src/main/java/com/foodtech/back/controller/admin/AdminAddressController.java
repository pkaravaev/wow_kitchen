package com.foodtech.back.controller.admin;

import com.foodtech.back.entity.model.AddressDirectory;
import com.foodtech.back.service.admin.AddressesExcelParser;
import com.foodtech.back.service.model.AddressDirectoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@Slf4j
public class AdminAddressController {

    private final AddressesExcelParser excelParser;

    private final AddressDirectoryService addressDirectoryService;

    public AdminAddressController(AddressesExcelParser excelParser, AddressDirectoryService addressDirectoryService) {
        this.excelParser = excelParser;
        this.addressDirectoryService = addressDirectoryService;
    }

    @GetMapping("/admin/addresses")
    public String addressesProperties() {
        return "addresses/addresses";
    }

    @GetMapping("/admin/addresses/list/page/{page}")
    public String getAddresses(@PathVariable Integer page, Model model) {
        Page<AddressDirectory> addressesPage = addressDirectoryService.getAllInActiveZones(PageRequest.of(page - 1, 200, Sort.by("street")));
        model.addAttribute("addresses", addressesPage.getContent());
        int totalPages = addressesPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1,totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        model.addAttribute("currentPage", page);
        return "addresses/addressesList";
    }

    @PostMapping("/admin/addresses/upload")
    public String uploadAddressDirectory(MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
        InputStream in = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(in);
        Set<AddressDirectory> directory = excelParser.parseWorkbook(workbook);
        addressDirectoryService.saveDirectory(directory);
        redirectAttributes.addFlashAttribute("message", "Загружено " + directory.size() + " адресов");

        return "redirect:/admin/addresses";
    }

    /* Пример данных в файле для сохранения координат:
    * улица Сарайшык, 34, координаты: 51.133752 71.429127 #проспект Кабанбай Батыра, 13/1, координаты: 51.137448 71.413792 #проспект Кабанбай Батыра, 36/2, координаты: 51.126223 71.416505 */
    @PostMapping("/admin/addresses/coordinates/upload")
    public String uploadCoordinates(MultipartFile file) throws IOException {
        String content = new String(file.getBytes());
        addressDirectoryService.saveWithCoordinates(content);
        return "redirect:/admin/addresses";
    }


}
