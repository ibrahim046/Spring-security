package com.ibrahim.studentserver.token;

import com.ibrahim.studentserver.beans.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Table_token")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Token_id")
    @Basic(optional = false)
    private Integer id;

    @Column(name = "Token")
    private String token;

    @Column(name = "Token_type")
    @Enumerated(EnumType.STRING)
    private TokenType tokenType ;

    @Column(name = "Token_expired")
    private boolean expired;

    @Column(name="Token_revoked")
    private boolean revoked ;

    @ManyToOne
    @JoinColumn(name = "User_id")
    private User user ;

}
