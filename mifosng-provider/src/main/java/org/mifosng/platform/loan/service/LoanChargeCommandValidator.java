package org.mifosng.platform.loan.service;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.LoanChargeCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

import java.util.ArrayList;
import java.util.List;

public class LoanChargeCommandValidator {

    private final LoanChargeCommand command;

    public LoanChargeCommandValidator(final LoanChargeCommand command) {
        this.command = command;
    }

    public void validateForCreate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");

        baseDataValidator.reset().parameter("chargeId").value(command.getChargeId()).notNull().longGreaterThanZero();
        baseDataValidator.reset().parameter("amount").value(command.getAmount()).notNull().positiveAmount();
//        baseDataValidator.reset().parameter("chargeTimeType").value(command.getChargeTimeType()).ignoreIfNull().inMinMaxRange(1, 2);
//        
//        if (command.getChargeTimeType().equals(Integer.valueOf(2))) {
//        	// date must be provided
//        	 baseDataValidator.reset().parameter("specifiedDueDate").value(command.getSpecifiedDueDate()).notNull();
//        }
//        
//        baseDataValidator.reset().parameter("chargeCalculationType").value(command.getChargeCalculationType()).ignoreIfNull().inMinMaxRange(1, 4);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
        }
    }

    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");

        baseDataValidator.reset().parameter("amount").value(command.getAmount()).ignoreIfNull().positiveAmount();
        baseDataValidator.reset().parameter("chargeTimeType").value(command.getChargeTimeType()).ignoreIfNull().inMinMaxRange(1, 2);
        baseDataValidator.reset().parameter("chargeCalculationType").value(command.getChargeCalculationType()).ignoreIfNull().inMinMaxRange(1, 4);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
        }
    }
}