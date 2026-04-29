package com.startup_service.Util;

import com.startup_service.Entity.Startup;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class StartupSpecification {

    private StartupSpecification() {
    }

    public static Specification<Startup> search(String category, String status, String currentRound, String stage, String founderEmail) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (category != null && !category.isBlank()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("category")), category.toLowerCase()));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get("status")), status.toUpperCase()));
            }
            if (currentRound != null && !currentRound.isBlank()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("currentRound")), currentRound.toLowerCase()));
            }
            if (stage != null && !stage.isBlank()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("stage")), stage.toLowerCase()));
            }
            if (founderEmail != null && !founderEmail.isBlank()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("founderEmail")), founderEmail.toLowerCase()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Startup> search(String category, String status, String currentRound, String stage) {
        return search(category, status, currentRound, stage, null);
    }
}
