package com.gestmed.scheduling.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeParseException;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GraphQLExceptionResolver
        extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(
            Throwable exception,
            DataFetchingEnvironment environment) {

        if (exception instanceof AppointmentNotFoundException) {
            return buildError(
                    exception,
                    environment,
                    ErrorType.NOT_FOUND,
                    AppointmentErrorCode.APPOINTMENT_NOT_FOUND
            );
        }

        if (exception instanceof UserNotFoundException) {
            return buildError(
                    exception,
                    environment,
                    ErrorType.NOT_FOUND,
                    AppointmentErrorCode.USER_NOT_FOUND
            );
        }

        if (exception instanceof ScheduleConflictException) {
            return buildError(
                    exception,
                    environment,
                    ErrorType.BAD_REQUEST,
                    AppointmentErrorCode.SCHEDULE_CONFLICT
            );
        }

        if (exception instanceof InvalidAppointmentException) {
            return buildError(
                    exception,
                    environment,
                    ErrorType.BAD_REQUEST,
                    AppointmentErrorCode.INVALID_APPOINTMENT
            );
        }

        if (exception instanceof DateTimeParseException) {
            return GraphqlErrorBuilder
                    .newError(environment)
                    .message(
                            "Formato de data inválido. " +
                                    "Utilize o formato yyyy-MM-dd'T'HH:mm:ss"
                    )
                    .errorType(ErrorType.BAD_REQUEST)
                    .extensions(Map.of(
                            "code",
                            AppointmentErrorCode
                                    .INVALID_DATE_FORMAT
                                    .name()
                    ))
                    .build();
        }

        return null;
    }

    private GraphQLError buildError(
            Throwable exception,
            DataFetchingEnvironment environment,
            ErrorType errorType,
            AppointmentErrorCode errorCode) {

        return GraphqlErrorBuilder
                .newError(environment)
                .message(exception.getMessage())
                .errorType(errorType)
                .extensions(Map.of(
                        "code",
                        errorCode.name()
                ))
                .build();
    }
}