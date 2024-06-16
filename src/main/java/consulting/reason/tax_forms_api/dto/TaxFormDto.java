package consulting.reason.tax_forms_api.dto;

import consulting.reason.tax_forms_api.enums.TaxFormStatus;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TaxFormDto {
    private Integer id;
    private Integer formYear;
    private String formName;
    private TaxFormStatus status;
    private TaxFormDetailsDto details;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private List<TaxFormHistoryDto> history;
}
