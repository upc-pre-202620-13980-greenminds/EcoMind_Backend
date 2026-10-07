package pe.greenminds.ecomind.shared.application.result;

import java.util.Optional;
import java.util.function.Function;

/**
 * Outcome of a command or query: either a success with a value or a failure with an error.
 * Application services return it instead of throwing exceptions for expected failures.
 *
 * @param <T> type of the success value
 * @param <E> type of the error
 */
public sealed interface Result<T, E> {

  record Success<T, E>(T value) implements Result<T, E> {
  }

  record Failure<T, E>(E error) implements Result<T, E> {
  }

  static <T, E> Result<T, E> success(T value) {
    return new Success<>(value);
  }

  static <T, E> Result<T, E> failure(E error) {
    return new Failure<>(error);
  }

  default boolean isSuccess() {
    return this instanceof Success;
  }

  default boolean isFailure() {
    return this instanceof Failure;
  }

  default Optional<T> toOptional() {
    return switch (this) {
      case Success<T, E> success -> Optional.of(success.value());
      case Failure<T, E> failure -> Optional.empty();
    };
  }

  default T getOrElse(T defaultValue) {
    return switch (this) {
      case Success<T, E> success -> success.value();
      case Failure<T, E> failure -> defaultValue;
    };
  }

  /** Transforms the value of a success; a failure is returned unchanged. */
  default <T2> Result<T2, E> map(Function<T, T2> mapper) {
    return switch (this) {
      case Success<T, E> success -> Result.success(mapper.apply(success.value()));
      case Failure<T, E> failure -> Result.failure(failure.error());
    };
  }

  /** Chains another operation that can also fail; the first failure stops the chain. */
  default <T2> Result<T2, E> flatMap(Function<T, Result<T2, E>> mapper) {
    return switch (this) {
      case Success<T, E> success -> mapper.apply(success.value());
      case Failure<T, E> failure -> Result.failure(failure.error());
    };
  }

  /** Transforms the error of a failure; a success is returned unchanged. */
  default <E2> Result<T, E2> mapError(Function<E, E2> mapper) {
    return switch (this) {
      case Success<T, E> success -> Result.success(success.value());
      case Failure<T, E> failure -> Result.failure(mapper.apply(failure.error()));
    };
  }
}
