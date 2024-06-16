package consulting.reason.tax_forms_api.controller;

import consulting.reason.tax_forms_api.AbstractControllerTest;
import consulting.reason.tax_forms_api.dto.TaxFormDetailsDto;
import consulting.reason.tax_forms_api.dto.TaxFormDto;
import consulting.reason.tax_forms_api.dto.request.TaxFormDetailsRequest;
import consulting.reason.tax_forms_api.enums.TaxFormStatus;
import consulting.reason.tax_forms_api.service.TaxFormService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = TaxFormController.class)
public class TaxFormControllerTest extends AbstractControllerTest {
    @Autowired
    protected MockMvc mockMvc;
    @MockBean
    private TaxFormService taxFormService;
    private final TaxFormDetailsDto taxFormDetailsDto = TaxFormDetailsDto.builder()
            .ratio(0.5)
            .assessedValue(100)
            .appraisedValue(1000L)
            .comments("testing")
            .build();
    private final TaxFormDetailsRequest taxFormDetailsRequest = TaxFormDetailsRequest.builder()
            .ratio(0.5)
            .assessedValue(100)
            .appraisedValue(1000L)
            .comments("testing")
            .build();
    private final TaxFormDto taxFormDto = TaxFormDto.builder()
            .id(1)
            .details(taxFormDetailsDto)
            .formName("Testing form RCC")
            .formYear(2024)
            .createdAt(ZonedDateTime.now())
            .updatedAt(ZonedDateTime.now())
            .build();

    @Test
    void testFindAllByYear() throws Exception {
        given(taxFormService.findAllByYear(2024)).willReturn(List.of(taxFormDto));

        mockMvc.perform(get(Endpoints.FORMS)
                        .param("year", "2024")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(taxFormDto))));
    }

    @Test
    void testFindById() throws Exception {
        given(taxFormService.findById(taxFormDto.getId())).willReturn(Optional.of(taxFormDto));

        mockMvc.perform(get(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(taxFormDto)));
    }

    @Test
    void testFindByIdHandlesNotFound() throws Exception {
        mockMvc.perform(get(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSave() throws Exception {
        given(taxFormService.save(taxFormDto.getId(), taxFormDetailsRequest)).willReturn(Optional.of(taxFormDto));

        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .content(objectMapper.writeValueAsString(taxFormDetailsRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(taxFormDto)));
    }

    @Test
    void testSaveHandlesNotFound() throws Exception {
        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .content(objectMapper.writeValueAsString(taxFormDetailsRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testAssessedValueIsInvalid() throws Exception {
        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assessedValue\": -1, \"appraisedValue\": 50000, \"ratio\": 0.5, \"comments\": \"testing\"}"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testAppraisedValueIsInvalid() throws Exception {
        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assessedValue\": 0.5, \"appraisedValue\": -1, \"ratio\": 0.5, \"comments\": \"testing\"}"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testRatioIsInvalid() throws Exception {
        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assessedValue\": 1, \"appraisedValue\": 50000, \"ratio\": 5, \"comments\": \"testing\"}"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testCommentsIsInvalid() throws Exception {
        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assessedValue\": 1, \"appraisedValue\": 50000, \"ratio\": 0.5, \"comments\": \"" + "t".repeat(501) + "\"}"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testSubmit() throws Exception {
    	given(taxFormService.submitForm(taxFormDto.getId())).willAnswer(invocation -> {
            taxFormDto.setStatus(TaxFormStatus.SUBMITTED);
            return Optional.of(taxFormDto);
        });
        MvcResult result = mockMvc.perform(get(Endpoints.FORMS + "/submit/" + taxFormDto.getId()))
        		.andExpect(status().isOk())
        		.andReturn();
        TaxFormDto updatedTaxForm = objectMapper.readValue(result.getResponse().getContentAsString(), TaxFormDto.class);
        assertEquals(TaxFormStatus.SUBMITTED, updatedTaxForm.getStatus());
    }
    
    @Test
    void testSubmitHandlesNotFound() throws Exception {
        mockMvc.perform(get(Endpoints.FORMS + "/submit/" + taxFormDto.getId()))
        		.andExpect(status().isNotFound());
    }
    
    @Test
    void testReturnForm() throws Exception {
    	given(taxFormService.returnForm(taxFormDto.getId())).willAnswer(invocation -> {
            taxFormDto.setStatus(TaxFormStatus.RETURNED);
            return Optional.of(taxFormDto);
        });
        MvcResult result = mockMvc.perform(get(Endpoints.FORMS + "/return/" + taxFormDto.getId()))
        		.andExpect(status().isOk())
        		.andReturn();
        TaxFormDto updatedTaxForm = objectMapper.readValue(result.getResponse().getContentAsString(), TaxFormDto.class);
        assertEquals(TaxFormStatus.RETURNED, updatedTaxForm.getStatus());
    }
    
    @Test
    void testReturntHandlesNotFound() throws Exception {
        mockMvc.perform(get(Endpoints.FORMS + "/return/" + taxFormDto.getId()))
        		.andExpect(status().isNotFound());
    }
    
    @Test
    void testAcceptForm() throws Exception {
    	given(taxFormService.acceptForm(taxFormDto.getId())).willAnswer(invocation -> {
            taxFormDto.setStatus(TaxFormStatus.ACCEPTED);
            return Optional.of(taxFormDto);
        });
        MvcResult result = mockMvc.perform(get(Endpoints.FORMS + "/accept/" + taxFormDto.getId()))
        		.andExpect(status().isOk())
        		.andReturn();
        TaxFormDto updatedTaxForm = objectMapper.readValue(result.getResponse().getContentAsString(), TaxFormDto.class);
        assertEquals(TaxFormStatus.ACCEPTED, updatedTaxForm.getStatus());
    }
    
    @Test
    void testAcceptHandlesNotFound() throws Exception {
        mockMvc.perform(get(Endpoints.FORMS + "/accepted/" + taxFormDto.getId()))
        		.andExpect(status().isNotFound());
    }
}
