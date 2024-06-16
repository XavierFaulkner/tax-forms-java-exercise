package consulting.reason.tax_forms_api.service;

import consulting.reason.tax_forms_api.dto.TaxFormDto;
import consulting.reason.tax_forms_api.dto.request.TaxFormDetailsRequest;

import java.util.List;
import java.util.Optional;

public interface TaxFormService {
    List<TaxFormDto> findAllByYear(Integer year);

    Optional<TaxFormDto> findById(Integer id);

    Optional<TaxFormDto> save(Integer id, TaxFormDetailsRequest taxFormDetailsRequest);
    
    Optional<TaxFormDto> submitForm(Integer id);
}
