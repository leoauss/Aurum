package com.example.aurum.domain.usecase

import java.math.BigDecimal
import javax.inject.Inject

/**
 * Validates a user-typed amount string for a UPI payment.
 *
 * Returns [Result.success] with the parsed [BigDecimal] if all rules pass,
 * or [Result.failure] with an [AmountValidationException] describing the problem.
 *
 * Rules:
 *  - Must be non-blank.
 *  - Must be a valid number.
 *  - Must be positive (> 0).
 *  - Must have at most 2 decimal places.
 *  - Must not exceed [maxAmount] (default ₹1,00,000).
 */
class ValidateAmount @Inject constructor() {

    operator fun invoke(
        input: String,
        maxAmount: BigDecimal = DEFAULT_MAX_AMOUNT,
    ): Result<BigDecimal> {
        val trimmed = input.trim()

        if (trimmed.isBlank()) {
            return failure(AmountError.Empty)
        }

        // Reject scientific notation (e.g. 1e2), leading plus (+50), and invalid characters
        if (!DECIMAL_NUMBER_REGEX.matches(trimmed)) {
            return failure(AmountError.NotANumber)
        }

        val value = trimmed.toBigDecimalOrNull()
            ?: return failure(AmountError.NotANumber)

        if (value <= BigDecimal.ZERO) {
            return failure(AmountError.NotPositive)
        }

        // Check decimal places — use stripTrailingZeros() so "10.00" is fine
        if (value.stripTrailingZeros().scale() > 2) {
            return failure(AmountError.TooManyDecimals)
        }

        if (value > maxAmount) {
            return failure(AmountError.ExceedsCap(maxAmount))
        }

        return Result.success(value)
    }

    private fun failure(error: AmountError) = Result.failure<BigDecimal>(AmountValidationException(error))

    companion object {
        val DEFAULT_MAX_AMOUNT: BigDecimal = BigDecimal("100000") // ₹1,00,000
        private val DECIMAL_NUMBER_REGEX = Regex("""^-?\d+(\.\d+)?$""")
    }
}

/** Describes why amount validation failed. */
sealed interface AmountError {
    /** Input string was blank. */
    data object Empty : AmountError

    /** Input string could not be parsed as a number. */
    data object NotANumber : AmountError

    /** Parsed value is zero or negative. */
    data object NotPositive : AmountError

    /** Parsed value has more than 2 decimal places. */
    data object TooManyDecimals : AmountError

    /** Parsed value exceeds the allowed maximum. */
    data class ExceedsCap(val cap: BigDecimal) : AmountError
}

/** Thrown (wrapped in [Result.failure]) when amount validation fails. */
class AmountValidationException(val error: AmountError) :
    IllegalArgumentException("Amount validation error: $error")

