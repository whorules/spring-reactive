package com.korovko.reactive.utils;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;
import reactor.core.publisher.Signal;

import java.util.Optional;
import java.util.function.Consumer;

@UtilityClass
public class LoggingUtils {

  public <T> Consumer<Signal<T>> logOnNext(final Consumer<T> logStatement) {
    return signal -> {
      if (!signal.isOnNext()) {
        return;
      }
      Optional<String> toPutInMdc = signal.getContextView().getOrEmpty("CONTEXT_KEY");

      toPutInMdc.ifPresentOrElse(tpim -> {
        try (MDC.MDCCloseable cMdc = MDC.putCloseable("MDC_KEY", tpim)) {
          logStatement.accept(signal.get());
        }
      }, () -> logStatement.accept(signal.get()));
    };
  }

  public Consumer<Signal<?>> logOnError(final Consumer<Throwable> errorLogStatement) {
    return signal -> {
      if (!signal.isOnError()) {
        return;
      }
      Optional<String> toPutInMdc = signal.getContextView().getOrEmpty("CONTEXT_KEY");

      toPutInMdc.ifPresentOrElse(tpim -> {
        try (MDC.MDCCloseable cMdc = MDC.putCloseable("MDC_KEY", tpim)) {
          errorLogStatement.accept(signal.getThrowable());
        }
      }, () -> errorLogStatement.accept(signal.getThrowable()));
    };
  }

}
