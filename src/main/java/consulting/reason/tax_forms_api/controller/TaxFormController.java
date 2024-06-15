package consulting.reason.tax_forms_api.controller;

import consulting.reason.tax_forms_api.dto.TaxFormDto;
import consulting.reason.tax_forms_api.dto.request.TaxFormDetailsRequest;
import consulting.reason.tax_forms_api.exception.TaxFormNotFoundException;
import consulting.reason.tax_forms_api.service.TaxFormService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Endpoints.FORMS)
public class TaxFormController {
    private final TaxFormService taxFormService;

    public TaxFormController(TaxFormService taxFormService) {
        this.taxFormService = taxFormService;
    }

    @GetMapping
    public List<TaxFormDto> findAllByYear(@RequestParam("year") Integer year) {
        return taxFormService.findAllByYear(year);
    }

    @GetMapping("/{id}")
    public TaxFormDto findById(@PathVariable("id") Integer id) {
        return taxFormService.findById(id)
                .orElseThrow(() -> new TaxFormNotFoundException(id));
    }

    @PatchMapping("/{id}")
    public TaxFormDto save(@PathVariable("id") Integer id, @Validated @RequestBody TaxFormDetailsRequest taxFormDetailsRequest) {
        return taxFormService.save(id, taxFormDetailsRequest)
                .orElseThrow(() -> new TaxFormNotFoundException(id));
    }
}
