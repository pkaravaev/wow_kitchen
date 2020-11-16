package com.foodtech.back.service.model;

import com.foodtech.back.entity.model.Address;
import com.foodtech.back.entity.model.KitchenDeliveryTerminal;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.model.iiko.DeliveryCoordinate;
import com.foodtech.back.entity.model.iiko.NewStreet;
import com.foodtech.back.repository.iiko.IikoStreetDirectoryRepository;
import com.foodtech.back.repository.model.AddressRepository;
import com.foodtech.back.repository.model.NewStreetRepository;
import com.foodtech.back.util.exceptions.UserAddressInvalidException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.foodtech.back.util.validation.AddressValidationUtil.addressIsValid;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.isEmpty;

@Service
@Slf4j
public class AddressServiceImpl implements AddressService {

    public static final String DEFAULT_CITY = "Нур-Султан";

    private final AddressRepository addressRepository;

    private final DeliveryZoneService deliveryZoneService;

    private final IikoStreetDirectoryRepository streetRepository;

    private final NewStreetRepository newStreetRepository;

    public AddressServiceImpl(AddressRepository addressRepository, DeliveryZoneService deliveryZoneService,
                              IikoStreetDirectoryRepository streetRepository, NewStreetRepository newStreetRepository) {
        this.addressRepository = addressRepository;
        this.deliveryZoneService = deliveryZoneService;
        this.streetRepository = streetRepository;
        this.newStreetRepository = newStreetRepository;
    }

    @Override
    public List<Address> getAllByUser(Long userId) {
        return addressRepository.findByUserIdOrderByCreatedDesc(userId);
    }

    @Override
    @Transactional
    public Address getActualByUser(Long userId) {
        Optional<Address> actualAddress = addressRepository.findFirstByUserIdAndActualTrue(userId);
        return actualAddress.orElseGet(() -> makeActualAndReturnLastCreated(userId));
    }

    private Address makeActualAndReturnLastCreated(Long userId) {
        Address lastCreatedAddress = addressRepository.findFirstByUserIdOrderByCreatedDesc(userId)
                .orElseThrow(() -> new NoSuchElementException("User '{}' doesn't have any addresses!"));
        lastCreatedAddress.setActual(true);
        return lastCreatedAddress;
    }

    @Override
    @Transactional
    public Address add(User user, Address newAddress, DeliveryCoordinate coordinate) {
        validate(newAddress, user);
        checkStreetExistsInIikoDirectory(newAddress.getStreet());
        List<Address> addresses = addressRepository.findByUserIdOrderByCreatedDesc(user.getId());
        prepareForSaving(newAddress, addresses, user, coordinate);
        Optional<Address> equalExistingAddress = findEqual(newAddress, addresses);
        return equalExistingAddress
                .map(address -> updateExisting(address, newAddress))
                .orElseGet(() -> createNew(newAddress));
    }

    private void validate(Address address, User user) {
        if (!addressIsValid(address)) {
            throw new UserAddressInvalidException(user.toString());
        }
    }

    private void prepareForSaving(Address address, List<Address> addresses, User user, DeliveryCoordinate coordinate) {
        KitchenDeliveryTerminal deliveryTerminal = deliveryZoneService.getDeliveryTerminalByCoordinatesInActiveZone(coordinate)
                .orElseThrow(() -> new UserAddressInvalidException(user.toString()));
        address.setDeliveryTerminal(deliveryTerminal);
        address.setUser(user);
        address.setCity(isEmpty(address.getCity()) ? DEFAULT_CITY : address.getCity().trim());
        address.setStreet(address.getStreet().trim());
        address.setHome(address.getHome().trim());
        addresses.forEach(a -> a.setActual(false));
        address.setActual(true);
    }

    private void checkStreetExistsInIikoDirectory(String street) {
        if (!streetRepository.existsByNameEquals(street)) {
            log.info("Street '{}' not found in iiko street directory. Saving to new street table", street);
            newStreetRepository.save(new NewStreet(street));
        }
    }

    private Optional<Address> findEqual(Address newAddress, List<Address> addresses) {
        return addresses
                .stream()
                .filter(a -> a.equals(newAddress))
                .findFirst();
    }

    private Address updateExisting(Address address, Address newAddress) {
        address.setEntrance(hasText(newAddress.getEntrance()) ? newAddress.getEntrance() : address.getEntrance());
        address.setFloor(hasText(newAddress.getFloor()) ? newAddress.getFloor() : address.getFloor());
        address.setDoorphone(hasText(newAddress.getDoorphone()) ? newAddress.getDoorphone() : address.getDoorphone());
        address.setComment(hasText(newAddress.getComment()) ? newAddress.getComment() : address.getComment());
        address.setActual(true);
        return address;
    }

    private Address createNew(Address newAddress) {
        addressRepository.save(newAddress);
        return newAddress;
    }

    @Override
    @Transactional
    public Address setActual(Long userId, Long addressId) {
        List<Address> addresses = addressRepository.findByUserIdOrderByCreatedDesc(userId);
        addresses.forEach(a -> a.setActual(a.getId().equals(addressId)));
        return actual(addresses);
    }

    private Address actual(List<Address> addresses) {
        return addresses.stream()
                .filter(Address::isActual)
                .findFirst()
                .orElseThrow();
    }
}
