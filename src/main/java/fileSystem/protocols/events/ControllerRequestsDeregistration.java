package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import static fileSystem.protocols.Protocol.CONTROLLER_REQUESTS_DEREGISTRATION;

public class ControllerRequestsDeregistration implements Event {

    static final int type = CONTROLLER_REQUESTS_DEREGISTRATION;

    // TODO: pass a hash or password on register to Controller, which it will then pass
    //  - back in order to authenticate deregister

    public ControllerRequestsDeregistration() {
    }


    @Override
    public int getType() {
        return type;
    }

}
