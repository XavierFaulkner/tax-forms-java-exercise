package consulting.reason.tax_forms_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import consulting.reason.tax_forms_api.AbstractServiceTest;
import consulting.reason.tax_forms_api.dto.TaxFormDetailsDto;
import consulting.reason.tax_forms_api.dto.TaxFormDto;
import consulting.reason.tax_forms_api.dto.request.TaxFormDetailsRequest;
import consulting.reason.tax_forms_api.entity.TaxForm;
import consulting.reason.tax_forms_api.entity.TaxFormHistory;
import consulting.reason.tax_forms_api.enums.TaxFormStatus;
import consulting.reason.tax_forms_api.exception.TaxFormStatusException;
import consulting.reason.tax_forms_api.repository.TaxFormHistoryRepository;
import consulting.reason.tax_forms_api.repository.TaxFormRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@ExtendWith(MockitoExtension.class)
public class TaxFormServiceTest extends AbstractServiceTest {
    @Autowired
    @SpyBean
    private TaxFormRepository taxFormRepository;
    @Autowired
    @SpyBean
    private TaxFormHistoryRepository taxFormHistoryRepository;
    private TaxFormService taxFormService;
    private TaxForm taxForm;
    private TaxFormDto taxFormDto;
    private Validator validator;
    private final TaxFormDetailsRequest taxFormDetailsRequest = TaxFormDetailsRequest.builder()
            .ratio(0.5)
            .assessedValue(100)
            .appraisedValue(200L)
            .comments("Testing")
            .build();

    @BeforeEach
    void before() {
        taxFormService = new TaxFormServiceImpl(
                taxFormRepository,
                modelMapper,
                taxFormHistoryRepository
        );

        taxForm = taxFormRepository.save(TaxForm.builder()
                .formName("Test Form 1")
                .formYear(2024)
                .status(TaxFormStatus.NOT_STARTED)
                .build());
        taxFormDto = modelMapper.map(taxForm, TaxFormDto.class);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testFindAll() {
        assertThat(taxFormService.findAllByYear(2024)).containsExactly(taxFormDto);
        assertThat(taxFormService.findAllByYear(2025)).isEmpty();
    }

    @Test
    void testFindById() {
        assertThat(taxFormService.findById(taxForm.getId())).isEqualTo(Optional.of(taxFormDto));
        assertThat(taxFormService.findById(0)).isEmpty();
    }

    @Test
    void testSave() {
        TaxFormDetailsDto taxFormDetailsDto = TaxFormDetailsDto.builder()
                .ratio(0.5)
                .assessedValue(100)
                .appraisedValue(200L)
                .comments("Testing")
                .build();

        Optional<TaxFormDto> taxFormDto1 = taxFormService.save(taxForm.getId(), taxFormDetailsRequest);
        assertThat(taxFormDto1).isPresent();
        assertThat(taxFormDto1.get().getDetails()).isEqualTo(taxFormDetailsDto);

        assertThat(taxFormService.save(0, taxFormDetailsRequest)).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = TaxFormStatus.class, names = {
            "SUBMITTED",
            "ACCEPTED"
    })
    void testSaveHandlesInvalidStatus(TaxFormStatus taxFormStatus) {
        taxForm.setStatus(taxFormStatus);

        TaxFormStatusException taxFormStatusException = new TaxFormStatusException(
                taxForm,
                TaxFormStatus.IN_PROGRESS
        );

        assertThatThrownBy(() -> taxFormService.save(taxForm.getId(), taxFormDetailsRequest))
                .isInstanceOf(TaxFormStatusException.class)
                .hasMessage(taxFormStatusException.getMessage());
    }
    
    @Test
    void testSaveWithInvalidRequest() {
        TaxFormDetailsRequest invalidRequest = TaxFormDetailsRequest.builder()
                .ratio(-0.5)
                .assessedValue(-100)
                .appraisedValue(-200L)
                .comments("T".repeat(501))
                .build();

        Set<ConstraintViolation<TaxFormDetailsRequest>> violations = validator.validate(invalidRequest);
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSize(4);
    }
    
    @Test
    void testFormHistoryRelationship() {
    	assertThat(taxFormService.findById(taxForm.getId()).get().getHistory()).hasSize(0);
    }
    
    @Test
    void testSubmitForm() {
    	taxForm.setStatus(TaxFormStatus.IN_PROGRESS);
		Optional<TaxFormDto> result = taxFormService.submitForm(taxForm.getId());
		assertTrue(result.isPresent());
		assertEquals(TaxFormStatus.SUBMITTED, result.get().getStatus());
		verify(taxFormHistoryRepository).save(any(TaxFormHistory.class));
    }
    
    @Test
    void testSubmitFormInvalidWorkflow() {
        //taxForm status is NOT_STARTED
    	assertThrows(TaxFormStatusException.class, () -> taxFormService.submitForm(taxForm.getId()));
    	assertEquals(TaxFormStatus.NOT_STARTED, taxForm.getStatus());
        verify(taxFormHistoryRepository, never()).save(any(TaxFormHistory.class));
    }
    
    @Test
    void testSubmitFormIdNotFound() {
    	Optional<TaxFormDto> result = taxFormService.submitForm(0);
    	assertThat(result).isEmpty();
    }
    
    @Test
    void testReturnForm() {
    	taxForm.setStatus(TaxFormStatus.SUBMITTED);
		Optional<TaxFormDto> result = taxFormService.returnForm(taxForm.getId());
		assertTrue(result.isPresent());
		assertEquals(TaxFormStatus.RETURNED, result.get().getStatus());
		verify(taxFormHistoryRepository).save(any(TaxFormHistory.class));
    }
    
    @Test
    void testReturnFormInvalidWorkflow() {
        //taxForm status is NOT_STARTED
    	assertThrows(TaxFormStatusException.class, () -> taxFormService.returnForm(taxForm.getId()));
    	assertEquals(TaxFormStatus.NOT_STARTED, taxForm.getStatus());
        verify(taxFormHistoryRepository, never()).save(any(TaxFormHistory.class));
    }
    
    @Test
    void testReturnFormIdNotFound() {
    	Optional<TaxFormDto> result = taxFormService.returnForm(0);
    	assertThat(result).isEmpty();
    }
}
