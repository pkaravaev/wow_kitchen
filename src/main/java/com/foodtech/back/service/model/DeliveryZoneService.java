package com.foodtech.back.service.model;

import com.foodtech.back.dto.iiko.IikoCityStreetsDirectoryDto;
import com.foodtech.back.dto.iiko.IikoDeliveryZoneDto;
import com.foodtech.back.entity.model.Address;
import com.foodtech.back.entity.model.KitchenDeliveryTerminal;
import com.foodtech.back.entity.model.OpeningHours;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.model.iiko.DeliveryCoordinate;
import com.foodtech.back.entity.model.iiko.DeliveryZone;
import com.foodtech.back.repository.iiko.IikoCityDirectoryRepository;
import com.foodtech.back.repository.iiko.IikoDeliveryZoneRepository;
import com.foodtech.back.repository.model.AddressRepository;
import com.foodtech.back.service.iiko.IikoRequestService;
import com.foodtech.back.util.DateUtil;
import com.foodtech.back.util.exceptions.DeliveryZoneNotActiveException;
import com.foodtech.back.util.exceptions.KitchenClosedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.foodtech.back.util.CoordinatesUtil.coordinateInZone;
import static com.foodtech.back.util.mapper.IikoMappers.toDeliveryZoneEntityList;

@Service
@Slf4j
public class DeliveryZoneService {

    private final IikoDeliveryZoneRepository deliveryZoneRepository;

    private final IikoCityDirectoryRepository cityDirectoryRepository;

    private final IikoRequestService iikoRequestService;

    private final AddressRepository addressRepository;

    private final DateUtil dateUtil;

    public DeliveryZoneService(IikoDeliveryZoneRepository deliveryZoneRepository,
                               IikoCityDirectoryRepository cityDirectoryRepository,
                               IikoRequestService requestService,
                               AddressRepository addressRepository, DateUtil dateUtil) {
        this.deliveryZoneRepository = deliveryZoneRepository;
        this.cityDirectoryRepository = cityDirectoryRepository;
        this.iikoRequestService = requestService;
        this.addressRepository = addressRepository;
        this.dateUtil = dateUtil;
    }

    public boolean coordinatesInDeliveryZone(DeliveryCoordinate coordinate) {
        List<DeliveryZone> deliveryZones = deliveryZoneRepository.findAll();
        return deliveryZones.stream().anyMatch(zone -> coordinateInZone(zone, coordinate));
    }

    public Optional<DeliveryZone> getByCoordinates(DeliveryCoordinate coordinate) {
        List<DeliveryZone> zones = deliveryZoneRepository.findAll();
        return zones.stream().filter(zone -> coordinateInZone(zone, coordinate)).findFirst();
    }

    public Optional<KitchenDeliveryTerminal> getDeliveryTerminalByCoordinatesInActiveZone(DeliveryCoordinate coordinate) {
        List<DeliveryZone> zones = deliveryZoneRepository.findAllActive();
        Optional<DeliveryZone> thisAddressZone = zones.stream()
                .filter(zone -> coordinateInZone(zone, coordinate))
                .findFirst();
        return thisAddressZone.map(DeliveryZone::getDeliveryTerminal);
    }

    public void checkKitchenIsOpenedOrElseThrow(User user) {
        Address address = addressRepository.findByUserIdAndActualTrueWithDeliveryTerminalAndOpeningHours(user.getId()).orElseThrow();
        KitchenDeliveryTerminal terminal = address.getDeliveryTerminal();

        if (!zoneIsActive(terminal)) {
            throw new DeliveryZoneNotActiveException();
        }

        if (!kitchenIsOpened(terminal)) {
            throw new KitchenClosedException();
        }
    }

    private boolean zoneIsActive(KitchenDeliveryTerminal terminal) {
        return deliveryZoneRepository.existsByDeliveryTerminalAndActiveIsTrue(terminal);
    }

    private boolean kitchenIsOpened(KitchenDeliveryTerminal terminal) {
        LocalDateTime currentDateForTerminal = dateUtil.getCurrentDayForTimeZone(terminal.getTimeZone());
        OpeningHours openingHoursForCurrentDay = getOpeningHoursForCurrentDay(currentDateForTerminal, terminal.getOpeningHours());
        return openingHoursForCurrentDay.isOpenNow(currentDateForTerminal.toLocalTime());
    }

    private OpeningHours getOpeningHoursForCurrentDay(LocalDateTime currentDate, List<OpeningHours> openingHours) {
        DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();
        return openingHours
                .stream()
                .filter(oh -> oh.getDayOfWeek().equals(currentDayOfWeek.getValue()))
                .findFirst()
                .orElseThrow();
    }

    /*
    Координаты зон доставки
     */

    @Cacheable(CacheService.DELIVERY_ZONE)
    public List<DeliveryZone> getDeliveryZones() {
        return deliveryZoneRepository.findAll();
    }

    @CacheEvict(value = CacheService.DELIVERY_ZONE, allEntries = true)
    public void saveDeliveryZones() {
        List<IikoDeliveryZoneDto> deliveryZonesDto = iikoRequestService.getDeliveryZones();
        List<DeliveryZone> deliveryZones = toDeliveryZoneEntityList(deliveryZonesDto);
        deliveryZoneRepository.saveAll(deliveryZones);
    }


    /*
    Справочник городов-улиц выгруженный в iiko
     */

    // Сохранение справочника городов/улиц
    public void saveCityStreetsDirectory() {
        List<IikoCityStreetsDirectoryDto> directory = iikoRequestService.getCityStreetsDirectory();
        for (IikoCityStreetsDirectoryDto dto : directory) {
            dto.getStreets().forEach(street -> street.setCity(dto.getCity()));
            dto.getCity().setStreets(dto.getStreets());
            cityDirectoryRepository.save(dto.getCity());
        }
    }

}
