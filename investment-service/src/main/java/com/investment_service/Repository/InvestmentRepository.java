package com.investment_service.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.investment_service.Entity.Investment;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {

    List<Investment> findByInvestorEmail(String email);

    List<Investment> findByStartupId(Long startupId);

    Optional<Investment> findByInvestorEmailAndStartupId(String email, Long startupId);
}
