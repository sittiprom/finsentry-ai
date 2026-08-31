package com.finsentry.finsentry_ai.repository;

import com.finsentry.finsentry_ai.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findByCustomerId(String customerId);

}
