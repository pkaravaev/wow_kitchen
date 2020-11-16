package com.foodtech.back.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.foodtech.back.entity.model.Address;
import com.foodtech.back.repository.model.AddressRepository;
import com.foodtech.back.util.ResponseCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Set;

import static com.foodtech.back.IntegrationTestData.LATITUDE;
import static com.foodtech.back.IntegrationTestData.LONGITUDE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = {"classpath:user-data.sql"})
class AddressControllerIntegrationTest extends AbstractControllerIntegrationTest {

    @Autowired
    AddressRepository addressRepository;

    @Test
    void getAddresses() throws Exception {

        MvcResult result = mockMvc.perform(get("/app/user/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<Address> addresses = mapResult(result.getResponse().getContentAsString(), new TypeReference<>() {});

        assertEquals(2, addresses.size());
        assertThat(addresses).usingElementComparatorIgnoringFields("user", "id", "actual")
                .containsOnlyElementsOf(Set.of(testData.address1(), testData.address2()));
    }

    @Test
    void addAddress() throws Exception {

        checkAddAddressTestData();

        mockMvc.perform(post("/app/user/address?latitude=" + LATITUDE + "&longitude=" + LONGITUDE)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader())
                .content(objectMapper.writeValueAsString(testData.addressNew())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertAddressAdded();
        assertOnlyOneActualAddress();
    }

    private void checkAddAddressTestData() {
        List<Address> addresses = addressRepository.findByUserIdOrderByCreatedDesc(testData.user1().getId());
        assertEquals(2, addresses.size());
    }

    private void assertAddressAdded() {
        Address address = addressRepository.findFirstByUserIdAndActualTrue(testData.user1().getId()).orElseThrow();
        assertTrue(address.isActual());
        assertThat(address).isEqualToIgnoringGivenFields(testData.addressNew(), "user", "id", "deliveryTerminal", "created", "updated");
    }

    @Test
    void addAddressRestrictedHomeValue() throws Exception {

        checkAddAddressTestData();

        Address addressNew = testData.addressNew();
        addressNew.setHome("улица4");

        mockMvc.perform(post("/app/user/address?latitude=" + LATITUDE + "&longitude=" + LONGITUDE)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader())
                .content(objectMapper.writeValueAsString(addressNew)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.USER_ADDRESS_INVALID.toString()));

        assertAddressNotSaved();
    }

    @Test
    void addAddressInvalidHomeValue() throws Exception {

        checkAddAddressTestData();

        Address addressNew = testData.addressNew();
        addressNew.setHome("aaaa");

        mockMvc.perform(post("/app/user/address?latitude=" + LATITUDE + "&longitude=" + LONGITUDE)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader())
                .content(objectMapper.writeValueAsString(addressNew)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.USER_ADDRESS_INVALID.toString()));

        assertAddressNotSaved();
    }

    @Test
    void addAddressWrongModel() throws Exception {

        checkAddAddressTestData();

        Address addressNew = testData.addressNew();
        addressNew.setHome(null);
        addressNew.setStreet(null);

        mockMvc.perform(post("/app/user/address?latitude=" + LATITUDE + "&longitude=" + LONGITUDE)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader())
                .content(objectMapper.writeValueAsString(addressNew)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.REQUEST_INVALID.toString()));

        assertAddressNotSaved();
    }

    @Test
    void addAddressNotInZone() throws Exception {

        checkAddAddressTestData();

        mockMvc.perform(post("/app/user/address?latitude=" + "53.148427" + "&longitude=" + "29.238390")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader())
                .content(objectMapper.writeValueAsString(testData.addressNew())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.USER_ADDRESS_INVALID.toString()));

        assertAddressNotSaved();
    }

    private void assertAddressNotSaved() {
        List<Address> addresses = addressRepository.findByUserIdOrderByCreatedDesc(testData.user1().getId());
        assertEquals(2, addresses.size());
    }

    @Test
    void setActualAddress() throws Exception {

        checkSetActualAddressTestData();

        mockMvc.perform(post("/app/user/address/actual/" + testData.address2().getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertActualAddressChanged();
        assertOnlyOneActualAddress();
    }

    private void checkSetActualAddressTestData() {
        Address nonActualAddress = addressRepository.findById(testData.address2().getId()).orElseThrow();
        assertFalse(nonActualAddress.isActual());
    }

    private void assertActualAddressChanged() {
        Address actualAddress = addressRepository.findFirstByUserIdAndActualTrue(testData.user1().getId()).orElseThrow();
        assertTrue(actualAddress.isActual());
        assertThat(actualAddress).isEqualToIgnoringGivenFields(testData.address2(), "actual", "user", "id", "deliveryTerminal", "created", "updated");
    }

    private void assertOnlyOneActualAddress() {
        List<Address> addresses = addressRepository.findByUserIdOrderByCreatedDesc(testData.user1().getId());
        int actualAddressesNum = 0;
        for (Address address : addresses) {
            if (address.isActual()) {
                actualAddressesNum++;
            }
        }
        assertEquals(1, actualAddressesNum);
    }
}
