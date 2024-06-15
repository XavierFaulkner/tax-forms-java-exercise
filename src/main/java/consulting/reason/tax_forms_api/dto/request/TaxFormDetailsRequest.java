package consulting.reason.tax_forms_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class TaxFormDetailsRequest {
	@NotNull
	@Min(0)
	@Max(100000)
    private Integer assessedValue;
	
	@Min(0)
	@Max(100000)
    private Long appraisedValue;
	
	@NotNull
	@DecimalMin("0.0")
	@DecimalMax("1.0")
    private Double ratio;
	
	@Size(max = 500)
    private String comments;
}
