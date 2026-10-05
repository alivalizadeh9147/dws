package ir.av.dws.wallet.core.application.usecases.base;

import ir.av.dws.wallet.core.application.ports.inbound.base.BaseRequest;
import ir.av.dws.wallet.core.application.ports.inbound.base.UseCaseResult;

public interface UseCase<REQUEST extends BaseRequest, RESPONSE> {

    UseCaseResult<RESPONSE> execute(REQUEST request);
}
