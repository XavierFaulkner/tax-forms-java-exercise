package consulting.reason.tax_forms_api.entity;

import consulting.reason.tax_forms_api.dto.TaxFormDetailsDto;
import consulting.reason.tax_forms_api.enums.TaxFormStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tax_forms")
@Entity
public class TaxForm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private Integer formYear;
    @Column(nullable = false)
    private String formName;
    @Type(JsonType.class)
    @Column(name = "details", columnDefinition = "VARCHAR2")
    private TaxFormDetailsDto details;
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private TaxFormStatus status;
    @CreationTimestamp
    private ZonedDateTime createdAt;
    @UpdateTimestamp
    private ZonedDateTime updatedAt;
    @OneToMany(mappedBy = "taxForm", cascade = CascadeType.ALL)
    private List<TaxFormHistory> history;
}
