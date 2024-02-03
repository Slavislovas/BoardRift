package com.socialnetwork.boardrift.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AgeValidator implements ConstraintValidator<AgeConstraint, String> {
    private static final int MIN_AGE = 18;

    @Override
    public void initialize(AgeConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(String dateOfBirth, ConstraintValidatorContext context) {
        try {
            LocalDate birthDate = LocalDate.parse(dateOfBirth);
            LocalDate currentDate = LocalDate.now();
            Period period = Period.between(birthDate, currentDate);

            if (period.getYears() > MIN_AGE) {
                return true;
            } else if (period.getYears() == MIN_AGE) {
                // Check month and day for exact 18 years
                return birthDate.plusYears(MIN_AGE).isBefore(currentDate)
                        || (birthDate.plusYears(MIN_AGE).isEqual(currentDate)
                        && birthDate.getMonthValue() <= currentDate.getMonthValue()
                        && birthDate.getDayOfMonth() <= currentDate.getDayOfMonth());
            }
            return false;
        } catch (Exception e) {
            return false; // Handle invalid date format
        }
    }
}