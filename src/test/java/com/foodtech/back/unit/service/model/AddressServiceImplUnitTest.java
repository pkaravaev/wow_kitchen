package com.foodtech.back.unit.service.model;

import com.foodtech.back.entity.model.Address;
import com.foodtech.back.entity.model.KitchenDeliveryTerminal;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.model.iiko.DeliveryCoordinate;
import com.foodtech.back.repository.iiko.IikoStreetDirectoryRepository;
import com.foodtech.back.repository.model.AddressRepository;
import com.foodtech.back.repository.model.NewStreetRepository;
import com.foodtech.back.service.model.AddressServiceImpl;
import com.foodtech.back.service.model.DeliveryZoneService;
import com.foodtech.back.unit.AbstractUnitTest;
import com.foodtech.back.util.exceptions.UserAddressInvalidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AddressServiceImplUnitTest extends AbstractUnitTest {

    @Autowired
    AddressServiceImpl addressService;

    @MockBean
    AddressRepository addressRepository;

    @MockBean
    DeliveryZoneService deliveryZoneService;

    @MockBean
    IikoStreetDirectoryRepository streetRepository;

    @MockBean
    NewStreetRepository newStreetRepository;

    @BeforeEach
    void setUp() {
        when(deliveryZoneService.getDeliveryTerminalByCoordinatesInActiveZone(COORDINATE)).thenReturn(Optional.of(terminal()));
        when(streetRepository.existsByNameEquals(newAddress().getStreet())).thenReturn(true);
    }

    @Test
    void isActualByUser() {
        //when
        when(addressRepository.findFirstByUserIdAndActualTrue(user().getId())).thenReturn(Optional.of(actualAddress()));

        //do
        Address address = addressService.getActualByUser(user().getId());

        //then
        assertEquals(actualAddress(), address);
    }

    @Test
    void isActualByUserNoActualAddress() {
        //when
        when(addressRepository.findFirstByUserIdAndActualTrue(user().getId())).thenReturn(Optional.empty());
        Address notActual = notActualAddress1();
        when(addressRepository.findFirstByUserIdOrderByCreatedDesc(user().getId())).thenReturn(Optional.of(notActual));

        //do
        Address address = addressService.getActualByUser(user().getId());

        //then
        assertThat(address).isEqualToIgnoringGivenFields(notActual);
        assertTrue(address.isActual());
        assertTrue(notActual.isActual());
    }

    @Test
    void isActualByUserNoAddresses() {
        //when
        when(addressRepository.findFirstByUserIdAndActualTrue(user().getId())).thenReturn(Optional.empty());
        when(addressRepository.findFirstByUserIdOrderByCreatedDesc(user().getId())).thenReturn(Optional.empty());

        //do
        assertThrows(NoSuchElementException.class, () -> addressService.getActualByUser(user().getId()));
    }

    @Test
    void addNewNoAddressesPresent() {
        //when
        when(addressRepository.findByUserIdOrderByCreatedDesc(user().getId())).thenReturn(Collections.emptyList());

        //do
        Address createdAddress = addressService.add(user(), newAddress(), COORDINATE);

        //then
        assertThat(createdAddress).isEqualToIgnoringGivenFields(newAddress(), "user", "actual", "deliveryTerminal");
        assertTrue(createdAddress.isActual());
        assertEquals(terminal().getId(), createdAddress.getDeliveryTerminal().getId());
        assertEquals(user(), createdAddress.getUser());

        verify(addressRepository, times(1)).save(createdAddress);
    }


    @Test
    void addNewActualAlreadyPresent_order1() {
        //when
        Address actual = actualAddress();
        Address notActual1 = notActualAddress1();
        Address notActual2 = notActualAddress2();
        when(addressRepository.findByUserIdOrderByCreatedDesc(user().getId())).thenReturn(List.of(actual, notActual1, notActual2));

        //do
        Address createdAddress = addressService.add(user(), newAddress(), COORDINATE);

        //then
        assertThat(createdAddress).isEqualToIgnoringGivenFields(newAddress(), "user", "actual", "deliveryTerminal");
        assertTrue(createdAddress.isActual());
        assertEquals(terminal().getId(), createdAddress.getDeliveryTerminal().getId());
        assertEquals(user(), createdAddress.getUser());

        assertFalse(actual.isActual());
        assertFalse(notActual1.isActual());

        verify(addressRepository, times(1)).save(createdAddress);
    }

    @Test
    void addNewActualAlreadyPresent_order2() {
        //when
        Address actual = actualAddress();
        Address notActual1 = notActualAddress1();
        Address notActual2 = notActualAddress2();
        when(addressRepository.findByUserIdOrderByCreatedDesc(user().getId())).thenReturn(List.of(notActual1, actual, notActual2));

        //do
        Address createdAddress = addressService.add(user(), newAddress(), COORDINATE);

        //then
        assertThat(createdAddress).isEqualToIgnoringGivenFields(newAddress(), "user", "actual", "deliveryTerminal");
        assertTrue(createdAddress.isActual());
        assertEquals(terminal().getId(), createdAddress.getDeliveryTerminal().getId());
        assertEquals(user(), createdAddress.getUser());

        assertFalse(actual.isActual());
        assertFalse(notActual1.isActual());

        verify(addressRepository, times(1)).save(createdAddress);
    }

    @Test
    void addNewActualAlreadyPresent_order3() {
        //when
        Address actual = actualAddress();
        Address notActual1 = notActualAddress1();
        Address notActual2 = notActualAddress2();
        when(addressRepository.findByUserIdOrderByCreatedDesc(user().getId())).thenReturn(List.of(notActual1, notActual2, actual));

        //do
        Address createdAddress = addressService.add(user(), newAddress(), COORDINATE);

        //then
        assertThat(createdAddress).isEqualToIgnoringGivenFields(newAddress(), "user", "actual", "deliveryTerminal");
        assertTrue(createdAddress.isActual());
        assertEquals(terminal().getId(), createdAddress.getDeliveryTerminal().getId());
        assertEquals(user(), createdAddress.getUser());

        assertFalse(actual.isActual());
        assertFalse(notActual1.isActual());

        verify(addressRepository, times(1)).save(createdAddress);
    }

    @Test
    void addNewNoActualPresent() {
        //when
        Address notActual1 = notActualAddress1();
        Address notActual2 = notActualAddress2();
        when(addressRepository.findByUserIdOrderByCreatedDesc(user().getId())).thenReturn(List.of(notActual1, notActual2));

        //do
        Address createdAddress = addressService.add(user(), newAddress(), COORDINATE);

        //then
        assertThat(createdAddress).isEqualToIgnoringGivenFields(newAddress(), "user", "actual", "deliveryTerminal");
        assertTrue(createdAddress.isActual());
        assertEquals(terminal().getId(), createdAddress.getDeliveryTerminal().getId());
        assertEquals(user(), createdAddress.getUser());

        assertFalse(notActual1.isActual());
        assertFalse(notActual2.isActual());

        verify(addressRepository, times(1)).save(createdAddress);
    }

    @Test
    void addNewDefaultCity() {
        //when
        Address actualAddress = actualAddress();
        when(addressRepository.findByUserIdOrderByCreatedDesc(user().getId())).thenReturn(List.of(actualAddress));

        //do
        Address newAddress = newAddress();
        newAddress.setCity(null);
        Address createdAddress = addressService.add(user(), newAddress, COORDINATE);

        //then
        assertThat(createdAddress).isEqualToIgnoringGivenFields(newAddress, "user", "actual", "deliveryTerminal");
        assertTrue(createdAddress.isActual());
        assertEquals(terminal().getId(), createdAddress.getDeliveryTerminal().getId());
        assertEquals(user(), createdAddress.getUser());
        assertEquals(AddressServiceImpl.DEFAULT_CITY, createdAddress.getCity());

        assertFalse(actualAddress.isActual());

        verify(addressRepository, times(1)).save(createdAddress);
    }


    @Test
    void addUpdateExistingActual() {
        //when
        Address actual = actualAddress();
        Address notActual1 = notActualAddress1();
        Address notActual2 = notActualAddress2();
        when(addressRepository.findByUserIdOrderByCreatedDesc(user().getId())).thenReturn(List.of(actual, notActual1, notActual2));

        //do
        Address updatedAddress = addressService.add(user(), existedActualAddressUpdated(), COORDINATE);

        //then
        assertThat(updatedAddress).isEqualToIgnoringGivenFields(existedActualAddressUpdated(), "user", "actual", "deliveryTerminal");
        assertTrue(updatedAddress.isActual());

        assertThat(actual).isEqualToIgnoringGivenFields(existedActualAddressUpdated(), "user", "actual", "deliveryTerminal");
        assertTrue(actual.isActual());
        assertFalse(notActual1.isActual());
        assertFalse(notActual2.isActual());

        verify(addressRepository, times(0)).save(any(Address.class));
    }

    @Test
    void addUpdateExistingNotActual() {
        //when
        Address actual = actualAddress();
        Address notActual1 = notActualAddress1();
        Address notActual2 = notActualAddress2();
        when(addressRepository.findByUserIdOrderByCreatedDesc(user().getId())).thenReturn(List.of(actual, notActual1, notActual2));

        //do
        Address updatedAddress = addressService.add(user(), existedNotActualAddress1Updated(), COORDINATE);

        //then
        assertThat(updatedAddress).isEqualToIgnoringGivenFields(existedNotActualAddress1Updated(), "user", "actual", "deliveryTerminal");
        assertTrue(updatedAddress.isActual());

        assertThat(notActual1).isEqualToIgnoringGivenFields(existedNotActualAddress1Updated(), "user", "actual", "deliveryTerminal");
        assertTrue(notActual1.isActual());
        assertFalse(actual.isActual());
        assertFalse(notActual2.isActual());

        verify(addressRepository, times(0)).save(any(Address.class));
    }

    @Test
    void addNewAddressInvalidHome() {
        //when
        Address actualAddress = actualAddress();
        when(addressRepository.findByUserIdOrderByCreatedDesc(user().getId())).thenReturn(List.of(actualAddress));

        //do
        Address newAddress = newAddress();
        newAddress.setHome("INVALID_HOME");
        assertThrows(UserAddressInvalidException.class, () -> addressService.add(user(), newAddress, COORDINATE));

        //then
        verify(addressRepository, times(0)).save(any(Address.class));
    }

    @Test
    void addNoSuchDeliveryZone() {
        //when
        Address actualAddress = actualAddress();
        when(addressRepository.findByUserIdOrderByCreatedDesc(user().getId())).thenReturn(List.of(actualAddress));
        when(deliveryZoneService.getDeliveryTerminalByCoordinatesInActiveZone(COORDINATE)).thenReturn(Optional.empty());

        //do
        assertThrows(UserAddressInvalidException.class, () -> addressService.add(user(), newAddress(), COORDINATE));

        //then
        verify(addressRepository, times(0)).save(any(Address.class));
    }

    @Test
    void setAnotherActual() {
        //when
        Address actual = actualAddress();
        Address notActual1 = notActualAddress1();
        Address notActual2 = notActualAddress2();
        when(addressRepository.findByUserIdOrderByCreatedDesc(user().getId())).thenReturn(List.of(actual, notActual1, notActual2));

        //do
        Address newActual = addressService.setActual(user().getId(), notActualAddress2().getId());

        //then
        assertEquals(notActual2.getId(), newActual.getId());
        assertTrue(notActual2.isActual());
        assertTrue(newActual.isActual());

        assertFalse(actual.isActual());
        assertFalse(notActual1.isActual());
    }

    @Test
    void setActualAlreadyActual() {
        //when
        Address actual = actualAddress();
        Address notActual1 = notActualAddress1();
        Address notActual2 = notActualAddress2();
        when(addressRepository.findByUserIdOrderByCreatedDesc(user().getId())).thenReturn(List.of(actual, notActual1, notActual2));

        //do
        Address newActual = addressService.setActual(user().getId(), actual.getId());

        //then
        assertEquals(actual.getId(), newActual.getId());
        assertTrue(actual.isActual());
        assertTrue(newActual.isActual());

        assertFalse(notActual1.isActual());
        assertFalse(notActual2.isActual());
    }

    @Test
    void setActualNoActualPresent() {
        //when
        Address notActual1 = notActualAddress1();
        Address notActual2 = notActualAddress2();
        when(addressRepository.findByUserIdOrderByCreatedDesc(user().getId())).thenReturn(List.of(notActual1, notActual2));

        //do
        Address newActual = addressService.setActual(user().getId(), notActualAddress2().getId());

        //then
        assertEquals(notActual2.getId(), newActual.getId());
        assertTrue(notActual2.isActual());
        assertTrue(newActual.isActual());

        assertFalse(notActual1.isActual());
    }

    @Test
    void setActualAddressNotFound() {
        when(addressRepository.findByUserIdOrderByCreatedDesc(user().getId())).thenReturn(List.of(notActualAddress1()));

        assertThrows(NoSuchElementException.class, () -> addressService.setActual(user().getId(), notActualAddress2().getId()));
    }

    @Test
    void setActualNoAddresses() {
        when(addressRepository.findByUserIdOrderByCreatedDesc(user().getId())).thenReturn(Collections.emptyList());

        assertThrows(NoSuchElementException.class, () -> addressService.setActual(user().getId(), notActualAddress2().getId()));
    }

    private static User user() {
        User user = new User();
        user.setId(1L);
        user.setCountryCode("7");
        user.setMobileNumber("7777777777");
        return user;
    }

    private static Address actualAddress() {
        Address address = new Address();
        address.setId(1L);
        address.setCity("Нур-Султан");
        address.setStreet("улица Жубанова");
        address.setHome("1");
        address.setHousing("2A");
        address.setEntrance("3");
        address.setDoorphone("58K6709");
        address.setFloor("4");
        address.setApartment("77");
        address.setComment("Вход со двора");
        address.setActual(true);
        return address;
    }

    private static Address existedActualAddressUpdated() {
        Address address = actualAddress();
        address.setDoorphone("another doorphone");
        address.setEntrance("another entrance");
        address.setFloor("another floor");
        return address;
    }

    private static Address notActualAddress1() {
        Address address = new Address();
        address.setId(2L);
        address.setCity("Нур-Султан");
        address.setStreet("улица Жубанова");
        address.setHome("10");
        address.setHousing("7Б");
        address.setEntrance("3");
        address.setDoorphone("58K8809");
        address.setFloor("7");
        address.setApartment("44");
        address.setComment("Вход со двора");
        address.setActual(false);
        return address;
    }

    private static Address existedNotActualAddress1Updated() {
        Address address = notActualAddress1();
        address.setDoorphone("another doorphone");
        address.setEntrance("another entrance");
        address.setFloor("another floor");
        return address;
    }

    private static Address notActualAddress2() {
        Address address = new Address();
        address.setId(3L);
        address.setCity("Нур-Султан");
        address.setStreet("улица Жубанова");
        address.setHome("14");
        address.setHousing("9Б");
        address.setEntrance("8");
        address.setDoorphone("58K8809");
        address.setFloor("7");
        address.setApartment("44");
        address.setComment("Вход со двора");
        address.setActual(false);
        return address;
    }

    private static Address newAddress() {
        Address address = new Address();
        address.setCity("Нур-Султан");
        address.setStreet("улица Жубанова");
        address.setHome("2");
        address.setHousing("3A");
        address.setEntrance("4");
        address.setDoorphone("58K6710");
        address.setFloor("5");
        address.setApartment("88");
        address.setComment("Вход улицы");
        return address;
    }

    private static KitchenDeliveryTerminal terminal() {
        KitchenDeliveryTerminal terminal = new KitchenDeliveryTerminal();
        terminal.setId(2L);
        return terminal;
    }

    private final static DeliveryCoordinate COORDINATE = DeliveryCoordinate.of(BigDecimal.valueOf(50.0000), BigDecimal.valueOf(70.0000));


}