package consulting.reason.tax_forms_api.service;

import consulting.reason.tax_forms_api.dto.TaxFormDetailsDto;
import consulting.reason.tax_forms_api.dto.TaxFormDto;
import consulting.reason.tax_forms_api.dto.request.TaxFormDetailsRequest;
import consulting.reason.tax_forms_api.entity.TaxFormHistory;
import consulting.reason.tax_forms_api.enums.TaxFormHistoryType;
import consulting.reason.tax_forms_api.repository.TaxFormRepository;
import consulting.reason.tax_forms_api.repository.TaxFormHistoryRepository;
import consulting.reason.tax_forms_api.util.TaxFormStatusUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaxFormServiceImpl implements TaxFormService {
    private final TaxFormRepository taxFormRepository;
    private final TaxFormHistoryRepository taxFormHistoryRepository;
    private final ModelMapper modelMapper;

    public TaxFormServiceImpl(TaxFormRepository taxFormRepository,
                              ModelMapper modelMapper,
                              TaxFormHistoryRepository taxFormHistoryRepository) {
        this.taxFormRepository = taxFormRepository;
		this.taxFormHistoryRepository = taxFormHistoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaxFormDto> findAllByYear(Integer year) {
        return taxFormRepository.findAllByFormYear(year).stream()
                .map(taxForm -> modelMapper.map(taxForm, TaxFormDto.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TaxFormDto> findById(Integer id) {
        return taxFormRepository.findById(id)
                .map(taxForm -> modelMapper.map(taxForm, TaxFormDto.class));
    }

    @Override
    @Transactional
    public Optional<TaxFormDto> save(Integer id, TaxFormDetailsRequest taxFormDetailsRequest) {
        return taxFormRepository.findById(id)
                .map(taxForm -> {
                    TaxFormStatusUtils.save(taxForm);
                    taxForm.setDetails(modelMapper.map(taxFormDetailsRequest, TaxFormDetailsDto.class));

                    taxFormRepository.save(taxForm);

                    return modelMapper.map(taxForm, TaxFormDto.class);
                });
    }

	@Override
	public Optional<TaxFormDto> submitForm(Integer id) {
		return taxFormRepository.findById(id)
                .map(taxForm -> {
                    TaxFormStatusUtils.submit(taxForm);

                    taxFormRepository.save(taxForm);
                    
                    TaxFormHistory history = TaxFormHistory.builder()
        					.taxForm(taxForm)
        					.createdAt(ZonedDateTime.now())
        					.type(TaxFormHistoryType.SUBMITTED)
        					.build();
                    
                    taxFormHistoryRepository.save(history);

                    return modelMapper.map(taxForm, TaxFormDto.class);
                });
	}

	@Override
	public Optional<TaxFormDto> returnForm(Integer id) {
		return taxFormRepository.findById(id)
                .map(taxForm -> {
                    TaxFormStatusUtils.returnForm(taxForm);

                    taxFormRepository.save(taxForm);
                    
                    TaxFormHistory history = TaxFormHistory.builder()
        					.taxForm(taxForm)
        					.createdAt(ZonedDateTime.now())
        					.type(TaxFormHistoryType.RETURNED)
        					.build();
                    
                    taxFormHistoryRepository.save(history);

                    return modelMapper.map(taxForm, TaxFormDto.class);
                });
	}

	@Override
	public Optional<TaxFormDto> acceptForm(Integer id) {
		return taxFormRepository.findById(id)
                .map(taxForm -> {
                    TaxFormStatusUtils.accept(taxForm);

                    taxFormRepository.save(taxForm);
                    
                    TaxFormHistory history = TaxFormHistory.builder()
        					.taxForm(taxForm)
        					.createdAt(ZonedDateTime.now())
        					.type(TaxFormHistoryType.ACCEPTED)
        					.build();
                    
                    taxFormHistoryRepository.save(history);

                    return modelMapper.map(taxForm, TaxFormDto.class);
                });
	}
}
