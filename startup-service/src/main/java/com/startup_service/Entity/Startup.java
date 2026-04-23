package com.startup_service.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.ElementCollection;
import java.util.List;
import java.util.ArrayList;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Startup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private String founderEmail;

    private String tagline;

    private String location;

    private Integer foundedYear;

    private Integer teamSize = 0;

    private Double mrr = 0.0;

    private Double fundingGoal;

    private Double currentFunding = 0.0;

    private String category;

    private String stage;

    private String currentRound;

    private Double valuation;

    private String status = "PENDING"; // PENDING / OPEN / CLOSED / REJECTED

    @ElementCollection
    private List<String> followers = new ArrayList<>();

    @CreationTimestamp
    @jakarta.persistence.Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
