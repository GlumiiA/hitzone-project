package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.json.JsonObject;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.json.Json;

@Entity
@Table(name = "results")
@Getter
@Setter
@NoArgsConstructor
public class Result implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "x", nullable = false)
    private double x;
    @Column(name = "y", nullable = false)
    private double y;
    @Column(name = "r", nullable = false)
    private double r;
    @Column(name = "isHit", nullable = false)
    private boolean hit;
    @Column(name = "script_time", nullable = false)
    private long scriptTime;
    @Column(name = "start_time", nullable = false)
    private long startTime;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Result(double x, double y, double r) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.hit = checkHit(x, y, r);
    }

    public boolean checkHit(double x, double y, double r) {
        boolean circle = (x >= 0 && y <= 0 && x*x + y*y <= r*r);
        boolean rectangle = (x <= 0 && y >= 0 && -r <= x && y <= r/2);
        boolean triangle = (x <= 0 && y <= 0 && y >= -x - r);
        return circle || rectangle || triangle;
    }

    public JsonObject toJSONObject() {
        return Json.createObjectBuilder()
                .add("x", x)
                .add("y", y)
                .add("r", r)
                .add("isHit", hit)
                .add("scriptTime", scriptTime)
                .add("startTime", LocalDateTime
                        .ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault())
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
                )
                .build();
    }

}
