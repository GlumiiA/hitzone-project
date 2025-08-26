package validation;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Validator {
    private double x, y, r;

    private boolean validateX() {
        return x >= -4 && x <= 4;
    }

    private boolean validateY() {
        return y >= -5 && y <= 5;
    }

    private boolean validateR() {
        return r >= 0.1 && r <= 5;
    }

    public boolean validateDot() {
        return validateX() && validateY() && validateR();
    }
}
