package com.farmer.Form.Entity;

import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDateTime;
 
@Entity
@Table(name = "otp", indexes = {
        @Index(name = "idx_email_or_phone", columnList = "email_or_phone"),
        @Index(name = "idx_otp_code", columnList = "otp_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Otp {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(name = "email_or_phone", nullable = false)
    private String emailOrPhone;
 
    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;
 
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;
}
 
