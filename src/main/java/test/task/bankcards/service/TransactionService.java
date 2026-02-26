package test.task.bankcards.service;

import test.task.bankcards.dto.request.TransferRequest;

public interface TransactionService {
    void transfer(TransferRequest transferRequest);
}
