package com.ibrahim.studentserver.beans;

import com.ibrahim.studentserver.token.Token;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Table_user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "User_id")
    private Integer id;

    @Column(name = "User_firstName")
    @NotBlank(message = "firstname is required !")
    @Max(value = 10, message = "The maximum firstname size most be 10 characters")
    @Min(value = 5, message = "The minimum firstname size most be 5 characters")
    private  String firstName;

    @NotBlank(message = "lastName is required !")
    @Max(value = 10, message = "The maximum lastName size most be 10 characters")
    @Min(value = 5, message = "The minimum lastName size most be 5 characters")
    @Column(name = "User_lastName")
    private String lastName ;

    @Column(name = "User_email")
    @Email(message="Enter valid email address")
    private String email;

    @Column(name = "User_password", nullable = false, length = 255)
    @NotBlank(message = "password is required !")

    private String password;

    @Column(name = "Created_on")
    @CreationTimestamp
    private Date createdOn ;

    @Column(name = "Last_update_on")
    @UpdateTimestamp
    private Date lastUpdateOn ;

    @Column(name = "User_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role ;

    @OneToMany(mappedBy = "user")
    private List<Token> tokenList ;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
