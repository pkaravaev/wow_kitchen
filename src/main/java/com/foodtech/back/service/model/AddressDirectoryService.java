package com.foodtech.back.service.model;

import com.foodtech.back.dto.model.AddressDirectoryDto;
import com.foodtech.back.dto.model.HouseDirectoryDto;
import com.foodtech.back.entity.model.AddressDirectory;
import com.foodtech.back.entity.model.iiko.DeliveryCoordinate;
import com.foodtech.back.entity.model.iiko.DeliveryZone;
import com.foodtech.back.repository.model.AddressDirectoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.foodtech.back.service.model.CacheService.ADDRESSES_DIRECTORY;
import static java.util.Objects.isNull;

/*
Наш вручную составленный справочник всех адресов в зоне доставки
 */

@Service
@Slf4j
public class AddressDirectoryService {

    private final AddressDirectoryRepository addressDirectoryRepository;

    private final DeliveryZoneService deliveryZoneService;

    public AddressDirectoryService(AddressDirectoryRepository addressDirectoryRepository,
                                   DeliveryZoneService deliveryZoneService) {
        this.addressDirectoryRepository = addressDirectoryRepository;
        this.deliveryZoneService = deliveryZoneService;
    }

    @Cacheable(ADDRESSES_DIRECTORY)
    @Transactional
    public List<AddressDirectoryDto> getAllInActiveZones() {
        List<AddressDirectory> directory = addressDirectoryRepository.findByDeliveryZone_ActiveIsTrue();
        Map<String, Set<HouseDirectoryDto>> streetsWithHouses = mapHousesToStreets(directory);
        return mapToAddressDirectoryDtos(streetsWithHouses);
    }

    private Map<String, Set<HouseDirectoryDto>> mapHousesToStreets(List<AddressDirectory> directory) {
        return directory
                .stream()
                .collect(Collectors.groupingBy(AddressDirectory::getStreet,
                        Collectors.mapping(this::houseDirectoryDto, Collectors.toSet())));
    }

    private HouseDirectoryDto houseDirectoryDto(AddressDirectory addressDirectory) {
        HouseDirectoryDto dto = new HouseDirectoryDto();
        dto.setHome(addressDirectory.getHouse());
        dto.setLatitude(addressDirectory.getLatitude());
        dto.setLongitude(addressDirectory.getLongitude());
        return dto;
    }

    private List<AddressDirectoryDto> mapToAddressDirectoryDtos(Map<String, Set<HouseDirectoryDto>> streetsWithHouses) {
        return streetsWithHouses.entrySet()
                .stream()
                .map(this::addressDirectoryDto)
                .collect(Collectors.toList());
    }

    private AddressDirectoryDto addressDirectoryDto(Map.Entry<String, Set<HouseDirectoryDto>> entry) {
        AddressDirectoryDto dto = new AddressDirectoryDto();
        dto.setStreet(entry.getKey());
        dto.setHouses(entry.getValue());
        return dto;
    }


    /* Методы для админки */

    public Page<AddressDirectory> getAllInActiveZones(PageRequest pageRequest) {
        return addressDirectoryRepository.findAll(pageRequest);
    }

    @CacheEvict(value = ADDRESSES_DIRECTORY, allEntries = true)
    public void saveDirectory(Set<AddressDirectory> addresses) {
        addressDirectoryRepository.saveAll(addresses);
    }

    @Transactional
    @CacheEvict(value = ADDRESSES_DIRECTORY, allEntries = true)
    public void saveWithCoordinates(String content) {
        Map<AddressDirectory, DeliveryCoordinate> coordinatesMap = parseContent(content);
        saveCoordinates(coordinatesMap);
    }

    private Map<AddressDirectory, DeliveryCoordinate> parseContent(String content) {
        String[] strings = content.split("#");
        return Arrays.stream(strings)
                .collect(Collectors.toMap(this::parseAddress, this::parseCoordinates, (c1, c2) -> c1));
    }

    private AddressDirectory parseAddress(String addressStr) {
        String street = addressStr.substring(0, addressStr.indexOf(',')).trim();
        String home = addressStr.substring(addressStr.indexOf(',')+2, addressStr.indexOf("координаты")).trim().replaceAll(",", "");
        return AddressDirectory.of(street, home);
    }

    private DeliveryCoordinate parseCoordinates(String addressStr) {
        String coordinatesStr = addressStr.substring(addressStr.indexOf("координаты: ") + 12).trim();
        String[] coordinatesArr = coordinatesStr.split(" ");
        return DeliveryCoordinate.of(new BigDecimal(coordinatesArr[0]), new BigDecimal(coordinatesArr[1]));
    }

    private void saveCoordinates(Map<AddressDirectory, DeliveryCoordinate> coordinatesMapFromFile) {
        List<AddressDirectory> addressDirectoriesFromDb = addressDirectoryRepository.findAll();
        for (AddressDirectory directory : addressDirectoriesFromDb) {
            DeliveryCoordinate coordinate = coordinatesMapFromFile.get(directory);
            if (isNull(coordinate)) {
                continue;
            }

            Optional<DeliveryZone> zone = deliveryZoneService.getByCoordinates(coordinate);
            if (zone.isEmpty()) {
                log.error("Coordinates for address '{}' not in delivery zone", directory);
                continue;
            }

            directory.setDeliveryZone(zone.get());
            directory.setLatitude(coordinate.getLatitude());
            directory.setLongitude(coordinate.getLongitude());
        }
    }
}
