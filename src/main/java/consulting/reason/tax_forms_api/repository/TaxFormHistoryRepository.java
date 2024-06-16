package consulting.reason.tax_forms_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import consulting.reason.tax_forms_api.entity.TaxFormHistory;

@Repository
public interface TaxFormHistoryRepository extends JpaRepository<TaxFormHistory, Integer> {

}
