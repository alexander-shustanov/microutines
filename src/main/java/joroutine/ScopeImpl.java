package joroutine;

import static joroutine.Context.getCurrent;

@SuppressWarnings("rawtypes")
class ScopeImpl implements Scope {
    Continuation continuation;


    @Override
    @Suspend
    public void delay(long millis) {
        getCurrent().getDispatcher().schedule(getCurrent(), continuation, millis);
    }

    @Override
    @Suspend
    public void await(Deferred deferred) {
//        launch(getCurrent(), createCheckSuspendable(deferred));
    }

    @Suspend
    @Override
    public void launchAwait(Context context, Suspendable suspendable) {
        Context currentContext = getCurrent();
        context.launch(suspendable, () -> {
            currentContext.getDispatcher().schedule(currentContext, continuation);
        });
    }

//    private Suspendable createCheckSuspendable(Deferred deferred) {
//        return new Suspendable() {
//            @Override
//            public void run(Scope scope) {
//                if (deferred.isDone())
//                    getCurrent().getDispatcher().schedule(getCurrent(), continuation);
//                else
//                    launch(getCurrent(), createCheckSuspendable(deferred));
//            }
//        };
//    }
}
