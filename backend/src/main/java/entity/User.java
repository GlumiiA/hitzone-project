package entity;

import jakarta.json.Json;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor // Добавляет конструктор со всеми полями
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", unique = true, nullable = false)
    private String login;
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Result> results = new ArrayList<>();

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public void addResult(Result result) {
        results.add(result);
        result.setUser(this);
    }

    public String getResultsAsJSON() {
        return Json.createArrayBuilder(
                results.stream()
                        .map(Result::toJSONObject)
                        .toList()
        ).build().toString();
    }

    public void clearResults() {
        results.clear();
    }
}
