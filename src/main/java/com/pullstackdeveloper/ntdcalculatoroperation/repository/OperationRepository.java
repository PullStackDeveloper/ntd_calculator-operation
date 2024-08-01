package com.pullstackdeveloper.ntdcalculatoroperation.repository;

import com.pullstackdeveloper.ntdcalculatoroperation.entity.Operation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRepository extends JpaRepository<Operation, Long> {
    Operation findByType(String type);
}