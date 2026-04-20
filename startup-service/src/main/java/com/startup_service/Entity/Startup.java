package com.startup_service.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.ElementCollection;
import java.util.List;
import java.util.ArrayList;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Startup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private String founderEmail;

    private Double fundingGoal;

    private Double currentFunding = 0.0;

    private String category;

    private String currentRound;

    private Double valuation;

    private String status = "PENDING"; // PENDING / OPEN / CLOSED / REJECTED

    @ElementCollection
    private List<String> followers = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;
}
