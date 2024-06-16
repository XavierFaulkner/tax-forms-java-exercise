package consulting.reason.tax_forms_api.dto;

import java.time.ZonedDateTime;

import consulting.reason.tax_forms_api.enums.TaxFormHistoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TaxFormHistoryDto {
	private Integer id;
	private TaxFormDto taxForm;
	private ZonedDateTime createdAt;
	private TaxFormHistoryType type;
}
