package consulting.reason.tax_forms_api.entity;

import java.time.ZonedDateTime;

import org.hibernate.annotations.CreationTimestamp;

import consulting.reason.tax_forms_api.enums.TaxFormHistoryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tax_form_history")
@Entity
public class TaxFormHistory {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@ManyToOne
    @JoinColumn(name = "tax_form_id", nullable = false)
	private TaxForm taxForm;
	@CreationTimestamp
    private ZonedDateTime createdAt;
	@Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
	private TaxFormHistoryType type;
}
